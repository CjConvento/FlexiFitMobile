package com.example.flexifitapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.flexifitapp.databinding.ActivitySurveyStep1Binding
import com.google.firebase.auth.FirebaseAuth

class SurveyStep1Activity : AppCompatActivity() {

    private lateinit var binding: ActivitySurveyStep1Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySurveyStep1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        fun goToLogin() {
            // 🔐 Log out current user (para hindi auto-redirect to Survey ulit)
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }

        // 🔙 Top AppBar back
        binding.topAppBar.setNavigationOnClickListener {
            goToLogin()
        }

        // 🔙 Bottom Back button
        binding.btnBack.setOnClickListener {
            goToLogin()
        }

        // 🔙 System back button
        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goToLogin()
            }
        }
        onBackPressedDispatcher.addCallback(this, backCallback)

        // ▶️ Continue logic...
        binding.btnContinue.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val age = binding.etAge.text.toString().trim()
            val weight = binding.etWeight.text.toString().trim()
            val height = binding.etHeight.text.toString().trim()

            if (name.isEmpty() || age.isEmpty() || weight.isEmpty() || height.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fitness = when {
                binding.rbBeginner.isChecked -> "Beginner"
                binding.rbIntermediate.isChecked -> "Intermediate"
                binding.rbAdvanced.isChecked -> "Advanced"
                else -> ""
            }

            if (fitness.isEmpty()) {
                Toast.makeText(this, "Please select fitness level", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, SurveyStep2Activity::class.java).apply {
                putExtra("name", name)
                putExtra("age", age)
                putExtra("weight", weight)
                putExtra("height", height)
                putExtra("fitness", fitness)
            }

            startActivity(intent)
        }
    }
}





