package com.example.flexifitapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
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
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import android.util.Patterns
import java.security.MessageDigest
import com.example.flexifitapp.utils.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

        Toast.makeText(this, "LoginActivity STARTED!", Toast.LENGTH_SHORT).show()  // 👈 Add this

        sharedPreferences = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        applyThemeFromPrefs()

//        // Auto-login if we have a saved token
//        if (UserPrefs.isLoggedIn(this)) {
//            AppLogger.d("LoginActivity", "Auto-login with existing token")
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

        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)
        tvForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }
    }

    private fun showForgotPasswordDialog() {
        val emailEditText = EditText(this)
        emailEditText.hint = "Enter your email"
        emailEditText.inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

        AlertDialog.Builder(this)
            .setTitle("Reset Password")
            .setMessage("We'll send a password reset link to your email.")
            .setView(emailEditText)
            .setPositiveButton("Send") { _, _ ->
                val email = emailEditText.text.toString().trim()
                if (email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    sendPasswordResetEmail(email)
                } else {
                    Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun sendPasswordResetEmail(email: String) {
        mAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Password reset email sent. Check your inbox.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
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
            AppLogger.d("GOOGLE_DEBUG", "📥 onActivityResult called (fallback)")

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                AppLogger.d("GOOGLE_DEBUG", "✅ Old API succeeded, ID token received: ${idToken?.take(20)}...")

                if (idToken != null) {
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    mAuth.signInWithCredential(firebaseCredential).addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {
                            AppLogger.d("GOOGLE_DEBUG", "✅ Firebase Auth successful!")
                            val user = mAuth.currentUser
                            if (user != null) {
                                fetchFcmAndConnectToBackend(user)
                            } else {
                                setAuthLoading(false)
                                Toast.makeText(this, "User is null", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            AppLogger.e("GOOGLE_DEBUG", "❌ Firebase Auth failed: ${authTask.exception?.message}")
                            setAuthLoading(false)
                            Toast.makeText(this, "Firebase Auth failed: ${authTask.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    AppLogger.e("GOOGLE_DEBUG", "❌ ID token is null")
                    setAuthLoading(false)
                    Toast.makeText(this, "ID token is null", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                AppLogger.e("GOOGLE_DEBUG", "❌ Google Sign-In failed: ${e.message}", e)
                setAuthLoading(false)
                Toast.makeText(this, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
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
                val hex = StringBuilder()
                for (b in md.digest()) hex.append(String.format("%02X", b))

                // ✅ ADD THESE LOGS BACK
                AppLogger.d("SHA1", "SHA-1 (Base64): $sha1")
                AppLogger.d("SHA1", "SHA-1 (Hex): $hex")
            }
        } catch (e: Exception) {
            AppLogger.e("SHA1", "Failed to get SHA-1", e)
        }
    }

    private fun checkExistingLogin() {
        AppLogger.d("AUTO_LOGIN", "🚀 checkExistingLogin STARTED")

        // ✅ Check if secure storage is available
        if (!SecurePrefs.isSecureStorageAvailable()) {
            AppLogger.w("AUTO_LOGIN", "Secure storage not available. Auto-login disabled.")
            loadingOverlay.visibility = View.GONE
            setAuthLoading(false)
            Toast.makeText(this, "Secure storage unavailable. Please login manually.", Toast.LENGTH_LONG).show()
            return
        }
        AppLogger.d("AUTO_LOGIN", "✅ Secure storage available")

        val currentUser = mAuth.currentUser
        AppLogger.d("AUTO_LOGIN", "currentUser = ${currentUser?.uid ?: "null"}")

        var token = UserPrefs.getToken(this)
        AppLogger.d("AUTO_LOGIN", "token exists: ${token.isNotEmpty()}")

        if (currentUser != null && token.isNotEmpty()) {
            AppLogger.d("LoginFlow", "Case 1: user & token exist → validating via bootstrap")
            loadingOverlay.visibility = View.VISIBLE
            setAuthLoading(true)

            lifecycleScope.launch {
                try {
                    AppLogger.d("BOOTSTRAP_DEBUG", "🔄 Creating API client...")
                    val api = ApiClient.get().create(ApiService::class.java)
                    AppLogger.d("BOOTSTRAP_DEBUG", "🔄 API client created")

                    AppLogger.d("BOOTSTRAP_DEBUG", "🔄 Calling bootstrap API...")
                    val bootRes = api.bootstrap()
                    AppLogger.d("BOOTSTRAP_DEBUG", "🔄 Bootstrap response received: ${bootRes.code()}")

                    if (bootRes.isSuccessful && bootRes.body() != null) {
                        val body = bootRes.body()!!
                        AppLogger.d("BOOTSTRAP_DEBUG", "✅ Bootstrap body: profileComplete=${body.profileComplete}, userId=${body.userId}")

                        // Sync user ID if not already set
                        if (UserPrefs.getUserId(this@LoginActivity) == 0 && body.userId != null) {
                            UserPrefs.putInt(this@LoginActivity, UserPrefs.KEY_USER_ID, body.userId)
                        }
                        // Redirect based on profile completeness
                        if (body.profileComplete) {
                            AppLogger.d("LoginFlow", "profileComplete=true → goToMain()")
                            goToMain()
                        } else {
                            AppLogger.d("LoginFlow", "profileComplete=false → goToOnboard()")
                            goToOnboard()
                        }
                    } else {
                        AppLogger.e("AUTO_LOGIN", "bootstrap failed: ${bootRes.code()}")
                        loadingOverlay.visibility = View.GONE
                        setAuthLoading(false)
                        if (bootRes.code() == 401) {
                            mAuth.signOut()
                        }
                    }
                } catch (e: Exception) {
                    AppLogger.e("AUTO_LOGIN", "❌ bootstrap exception: ${e.message}", e)
                    loadingOverlay.visibility = View.GONE
                    setAuthLoading(false)
                    Toast.makeText(this@LoginActivity, "Auto-login failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        } else if (currentUser != null && token.isEmpty()) {
            AppLogger.d("LoginFlow", "Case 2: user exists but token empty → fetch fresh token")
            loadingOverlay.visibility = View.VISIBLE
            setAuthLoading(true)
            currentUser.getIdToken(true).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val newToken = task.result?.token
                    if (newToken != null) {
                        AppLogger.d("LoginFlow", "Got fresh token, saving and retrying")
                        UserPrefs.putString(this, UserPrefs.KEY_JWT_TOKEN, newToken)
                        checkExistingLogin()
                    } else {
                        AppLogger.w("LoginFlow", "Fresh token null")
                        loadingOverlay.visibility = View.GONE
                        setAuthLoading(false)
                        Toast.makeText(this, "Unable to restore session", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    AppLogger.e("LoginFlow", "getIdToken failed", task.exception)
                    loadingOverlay.visibility = View.GONE
                    setAuthLoading(false)
                    Toast.makeText(this, "Unable to restore session", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            AppLogger.d("LoginFlow", "Case 3: no session → show login UI")
            loadingOverlay.visibility = View.GONE
            setAuthLoading(false)
        }
    }

    private fun startGoogleSignIn() {
        val webClientId = getString(R.string.default_web_client_id)
        AppLogger.d("GOOGLE_DEBUG", "🔑 Web client ID: $webClientId")
        AppLogger.d("GOOGLE_DEBUG", "📦 Package name: $packageName")
        logAppSha1()

        try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(false)
                .build()
            AppLogger.d("GOOGLE_DEBUG", "✅ Google ID option built")

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            AppLogger.d("GOOGLE_DEBUG", "✅ Credential request built")

            setAuthLoading(true)
            AppLogger.d("GOOGLE_DEBUG", "⏳ Starting credential manager...")

            lifecycleScope.launch {
                try {
                    AppLogger.d("GOOGLE_DEBUG", "⏳ Calling credentialManager.getCredential()...")
                    val result = credentialManager.getCredential(this@LoginActivity, request)
                    AppLogger.d("GOOGLE_DEBUG", "✅ Credential result received!")

                    val credential = result.credential
                    AppLogger.d("GOOGLE_DEBUG", "✅ Credential type: ${credential.type}")
                    AppLogger.d("GOOGLE_DEBUG", "✅ Credential data: ${credential.data}")

                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    AppLogger.d("GOOGLE_DEBUG", "✅ Google ID token parsed!")

                    val idToken = googleIdTokenCredential.idToken
                    AppLogger.d("GOOGLE_DEBUG", "✅ ID token received (length: ${idToken.length})")

                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    AppLogger.d("GOOGLE_DEBUG", "✅ Firebase credential created!")

                    mAuth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                AppLogger.d("GOOGLE_DEBUG", "✅ Firebase Auth successful!")
                                val user = mAuth.currentUser
                                if (user != null) {
                                    AppLogger.d("GOOGLE_DEBUG", "✅ User: ${user.email}, UID: ${user.uid}")
                                    fetchFcmAndConnectToBackend(user)
                                } else {
                                    AppLogger.e("GOOGLE_DEBUG", "❌ User is null!")
                                    setAuthLoading(false)
                                    Toast.makeText(this@LoginActivity, "User is null", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                AppLogger.e("GOOGLE_DEBUG", "❌ Firebase Auth failed: ${task.exception?.message}")
                                setAuthLoading(false)
                                Toast.makeText(this@LoginActivity, "Firebase Auth Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                } catch (e: Exception) {
                    // ✅ Check if it's the "No credentials" error
                    if (e.message?.contains("No credentials") == true) {
                        AppLogger.e("GOOGLE_DEBUG", "⚠️ Credential Manager failed, using fallback")
                        // ✅ Switch to main thread to start the fallback
                        withContext(Dispatchers.Main) {
                            startGoogleSignInFallback()
                        }
                    } else {
                        // ✅ Other errors - show Toast and stop loading
                        AppLogger.e("GOOGLE_DEBUG", "❌ EXCEPTION in Google Sign-In: ${e.message}", e)
                        setAuthLoading(false)
                        Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } catch (e: Exception) {
            // ✅ If setup fails, use fallback directly
            AppLogger.e("GOOGLE_DEBUG", "❌ Setup failed: ${e.message}", e)
            startGoogleSignInFallback()
        }
    }

    private fun startGoogleSignInFallback() {
        AppLogger.d("GOOGLE_DEBUG", "🚀 Using OLD GoogleSignInClient API (fallback)")

        // ✅ Show loading indicator
        setAuthLoading(true)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        startActivityForResult(googleSignInClient.signInIntent, RC_GOOGLE_SIGN_IN)
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
                    handleSuccessfulAuth(res.body()!!, api, firebaseToken)
                } else if (res.code() == 401) {
                    autoRegisterUser(firebaseToken, fcmToken, api)
                } else {
                    setAuthLoading(false)
                    Toast.makeText(this@LoginActivity, "Login failed: ${res.code()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                setAuthLoading(false)
                AppLogger.e("LOGIN_ERROR", e.message ?: "Unknown error")
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
                    handleSuccessfulAuth(res.body()!!, api, firebaseToken)
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
    private suspend fun handleSuccessfulAuth(
        auth: AuthResponse,
        api: ApiService,
        firebaseToken: String?
    ) {

        AppLogger.d("LoginActivity", "handleSuccessfulAuth: firebaseToken received")

        UserPrefs.saveAuth(
            ctx = this@LoginActivity,
            token = auth.token,
            userId = auth.userId,
            role = auth.role,
            status = auth.status,
            isVerified = auth.isVerified,
            name = auth.name ?: "",      // Idagdag mo ito 
            photoUrl = auth.photoUrl ?: "", // At ito rin
            firebaseToken = firebaseToken
        )
        AppLogger.d("AUTH", "User logged in successfully. UserId: ${UserPrefs.getUserId(this)}")

        // Optional: I-save na rin natin yung Name at Photo para sa ProfileFragment
        UserPrefs.putString(this@LoginActivity, UserPrefs.KEY_NAME, auth.name ?: "")
        UserPrefs.putString(this@LoginActivity, "avatar_url", auth.photoUrl ?: "")

        val bootRes = api.bootstrap()
        if (bootRes.isSuccessful && bootRes.body() != null) {
            val body = bootRes.body()!!
            AppLogger.d(
                "BOOTSTRAP_DEBUG",
                "handleSuccessfulAuth bootstrap: profileComplete=${body.profileComplete}, status=${body.status}, userId=${body.userId}, name=${body.name}, username=${body.username}"
            )

            // Sync the user ID to local prefs
            if (auth.userId != 0) {
                UserPrefs.putInt(this@LoginActivity, UserPrefs.KEY_USER_ID, auth.userId)
            }

            // Determine where to go
            if (body.profileComplete) {
                AppLogger.d("LoginFlow", "handleSuccessfulAuth: profileComplete=true → goToMain()")
                goToMain()
            } else {
                AppLogger.d("LoginFlow", "handleSuccessfulAuth: profileComplete=false → goToOnboard()")
                goToOnboard()
            }
        } else {
            // Default to onboarding if bootstrap fails but login succeeded
            AppLogger.d("LoginFlow", "handleSuccessfulAuth: profileComplete=false → goToOnboard()")
            goToOnboard()
        }
    }

    private fun setAuthLoading(isLoading: Boolean) {
        loginBtn.isEnabled = !isLoading
        btnGoogleLogin.isEnabled = !isLoading
        loginBtn.text = if (isLoading) "Connecting..." else "Login"
    }

    private fun goToMain() {
        AppLogger.d("LoginFlow", "goToMain() called")
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun goToOnboard() {
        AppLogger.d("LoginFlow", "goToOnboard() called")
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