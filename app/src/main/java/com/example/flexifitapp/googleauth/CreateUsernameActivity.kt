package com.example.flexifitapp.googleauth

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.flexifitapp.ApiClient
import com.example.flexifitapp.ApiService
import com.example.flexifitapp.MainActivity
import com.example.flexifitapp.OnboardingActivity
import com.example.flexifitapp.R
import com.example.flexifitapp.auth.RegisterRequest
import com.example.flexifitapp.UserPrefs
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

class CreateUsernameActivity : AppCompatActivity() {

    private lateinit var tvNameValue: TextView
    private lateinit var tvEmailValue: TextView
    private lateinit var etUsername: TextInputEditText
    private lateinit var btnContinue: MaterialButton

    private lateinit var prefs: SharedPreferences

    private val PREF_NAME = "flexifit_prefs"
    private val KEY_DARK_MODE = "dark_mode"

    private var isSubmitting = false

    private var firebaseToken: String = ""
    private var googleName: String = ""
    private var googleEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        applyThemeFromPrefs()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_username)

        firebaseToken = intent.getStringExtra("firebaseToken").orEmpty()
        googleName = intent.getStringExtra("name").orEmpty()
        googleEmail = intent.getStringExtra("email").orEmpty()

        if (firebaseToken.isBlank()) {
            Toast.makeText(this, "Missing Google signup token.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        tvNameValue = findViewById(R.id.tvGoogleNameValue)
        tvEmailValue = findViewById(R.id.tvGoogleEmailValue)
        etUsername = findViewById(R.id.etCreateUsername)
        btnContinue = findViewById(R.id.btnContinueCreateUsername)

        tvNameValue.text = googleName.ifBlank { "—" }
        tvEmailValue.text = googleEmail.ifBlank { "—" }

        btnContinue.setOnClickListener {
            if (isSubmitting) return@setOnClickListener

            clearErrors()

            if (!validateUsername()) return@setOnClickListener

            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { fcmToken ->
                    registerGoogleUser(fcmToken)
                }
                .addOnFailureListener {
                    registerGoogleUser(null)
                }
        }
    }

    private fun registerGoogleUser(fcmToken: String?) {
        val username = safeText(etUsername)
        val finalName = googleName.ifBlank { "Google User" }
        val provider = intent.getStringExtra("authProvider") ?: "GOOGLE"

        setLoading(true)

        lifecycleScope.launch {
            try {
                val api = ApiClient.get(this@CreateUsernameActivity)
                    .create(ApiService::class.java)

                val registerReq = RegisterRequest(
                    firebaseIdToken = firebaseToken,
                    name = finalName,
                    username = username,
                    fcmToken = fcmToken,
                    authProvider = provider
                )

                val registerRes = api.register(registerReq)

                if (!registerRes.isSuccessful || registerRes.body() == null) {
                    setLoading(false)
                    Toast.makeText(
                        this@CreateUsernameActivity,
                        "Register failed: ${registerRes.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }

                val authBody = registerRes.body()!!

                // Heto ang tamang paraan babe, kailangan nating gamitin ang 'authBody.'
                // para makuha ang data mula sa server response.
                UserPrefs.saveAuth(
                    ctx = this@CreateUsernameActivity,
                    token = authBody.token,
                    userId = authBody.userId,
                    role = authBody.role,
                    status = authBody.status,
                    isVerified = authBody.isVerified,
                    name = authBody.name ?: "",
                    photoUrl = authBody.photoUrl ?: ""
                )

                UserPrefs.putString(this@CreateUsernameActivity, UserPrefs.KEY_USER_EMAIL, googleEmail)
                UserPrefs.putString(this@CreateUsernameActivity, UserPrefs.KEY_NAME, finalName)

                val bootRes = api.bootstrap()

                if (!bootRes.isSuccessful || bootRes.body() == null) {
                    setLoading(false)
                    Toast.makeText(
                        this@CreateUsernameActivity,
                        "Bootstrap failed: ${bootRes.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }

                val body = bootRes.body()!!

                if (body.userId != null) {
                    UserPrefs.putInt(this@CreateUsernameActivity, UserPrefs.KEY_USER_ID, body.userId)
                }

                if (body.profileComplete) {
                    goToMain()
                } else {
                    goToOnboard()
                }

            } catch (e: Exception) {
                setLoading(false)
                Toast.makeText(
                    this@CreateUsernameActivity,
                    e.message ?: "Unknown error",
                    Toast.LENGTH_LONG
                ).show()
            }
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

    private fun clearErrors() {
        etUsername.error = null
    }

    private fun safeText(et: TextInputEditText): String {
        return et.text?.toString()?.trim().orEmpty()
    }

    private fun setLoading(loading: Boolean) {
        isSubmitting = loading

        btnContinue.isEnabled = !loading
        etUsername.isEnabled = !loading
        btnContinue.text = if (loading) "Creating..." else "Continue"

        val overlay = findViewById<View?>(R.id.loadingOverlayCreateUsername)
        overlay?.visibility = if (loading) View.VISIBLE else View.GONE
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