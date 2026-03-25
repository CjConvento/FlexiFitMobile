package com.example.flexifitapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.lifecycleScope
import com.example.flexifitapp.auth.AuthResponse
import com.example.flexifitapp.auth.LoginRequest
import com.example.flexifitapp.auth.RegisterRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import java.security.MessageDigest

class LoginActivity : AppCompatActivity() {

    private lateinit var loginEmail: EditText
    private lateinit var loginPass: EditText
    private lateinit var loginBtn: MaterialButton
    private lateinit var btnGoogleLogin: MaterialButton
    private lateinit var signupRedirect: TextView
    private lateinit var loadingOverlay: View
    private lateinit var mAuth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var credentialManager: CredentialManager

    private val KEY_DARK_MODE = "dark_mode"
    private val RC_GOOGLE_SIGN_IN = 1001  // for old API

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        applyThemeFromPrefs()

//        // Auto-login if we have a saved token
//        if (UserPrefs.isLoggedIn(this)) {
//            Log.d("LoginActivity", "Auto-login with existing token")
//            startActivity(Intent(this, MainActivity::class.java))
//            finish()
//            return
//        }

        setContentView(R.layout.activity_login)

        // Initialize components
        mAuth = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.create(this)

        loginEmail = findViewById(R.id.login_email)
        loginPass = findViewById(R.id.login_password)
        loginBtn = findViewById(R.id.login_button)
        btnGoogleLogin = findViewById(R.id.btnGoogleSignIn)
        signupRedirect = findViewById(R.id.signupRedirectText)
        loadingOverlay = findViewById(R.id.loadingOverlay)

        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)

        checkExistingLogin()

        // Email/Password Login
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

        // Google Login Listener – temporarily use the old API for testing
        btnGoogleLogin.setOnClickListener {
            startGoogleSignIn()
        }

        signupRedirect.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun testGoogleSignInOld() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        startActivityForResult(googleSignInClient.signInIntent, RC_GOOGLE_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                Log.d("GOOGLE_DEBUG", "Old API succeeded, ID token: ${idToken?.take(20)}")
                // Sign in to Firebase with the token
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                mAuth.signInWithCredential(firebaseCredential).addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        val user = mAuth.currentUser
                        if (user != null) fetchFcmAndConnectToBackend(user)
                    } else {
                        setAuthLoading(false)
                        Toast.makeText(this, "Firebase Auth failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: ApiException) {
                Log.e("GOOGLE_DEBUG", "Old API failed", e)
                setAuthLoading(false)
                Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun logAppSha1() {
        try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures!!) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val sha1 = android.util.Base64.encodeToString(md.digest(), android.util.Base64.NO_WRAP)
                Log.d("SHA1", "SHA-1 (Base64): $sha1")
                val hex = StringBuilder()
                for (b in md.digest()) hex.append(String.format("%02X", b))
                Log.d("SHA1", "SHA-1 (Hex): $hex")
            }
        } catch (e: Exception) {
            Log.e("SHA1", "Failed to get SHA-1", e)
        }
    }

    private fun checkExistingLogin() {
        val currentUser = mAuth.currentUser
        var token = UserPrefs.getToken(this)

        Log.d("AUTO_LOGIN", "currentUser=${currentUser?.uid}, token=${token.take(20)}...")

        if (currentUser != null && token.isNotEmpty()) {
            Log.d("LoginFlow", "Case 1: user & token exist → validating via bootstrap")
            // Already have a token – validate via bootstrap
            loadingOverlay.visibility = View.VISIBLE
            setAuthLoading(true)

            lifecycleScope.launch {
                try {
                    val api = ApiClient.get().create(ApiService::class.java)
                    val bootRes = api.bootstrap()
                    if (bootRes.isSuccessful && bootRes.body() != null) {
                        val body = bootRes.body()!!

                        // 🔥 ADD THIS LOGGING 🔥
                        Log.d("BOOTSTRAP_DEBUG", "LoginActivity bootstrap: profileComplete=${body.profileComplete}, " +
                                "status=${body.status}, userId=${body.userId}, " +
                                "name=${body.name}, username=${body.username}")

                        // Sync user ID if not already set
                        if (UserPrefs.getUserId(this@LoginActivity) == 0 && body.userId != null) {
                            UserPrefs.putInt(this@LoginActivity, UserPrefs.KEY_USER_ID, body.userId)
                        }
                        // Redirect based on profile completeness
                        if (body.profileComplete) {
                            Log.d("LoginFlow", "profileComplete=true → goToMain()")
                            goToMain()
                        } else {
                            Log.d("LoginFlow", "profileComplete=false → goToOnboard()")
                            goToOnboard()
                        }
                    } else {
                        // Bootstrap failed – token may be invalid
                        Log.e("AUTO_LOGIN", "bootstrap failed: ${bootRes.code()}")
                        UserPrefs.clearAuth(this@LoginActivity)
                        loadingOverlay.visibility = View.GONE
                        setAuthLoading(false)
                        // Also sign out Firebase to clean up
                        mAuth.signOut()
                    }
                } catch (e: Exception) {
                    Log.e("AUTO_LOGIN", "bootstrap exception", e)
                    UserPrefs.clearAuth(this@LoginActivity)
                    loadingOverlay.visibility = View.GONE
                    setAuthLoading(false)
                    mAuth.signOut()
                }
            }
        } else if (currentUser != null && token.isEmpty()) {
            // Firebase user exists but no local token – try to get a fresh token
            Log.d("LoginFlow", "Case 2: user exists but token empty → fetch fresh token")
            loadingOverlay.visibility = View.VISIBLE
            setAuthLoading(true)
            currentUser.getIdToken(true).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val newToken = task.result?.token
                    if (newToken != null) {
                        Log.d("LoginFlow", "Got fresh token, saving and retrying")
                        UserPrefs.putString(this, UserPrefs.KEY_JWT_TOKEN, newToken)
                        // Retry the auto-login
                        checkExistingLogin()
                    } else {
                        Log.w("LoginFlow", "Fresh token null")
                        loadingOverlay.visibility = View.GONE
                        setAuthLoading(false)
                        Toast.makeText(this, "Unable to restore session", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("LoginFlow", "getIdToken failed", task.exception)
                    loadingOverlay.visibility = View.GONE
                    setAuthLoading(false)
                    Toast.makeText(this, "Unable to restore session", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Log.d("LoginFlow", "Case 3: no session → show login UI")
            // No session – show login UI
            loadingOverlay.visibility = View.GONE
            setAuthLoading(false)
        }
    }

    private fun startGoogleSignIn() {
        val webClientId = getString(R.string.default_web_client_id)
        Log.d("GOOGLE_DEBUG", "Web client ID: $webClientId")
        Log.d("GOOGLE_DEBUG", "Package name: $packageName")
        logAppSha1()

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
                val result = credentialManager.getCredential(this@LoginActivity, request)
                val credential = result.credential
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                mAuth.signInWithCredential(firebaseCredential).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = mAuth.currentUser
                        if (user != null) {
                            fetchFcmAndConnectToBackend(user)
                        }
                    } else {
                        setAuthLoading(false)
                        Toast.makeText(this@LoginActivity, "Google Auth Failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                setAuthLoading(false)
                Log.e("GOOGLE_ERROR", "Exception: ${e.message}", e)
                Toast.makeText(this@LoginActivity, "Google Auth Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
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
                val api = ApiClient.get().create(ApiService::class.java)
                val req = LoginRequest(firebaseIdToken = firebaseToken, fcmToken = fcmToken)

                val res = api.login(req)

                if (res.isSuccessful && res.body() != null) {
                    handleSuccessfulAuth(res.body()!!, api)
                } else if (res.code() == 401) {
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

        // Gumamit tayo ng Named Arguments para hindi malito ang compiler sa sequence
        val regReq = RegisterRequest(
            firebaseIdToken = firebaseToken,
            name = if (savedName.isNullOrEmpty()) firebaseUser?.displayName ?: "FlexiFit User" else savedName,
            username = savedUsername,
            fcmToken = fcmToken,
            authProvider = "GOOGLE" // Siguraduhing "AuthProvider" ang name sa RegisterRequest.kt mo
        )

        lifecycleScope.launch {
            try {
                val res = api.register(regReq)
                if (res.isSuccessful && res.body() != null) {
                    handleSuccessfulAuth(res.body()!!, api)
                } else {
                    setAuthLoading(false)
                    Toast.makeText(this@LoginActivity, "Registration failed: ${res.code()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                setAuthLoading(false)
                Toast.makeText(this@LoginActivity, "Registration Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private suspend fun handleSuccessfulAuth(auth: AuthResponse, api: ApiService) {
        UserPrefs.saveAuth(
            ctx = this@LoginActivity,
            token = auth.token,
            userId = auth.userId,
            role = auth.role,
            status = auth.status,
            isVerified = auth.isVerified,
            name = auth.name ?: "",      // Idagdag mo ito babe
            photoUrl = auth.photoUrl ?: "" // At ito rin
        )
        Log.d("AUTH", "Saved token: ${UserPrefs.getToken(this@LoginActivity)}")

        // Optional: I-save na rin natin yung Name at Photo para sa ProfileFragment
        UserPrefs.putString(this@LoginActivity, UserPrefs.KEY_NAME, auth.name ?: "")
        UserPrefs.putString(this@LoginActivity, "avatar_url", auth.photoUrl ?: "")

        val bootRes = api.bootstrap()
        if (bootRes.isSuccessful && bootRes.body() != null) {
            val body = bootRes.body()!!
            Log.d("BOOTSTRAP_DEBUG", "handleSuccessfulAuth bootstrap: profileComplete=${body.profileComplete}, status=${body.status}, userId=${body.userId}, name=${body.name}, username=${body.username}")

            // Sync the user ID to local prefs
            if (auth.userId != 0) {
                UserPrefs.putInt(this@LoginActivity, UserPrefs.KEY_USER_ID, auth.userId)
            }

            // Determine where to go
            if (body.profileComplete)
                Log.d("LoginFlow", "handleSuccessfulAuth: profileComplete=true → goToMain()")
            goToMain()
        } else {
            // Default to onboarding if bootstrap fails but login succeeded
            Log.d("LoginFlow", "handleSuccessfulAuth: profileComplete=false → goToOnboard()")
            goToOnboard()
        }
    }

    private fun setAuthLoading(isLoading: Boolean) {
        loginBtn.isEnabled = !isLoading
        btnGoogleLogin.isEnabled = !isLoading
        loginBtn.text = if (isLoading) "Connecting..." else "Login"
    }

    private fun goToMain() {
        Log.d("LoginFlow", "goToMain() called")
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun goToOnboard() {
        Log.d("LoginFlow", "goToOnboard() called")
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