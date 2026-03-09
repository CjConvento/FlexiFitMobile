package com.example.flexifitapp

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var loginEmail: EditText
    private lateinit var loginPassword: EditText
    private lateinit var loginButton: MaterialButton
    private lateinit var btnGoogleSignIn: MaterialButton
    private lateinit var signupRedirectText: TextView
    private lateinit var tvForgotPassword: TextView

    private val PREF_NAME = "flexifit_prefs"
    private val KEY_DARK_MODE = "dark_mode"

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mAuth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        val isDark = sharedPreferences.getBoolean(KEY_DARK_MODE, false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )

        super.onCreate(savedInstanceState)
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler(this))
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.create(this)

        setupUi()

        val current = mAuth.currentUser
        if (current != null && current.isEmailVerified) {
            setAuthLoading(true)
            tokenThenBootstrap(current, showTokenDialog = false)
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun setupUi() {
        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)

        loginEmail = findViewById(R.id.login_email)
        loginPassword = findViewById(R.id.login_password)
        loginButton = findViewById(R.id.login_button)
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn)
        signupRedirectText = findViewById(R.id.signupRedirectText)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)

        tvForgotPassword.setOnClickListener { showForgotPasswordDialog() }

        loginButton.setOnClickListener {
            if (!validateEmail() || !validatePassword()) return@setOnClickListener
            signIn()
        }

        btnGoogleSignIn.setOnClickListener {
            startGoogleSignIn()
        }

        signupRedirectText.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun validateEmail(): Boolean {
        val valEmail = loginEmail.text.toString().trim()
        if (valEmail.isEmpty()) {
            loginEmail.error = "Email cannot be empty"
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(valEmail).matches()) {
            loginEmail.error = "Invalid email format"
            return false
        }
        loginEmail.error = null
        return true
    }

    private fun validatePassword(): Boolean {
        val valPass = loginPassword.text.toString().trim()
        if (valPass.isEmpty()) {
            loginPassword.error = "Password cannot be empty"
            return false
        }
        loginPassword.error = null
        return true
    }

    private fun showForgotPasswordDialog() {
        val input = EditText(this).apply {
            hint = "Enter your email"
            setText(loginEmail.text?.toString()?.trim().orEmpty())
        }

        AlertDialog.Builder(this)
            .setTitle("Reset Password")
            .setMessage("We'll send a password reset link to your email.")
            .setView(input)
            .setPositiveButton("Send") { dialog, _ ->
                val email = input.text?.toString()?.trim().orEmpty()

                if (email.isEmpty()) {
                    Toast.makeText(this, "Email is required.", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(this, "Invalid email format.", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                FirebaseAuth.getInstance()
                    .sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                this,
                                "Reset link sent. Check your email (and spam).",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                this,
                                "Failed: ${task.exception?.message ?: "Unknown error"}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun signIn() {
        val email = loginEmail.text.toString().trim()
        val password = loginPassword.text.toString().trim()

        setAuthLoading(true)

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    setAuthLoading(false)
                    loginPassword.error = task.exception?.message ?: "Login failed"
                    loginPassword.requestFocus()
                    return@addOnCompleteListener
                }

                val user = mAuth.currentUser
                if (user == null) {
                    setAuthLoading(false)
                    Toast.makeText(this, "Auth error.", Toast.LENGTH_SHORT).show()
                    return@addOnCompleteListener
                }

                if (!user.isEmailVerified) {
                    setAuthLoading(false)
                    Toast.makeText(this, "Please verify your email first.", Toast.LENGTH_LONG)
                        .show()
                    mAuth.signOut()
                    return@addOnCompleteListener
                }

                tokenThenBootstrap(user, showTokenDialog = true)
            }
    }

    private fun startGoogleSignIn() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id))
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        setAuthLoading(true)

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    context = this@LoginActivity,
                    request = request
                )

                val credential = result.credential
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val googleIdToken = googleIdTokenCredential.idToken

                firebaseAuthWithGoogle(googleIdToken)

            } catch (e: GoogleIdTokenParsingException) {
                setAuthLoading(false)
                Toast.makeText(
                    this@LoginActivity,
                    "Invalid Google credential.",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                setAuthLoading(false)
                Toast.makeText(
                    this@LoginActivity,
                    "Google sign in failed: ${e.message ?: "Unknown error"}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

        mAuth.signInWithCredential(firebaseCredential)
            .addOnCompleteListener(this) { task ->
                if (!task.isSuccessful) {
                    setAuthLoading(false)
                    Toast.makeText(
                        this,
                        "Firebase Google auth failed: ${task.exception?.message ?: "Unknown error"}",
                        Toast.LENGTH_LONG
                    ).show()
                    return@addOnCompleteListener
                }

                val user = mAuth.currentUser
                if (user == null) {
                    setAuthLoading(false)
                    Toast.makeText(this, "Google user not found.", Toast.LENGTH_LONG).show()
                    return@addOnCompleteListener
                }

                UserPrefs.putString(this, UserPrefs.KEY_USER_EMAIL, user.email ?: "")

                val existingName = UserPrefs.getString(this, UserPrefs.KEY_NAME, "")
                val firebaseName = user.displayName ?: ""
                UserPrefs.putString(
                    this,
                    UserPrefs.KEY_NAME,
                    if (firebaseName.isNotBlank()) firebaseName else existingName
                )

                tokenThenBootstrap(user, showTokenDialog = false)
            }
    }

    private fun tokenThenBootstrap(user: FirebaseUser, showTokenDialog: Boolean) {
        user.getIdToken(true).addOnCompleteListener { idTask ->
            val firebaseToken = idTask.result?.token

            if (!idTask.isSuccessful || firebaseToken.isNullOrBlank()) {
                setAuthLoading(false)
                Toast.makeText(
                    this,
                    "Token error: ${idTask.exception?.message ?: "Unknown error"}",
                    Toast.LENGTH_LONG
                ).show()
                return@addOnCompleteListener
            }

            if (showTokenDialog) {
                showTokenDialog(firebaseToken)
            }

            UserPrefs.putString(this, UserPrefs.KEY_USER_EMAIL, user.email ?: "")

            val existingName = UserPrefs.getString(this, UserPrefs.KEY_NAME, "")
            val firebaseName = user.displayName ?: ""
            UserPrefs.putString(
                this,
                UserPrefs.KEY_NAME,
                if (firebaseName.isNotBlank()) firebaseName else existingName
            )

            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { fcmToken ->
                    loginToBackendAndBootstrap(firebaseToken, fcmToken)
                }
                .addOnFailureListener {
                    loginToBackendAndBootstrap(firebaseToken, null)
                }
        }
    }

    private fun loginToBackendAndBootstrap(firebaseToken: String, fcmToken: String?) {
        lifecycleScope.launch {
            try {
                val api = ApiClient.get(this@LoginActivity)
                    .create(ApiService::class.java)

                val req = LoginRequest(
                    firebaseIdToken = firebaseToken,
                    fcmToken = fcmToken
                )

                val res = api.login(req)

                if (!res.isSuccessful || res.body() == null) {
                    setAuthLoading(false)
                    Toast.makeText(
                        this@LoginActivity,
                        "Backend login failed: ${res.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }

                val auth = res.body()!!

                UserPrefs.saveAuth(
                    this@LoginActivity,
                    auth.token,
                    auth.userId,
                    auth.role,
                    auth.status,
                    auth.isVerified
                )

                val bootRes = api.bootstrap()

                if (!bootRes.isSuccessful || bootRes.body() == null) {
                    setAuthLoading(false)
                    Toast.makeText(
                        this@LoginActivity,
                        "Bootstrap failed: ${bootRes.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }

                val body = bootRes.body()!!

                if (body.userId != null) {
                    UserPrefs.putInt(this@LoginActivity, UserPrefs.KEY_USER_ID, body.userId)
                }

                if (body.profileComplete) {
                    goToMain()
                } else {
                    goToOnboard()
                }

            } catch (e: Exception) {
                setAuthLoading(false)
                Toast.makeText(
                    this@LoginActivity,
                    e.message ?: "Unknown error",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showTokenDialog(token: String) {
        val preview = token.take(40) + "..."

        AlertDialog.Builder(this)
            .setTitle("Firebase ID Token")
            .setMessage("Preview:\n$preview\n\nFull token can be copied.")
            .setPositiveButton("Copy Token") { _, _ ->
                val clipboard =
                    getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Firebase ID Token", token))
                Toast.makeText(this, "Token copied.", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("Show Full") { _, _ ->
                AlertDialog.Builder(this)
                    .setTitle("Full Token")
                    .setMessage(token)
                    .setPositiveButton("OK", null)
                    .show()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun setAuthLoading(loading: Boolean) {
        loginButton.isEnabled = !loading
        btnGoogleSignIn.isEnabled = !loading
        signupRedirectText.isEnabled = !loading
        tvForgotPassword.isEnabled = !loading

        loginButton.text = if (loading) "Loading..." else "Login"
        btnGoogleSignIn.text = if (loading) "Please wait..." else "Continue with Google"
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    private fun goToOnboard() {
        val intent = Intent(this, OnboardingActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menuInflater.inflate(R.menu.menu_theme_switcher, menu)
        val item = menu.findItem(R.id.action_toggle_theme)
        val isDark = sharedPreferences.getBoolean(KEY_DARK_MODE, false)
        item.icon = ContextCompat.getDrawable(
            this,
            if (isDark) R.drawable.ic_sun else R.drawable.ic_moon
        )
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        if (item.itemId == R.id.action_toggle_theme) {
            val isDark = sharedPreferences.getBoolean(KEY_DARK_MODE, false)
            val newDark = !isDark
            sharedPreferences.edit().putBoolean(KEY_DARK_MODE, newDark).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (newDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
            recreate()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}