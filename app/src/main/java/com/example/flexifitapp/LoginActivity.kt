package com.example.flexifitapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var loginEmail: EditText
    private lateinit var loginPass: EditText
    private lateinit var loginBtn: MaterialButton
    private lateinit var signupRedirect: TextView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    private val KEY_DARK_MODE = "dark_mode"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        applyThemeFromPrefs()
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()
        loginEmail = findViewById(R.id.login_email)
        loginPass = findViewById(R.id.login_password)
        loginBtn = findViewById(R.id.login_button)
        signupRedirect = findViewById(R.id.signupRedirectText)

        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)

        loginBtn.setOnClickListener {
            val email = loginEmail.text.toString().trim()
            val pass = loginPass.text.toString().trim()

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                loginEmail.error = "Enter a valid email"
                return@setOnClickListener
            }
            if (pass.isEmpty()) {
                loginPass.error = "Password cannot be empty"
                return@setOnClickListener
            }

            setAuthLoading(true)
            mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = mAuth.currentUser
                        if (user != null && user.isEmailVerified) {
                            fetchFcmAndConnectToBackend(user)
                        } else {
                            setAuthLoading(false)
                            mAuth.signOut()
                            Toast.makeText(this, "Please verify your email first.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        setAuthLoading(false)
                        Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        signupRedirect.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun fetchFcmAndConnectToBackend(user: FirebaseUser) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            val fcmToken = if (task.isSuccessful) task.result else null
            user.getIdToken(true).addOnCompleteListener { tokenTask ->
                if (tokenTask.isSuccessful) {
                    val idToken = tokenTask.result?.token
                    if (idToken != null) {
                        loginToBackendAndBootstrap(idToken, fcmToken)
                    }
                } else {
                    setAuthLoading(false)
                    Toast.makeText(this, "Failed to get ID Token", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loginToBackendAndBootstrap(firebaseToken: String, fcmToken: String?) {
        lifecycleScope.launch {
            try {
                val api = ApiClient.get(this@LoginActivity).create(ApiService::class.java)
                // Gumagamit na ng nullable fields
                val req = LoginRequest(firebaseIdToken = firebaseToken, fcmToken = fcmToken)

                val res = api.login(req)

                if (res.isSuccessful && res.body() != null) {
                    handleSuccessfulAuth(res.body()!!, api)
                } else if (res.code() == 401) {
                    // Dito yung auto-register kapag wala sa SQL DB
                    autoRegisterUser(firebaseToken, fcmToken, api)
                } else {
                    setAuthLoading(false)
                    Toast.makeText(this@LoginActivity, "Login failed: ${res.code()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                setAuthLoading(false)
                Log.e("LOGIN_ERROR", e.message ?: "Unknown error")
                Toast.makeText(this@LoginActivity, "Connection Error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun autoRegisterUser(firebaseToken: String, fcmToken: String?, api: ApiService) {
        val firebaseUser = mAuth.currentUser

        val savedUsername = UserPrefs.getString(this, UserPrefs.KEY_USER_NAME, "")
        val savedName = UserPrefs.getString(this, UserPrefs.KEY_NAME, "")

        // Ito yung dating may error sa null. Ngayon okay na siya!
        val regReq = RegisterRequest(
            firebaseIdToken = firebaseToken,
            name = savedName ?: firebaseUser?.displayName ?: "FlexiFit User",
            username = savedUsername,
            fcmToken = fcmToken
        )

        lifecycleScope.launch {
            try {
                val res = api.register(regReq)
                if (res.isSuccessful && res.body() != null) {
                    handleSuccessfulAuth(res.body()!!, api)
                } else {
                    setAuthLoading(false)
                    Toast.makeText(this@LoginActivity, "Auto-registration failed", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                setAuthLoading(false)
                Toast.makeText(this@LoginActivity, "Registration Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun handleSuccessfulAuth(auth: AuthResponse, api: ApiService) {
        UserPrefs.saveAuth(this@LoginActivity, auth.token, auth.userId, auth.role, auth.status, auth.isVerified)

        val bootRes = api.bootstrap()
        if (bootRes.isSuccessful && bootRes.body() != null) {
            val body = bootRes.body()!!
            if (body.userId != null) {
                UserPrefs.putInt(this@LoginActivity, UserPrefs.KEY_USER_ID, body.userId)
            }
            if (body.profileComplete) goToMain() else goToOnboard()
        } else {
            goToOnboard()
        }
    }

    private fun setAuthLoading(isLoading: Boolean) {
        loginBtn.isEnabled = !isLoading
        loginBtn.text = if (isLoading) "Connecting..." else "Login"
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun goToOnboard() {
        val intent = Intent(this, OnboardingActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun applyThemeFromPrefs() {
        val isDark = sharedPreferences.getBoolean(KEY_DARK_MODE, false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}