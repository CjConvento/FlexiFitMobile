package com.example.flexifitapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.lifecycleScope
import com.example.flexifitapp.auth.LoginRequest
import com.example.flexifitapp.googleauth.CreateUsernameActivity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class SignupActivity : AppCompatActivity() {

    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etUsername: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var tilPassword: TextInputLayout
    private lateinit var btnSignup: MaterialButton
    private lateinit var btnGoogleSignup: MaterialButton
    private lateinit var cbTermsSignup: CheckBox
    private lateinit var tvLoginRedirect: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var prefs: SharedPreferences
    private lateinit var credentialManager: CredentialManager

    private val PREF_NAME = "flexifit_prefs"
    private val KEY_DARK_MODE = "dark_mode"

    private val KEY_PENDING_NAME = "pending_name"
    private val KEY_PENDING_USERNAME = "pending_username"
    private val KEY_PENDING_EMAIL = "pending_email"
    private val KEY_PENDING_CREATED_AT = "pending_created_at"

    private var isSubmitting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        applyThemeFromPrefs()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.create(this)

        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        etName = findViewById(R.id.signup_name)
        etEmail = findViewById(R.id.signup_email)
        etUsername = findViewById(R.id.signup_username)
        etPassword = findViewById(R.id.signup_password)
        tilPassword = findViewById(R.id.tilpass)
        btnSignup = findViewById(R.id.signup_button)
        btnGoogleSignup = findViewById(R.id.btnGoogleSignup)
        cbTermsSignup = findViewById(R.id.cbTermsSignup)
        tvLoginRedirect = findViewById(R.id.loginRedirectText)

        intent.getStringExtra("email")?.let { etEmail.setText(it) }

        btnSignup.setOnClickListener {
            if (isSubmitting) return@setOnClickListener

            clearErrors()

            if (!cbTermsSignup.isChecked) {
                Toast.makeText(
                    this,
                    "Please accept the Terms of Service first.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (!validateAll()) return@setOnClickListener
            createAccount()
        }

        btnGoogleSignup.setOnClickListener {
            if (isSubmitting) return@setOnClickListener

            clearErrors()

            if (!cbTermsSignup.isChecked) {
                Toast.makeText(
                    this,
                    "Please accept the Terms of Service first.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            startGoogleSignUp()
        }

        tvLoginRedirect.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun createAccount() {
        val name = safeText(etName)
        val email = safeText(etEmail)
        val username = safeText(etUsername)
        val password = safeText(etPassword)

        setLoading(true)
        savePendingSignup(name, username, email)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    setLoading(false)
                    handleSignupError(task.exception)
                    return@addOnCompleteListener
                }

                val user = auth.currentUser
                if (user == null) {
                    setLoading(false)
                    Toast.makeText(this, "Signup failed. Please try again.", Toast.LENGTH_LONG)
                        .show()
                    return@addOnCompleteListener
                }

                sendVerificationAndRedirect(user, email)
            }
    }

    private fun sendVerificationAndRedirect(user: FirebaseUser, email: String) {
        user.sendEmailVerification()
            .addOnCompleteListener { verifyTask ->
                // Check if the activity is still alive
                if (isFinishing || isDestroyed) return@addOnCompleteListener

                setLoading(false)

                if (!verifyTask.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Failed to send verification email: ${verifyTask.exception?.message ?: "Unknown error"}",
                        Toast.LENGTH_LONG
                    ).show()
                    return@addOnCompleteListener
                }

                auth.signOut()

                Toast.makeText(
                    this,
                    "Account created! Please verify via the link sent to your email.",
                    Toast.LENGTH_LONG
                ).show()

                val intent = Intent(this, EmailVerificationActivity::class.java).apply {
                    putExtra("email", email)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                startActivity(intent)
                finish()
            }
    }

    private fun startGoogleSignUp() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id))
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        setLoading(true)

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    context = this@SignupActivity,
                    request = request
                )

                val credential = result.credential
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val googleIdToken = googleIdTokenCredential.idToken

                firebaseAuthWithGoogle(googleIdToken)

            } catch (e: GoogleIdTokenParsingException) {
                setLoading(false)
                Toast.makeText(
                    this@SignupActivity,
                    "Invalid Google credential.",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                setLoading(false)
                Toast.makeText(
                    this@SignupActivity,
                    "Google sign up failed: ${e.message ?: "Unknown error"}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(firebaseCredential)
            .addOnCompleteListener(this) { task ->
                if (!task.isSuccessful) {
                    setLoading(false)
                    Toast.makeText(
                        this,
                        "Firebase Google auth failed: ${task.exception?.message ?: "Unknown error"}",
                        Toast.LENGTH_LONG
                    ).show()
                    return@addOnCompleteListener
                }

                val user = auth.currentUser
                if (user == null) {
                    setLoading(false)
                    Toast.makeText(this, "Google user not found.", Toast.LENGTH_LONG).show()
                    return@addOnCompleteListener
                }

                checkIfGoogleUserExists(user)
            }
    }

    private fun checkIfGoogleUserExists(user: FirebaseUser) {
        user.getIdToken(true).addOnCompleteListener { idTask ->
            val firebaseToken = idTask.result?.token

            if (!idTask.isSuccessful || firebaseToken.isNullOrBlank()) {
                setLoading(false)
                Toast.makeText(
                    this,
                    "Token error: ${idTask.exception?.message ?: "Unknown error"}",
                    Toast.LENGTH_LONG
                ).show()
                return@addOnCompleteListener
            }

            lifecycleScope.launch {
                try {
                    val api = ApiClient.get(this@SignupActivity)
                        .create(ApiService::class.java)

                    val loginReq = LoginRequest(
                        firebaseIdToken = firebaseToken,
                        fcmToken = null
                    )

                    val res = api.login(loginReq)

                    if (res.isSuccessful && res.body() != null) {
                        val authBody = res.body()!!

                        // Sa loob ng checkIfGoogleUserExists...
                        UserPrefs.saveAuth(
                            this@SignupActivity,
                            authBody.token,
                            authBody.userId,
                            authBody.role,
                            authBody.status,
                            authBody.isVerified,
                            authBody.name,    // Ika-anim na parameter
                            authBody.photoUrl // Ika-pitong parameter
                        )
                        val bootRes = api.bootstrap()

                        if (!bootRes.isSuccessful || bootRes.body() == null) {
                            setLoading(false)
                            Toast.makeText(
                                this@SignupActivity,
                                "Bootstrap failed: ${bootRes.code()}",
                                Toast.LENGTH_LONG
                            ).show()
                            return@launch
                        }

// Hanapin mo itong line sa lifecycleScope.launch:
                        val body = bootRes.body()!!

                        if (body.userId != null) {
                            UserPrefs.putInt(this@SignupActivity, UserPrefs.KEY_USER_ID, body.userId)
                        }

// DAPAT MAG-MATCH SA BOOTSTRAPRESPONSE MODEL NATIN
                        if (body.profileComplete) { // <--- Gamitin ang variable name na nasa Model mo
                            goToMain()
                        } else {
                            goToOnboard()
                        }
                    } else {
                        openCreateUsername(user, firebaseToken)
                    }

                } catch (e: Exception) {
                    setLoading(false)
                    Toast.makeText(
                        this@SignupActivity,
                        e.message ?: "Login check failed",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun openCreateUsername(user: FirebaseUser, firebaseToken: String) {
        val intent = Intent(this, CreateUsernameActivity::class.java).apply {
            putExtra("firebaseToken", firebaseToken)
            putExtra("name", user.displayName.orEmpty())
            putExtra("email", user.email.orEmpty())
        }
        startActivity(intent)
        finish()
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

    private fun handleSignupError(ex: Exception?) {
        when (ex) {
            is FirebaseAuthUserCollisionException -> {
                etEmail.error = "This email is already registered."
                Toast.makeText(this, "Email already in use. Try logging in.", Toast.LENGTH_LONG)
                    .show()
            }

            else -> {
                Toast.makeText(
                    this,
                    "Sign up failed: ${ex?.message ?: "Unknown error"}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun savePendingSignup(name: String, username: String, email: String) {
        prefs.edit()
            .putString(KEY_PENDING_NAME, name)
            .putString(KEY_PENDING_USERNAME, username)
            .putString(KEY_PENDING_EMAIL, email)
            .putLong(KEY_PENDING_CREATED_AT, System.currentTimeMillis())
            .apply()
    }

    private fun validateAll(): Boolean {
        val okName = validateName()
        val okEmail = validateEmail()
        val okUser = validateUsername()
        val okPass = validatePassword()
        return okName && okEmail && okUser && okPass
    }

    private fun validateName(): Boolean {
        val v = safeText(etName)
        return when {
            v.isEmpty() -> {
                etName.error = "Name cannot be empty"
                false
            }

            v.length < 2 -> {
                etName.error = "Name is too short"
                false
            }

            else -> true
        }
    }

    private fun validateEmail(): Boolean {
        val v = safeText(etEmail)
        return when {
            v.isEmpty() -> {
                etEmail.error = "Email cannot be empty"
                false
            }

            !Patterns.EMAIL_ADDRESS.matcher(v).matches() -> {
                etEmail.error = "Invalid email format"
                false
            }

            else -> true
        }
    }

    private fun validateUsername(): Boolean {
        val v = safeText(etUsername)
        return when {
            v.isEmpty() -> {
                etUsername.error = "Username cannot be empty"
                false
            }

            v.contains(" ") -> {
                etUsername.error = "Username cannot have spaces"
                false
            }

            v.length < 2 -> {
                etUsername.error = "Username must be at least 2 characters"
                false
            }

            else -> true
        }
    }

    private fun validatePassword(): Boolean {
        val v = safeText(etPassword)
        return when {
            v.isEmpty() -> {
                tilPassword.error = "Password cannot be empty"
                false
            }

            v.length < 6 -> {
                tilPassword.error = "Password must be at least 6 characters"
                false
            }

            else -> {
                tilPassword.error = null
                true
            }
        }
    }

    private fun clearErrors() {
        etName.error = null
        etEmail.error = null
        etUsername.error = null
        tilPassword.error = null
    }

    private fun safeText(et: TextInputEditText): String {
        return et.text?.toString()?.trim().orEmpty()
    }

    private fun setLoading(loading: Boolean) {
        isSubmitting = loading

        btnSignup.isEnabled = !loading
        btnGoogleSignup.isEnabled = !loading
        cbTermsSignup.isEnabled = !loading
        tvLoginRedirect.isEnabled = !loading

        btnSignup.text = if (loading) "Creating..." else "Sign Up"
        btnGoogleSignup.text = if (loading) "Please wait..." else "Sign up with Google"

        setEnabled(etName, !loading)
        setEnabled(etEmail, !loading)
        setEnabled(etUsername, !loading)
        setEnabled(etPassword, !loading)

        val overlay = findViewById<View?>(R.id.loadingOverlay)
        overlay?.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun setEnabled(view: View, enabled: Boolean) {
        view.isEnabled = enabled
        view.isClickable = enabled
        view.isFocusable = enabled
        view.isFocusableInTouchMode = enabled
    }

    private fun applyThemeFromPrefs() {
        val isDark = prefs.getBoolean(KEY_DARK_MODE, false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_theme_switcher, menu)
        val item = menu.findItem(R.id.action_toggle_theme)
        val isDark = prefs.getBoolean(KEY_DARK_MODE, false)
        item.icon = ContextCompat.getDrawable(
            this,
            if (isDark) R.drawable.ic_sun else R.drawable.ic_moon
        )
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_toggle_theme) {
            val current = prefs.getBoolean(KEY_DARK_MODE, false)
            val newMode = !current
            prefs.edit().putBoolean(KEY_DARK_MODE, newMode).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (newMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
            recreate()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}