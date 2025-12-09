package com.example.flexifitapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class EmailVerificationActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var tvEmailInfo: TextView
    private lateinit var btnVerifyOtp: Button

    private var email: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_verification)

        auth = FirebaseAuth.getInstance()

        tvEmailInfo = findViewById(R.id.tvEmailInfo)
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp)

        email = intent.getStringExtra("email")

        tvEmailInfo.text = "We sent a verification link to your email:\n$email"

        btnVerifyOtp.setOnClickListener {
            verifyEmailStatus()
        }
    }

    // 🔵 Check if user clicked the email verification link
    private fun verifyEmailStatus() {
        val user = auth.currentUser

        if (user == null) {
            Toast.makeText(this, "No user found. Please log in again.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Force-refresh account status
        user.reload().addOnSuccessListener {
            if (user.isEmailVerified) {
                Toast.makeText(this, "Your account is now verified!", Toast.LENGTH_LONG).show()

                // (Optional) signOut para pilitin siyang mag-login ulit
                auth.signOut()

                // Proceed to Login screen
                val intent = Intent(this, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Email not verified yet. Please check your inbox.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
