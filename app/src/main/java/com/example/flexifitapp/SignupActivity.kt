package com.example.flexifitapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser

class SignupActivity : AppCompatActivity() {

    // Views
    private lateinit var etName: TextInputEditText
    private lateinit var etAddress: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etUsername: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var tilPassword: TextInputLayout
    private lateinit var btnSignup: MaterialButton
    private lateinit var tvLoginRedirect: TextView

    // Firebase + prefs
    private lateinit var auth: FirebaseAuth
    private lateinit var prefs: SharedPreferences

    // Pref keys
    private val PREF_NAME = "flexifit_prefs"
    private val KEY_DARK_MODE = "dark_mode"

    // Pending signup data (for later bootstrap to backend, optional)
    private val KEY_PENDING_NAME = "pending_name"
    private val KEY_PENDING_ADDRESS = "pending_address"
    private val KEY_PENDING_USERNAME = "pending_username"
    private val KEY_PENDING_EMAIL = "pending_email"
    private val KEY_PENDING_CREATED_AT = "pending_created_at"

    private var isSubmitting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // Theme first
        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        applyThemeFromPrefs()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()

        // Toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        // Bind views
        etName = findViewById(R.id.signup_name)
        etAddress = findViewById(R.id.signup_address)
        etEmail = findViewById(R.id.signup_email)
        etUsername = findViewById(R.id.signup_username)
        etPassword = findViewById(R.id.signup_password)
        tilPassword = findViewById(R.id.tilpass)
        btnSignup = findViewById(R.id.signup_button)
        tvLoginRedirect = findViewById(R.id.loginRedirectText)

        // Prefill email if passed from other screen
        intent.getStringExtra("email")?.let { etEmail.setText(it) }

        btnSignup.setOnClickListener {
            if (isSubmitting) return@setOnClickListener
            clearErrors()
            if (!validateAll()) return@setOnClickListener
            createAccount()
        }

        tvLoginRedirect.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    // -------------------- Signup flow --------------------

    private fun createAccount() {
        val name = safeText(etName)
        val address = safeText(etAddress)
        val email = safeText(etEmail)
        val username = safeText(etUsername)
        val password = safeText(etPassword)

        setLoading(true)

        // Save pending data locally (optional)
        savePendingSignup(name, address, username, email)

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
                    Toast.makeText(this, "Signup failed. Please try again.", Toast.LENGTH_LONG).show()
                    return@addOnCompleteListener
                }

                sendVerificationAndRedirect(user, email)
            }
    }

    private fun sendVerificationAndRedirect(user: FirebaseUser, email: String) {
        user.sendEmailVerification()
            .addOnCompleteListener { verifyTask ->
                setLoading(false)

                if (!verifyTask.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Failed to send verification email: ${verifyTask.exception?.message ?: "Unknown error"}",
                        Toast.LENGTH_LONG
                    ).show()
                    return@addOnCompleteListener
                }

                // IMPORTANT: prevent unverified auto-login
                auth.signOut()

                Toast.makeText(
                    this,
                    "Account created! Please verify via the link sent to your email.",
                    Toast.LENGTH_LONG
                ).show()

                // Go to verification screen
                val intent = Intent(this, EmailVerificationActivity::class.java).apply {
                    putExtra("email", email)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                startActivity(intent)
                finish()
            }
    }

    private fun handleSignupError(ex: Exception?) {
        when (ex) {
            is FirebaseAuthUserCollisionException -> {
                // Email already used
                etEmail.error = "This email is already registered."
                Toast.makeText(this, "Email already in use. Try logging in.", Toast.LENGTH_LONG).show()
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

    private fun savePendingSignup(name: String, address: String, username: String, email: String) {
        prefs.edit()
            .putString(KEY_PENDING_NAME, name)
            .putString(KEY_PENDING_ADDRESS, address)
            .putString(KEY_PENDING_USERNAME, username)
            .putString(KEY_PENDING_EMAIL, email)
            .putLong(KEY_PENDING_CREATED_AT, System.currentTimeMillis())
            .apply()
    }

    // -------------------- Validation --------------------

    private fun validateAll(): Boolean {
        val okName = validateName()
        val okAddr = validateAddress()
        val okEmail = validateEmail()
        val okUser = validateUsername()
        val okPass = validatePassword()
        return okName && okAddr && okEmail && okUser && okPass
    }

    private fun validateName(): Boolean {
        val v = safeText(etName)
        return when {
            v.isEmpty() -> { etName.error = "Name cannot be empty"; false }
            v.length < 2 -> { etName.error = "Name is too short"; false }
            else -> true
        }
    }

    private fun validateAddress(): Boolean {
        val v = safeText(etAddress)
        return when {
            v.isEmpty() -> { etAddress.error = "Address cannot be empty"; false }
            v.length < 4 -> { etAddress.error = "Address is too short"; false }
            else -> true
        }
    }

    private fun validateEmail(): Boolean {
        val v = safeText(etEmail)
        return when {
            v.isEmpty() -> { etEmail.error = "Email cannot be empty"; false }
            !Patterns.EMAIL_ADDRESS.matcher(v).matches() -> { etEmail.error = "Invalid email format"; false }
            else -> true
        }
    }

    private fun validateUsername(): Boolean {
        val v = safeText(etUsername)
        return when {
            v.isEmpty() -> { etUsername.error = "Username cannot be empty"; false }
            v.contains(" ") -> { etUsername.error = "Username cannot have spaces"; false }
            v.length < 2 -> { etUsername.error = "Username must be at least 2 characters"; false }
            else -> true
        }
    }

    private fun validatePassword(): Boolean {
        val v = safeText(etPassword)
        return when {
            v.isEmpty() -> { tilPassword.error = "Password cannot be empty"; false }
            v.length < 6 -> { tilPassword.error = "Password must be at least 6 characters"; false }
            else -> { tilPassword.error = null; true }
        }
    }

    private fun clearErrors() {
        etName.error = null
        etAddress.error = null
        etEmail.error = null
        etUsername.error = null
        tilPassword.error = null
    }

    private fun safeText(et: TextInputEditText): String {
        return et.text?.toString()?.trim().orEmpty()
    }

    // -------------------- Loading / UX --------------------

    private fun setLoading(loading: Boolean) {
        isSubmitting = loading
        btnSignup.isEnabled = !loading
        btnSignup.text = if (loading) "Creating..." else "Sign Up"

        // Optional: disable all inputs while submitting
        setEnabled(etName, !loading)
        setEnabled(etAddress, !loading)
        setEnabled(etEmail, !loading)
        setEnabled(etUsername, !loading)
        setEnabled(etPassword, !loading)

        // Optional: if you have progress bar id "progressBar"
        val pb = findViewById<View?>(R.id.loadingOverlay)
        pb?.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun setEnabled(view: View, enabled: Boolean) {
        view.isEnabled = enabled
        view.isClickable = enabled
        view.isFocusable = enabled
        view.isFocusableInTouchMode = enabled
    }

    // -------------------- Theme menu --------------------

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
        item.icon = ContextCompat.getDrawable(this, if (isDark) R.drawable.ic_sun else R.drawable.ic_moon)
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

