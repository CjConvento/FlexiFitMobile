package com.example.flexifitapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.flexifitapp.onboarding.OnboardingStore
import kotlinx.coroutines.launch

class OnboardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        // Simulan ang pag-check ng user status pagkabukas ng app
        initOnboardingFlow()
    }

    private fun initOnboardingFlow() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.api(this@OnboardingActivity).bootstrap()

                if (response.isSuccessful) {
                    val data = response.body()

                    if (data != null) {
                        // 1. UPDATE LOCAL PREFS PARA HINDI NA BUMALIK
                        if (data.profileComplete) {
                            UserPrefs.setOnboardingDone(this@OnboardingActivity, true)
                            Log.d("FLEXIFIT_DEBUG", "Server says Profile is Complete. Going to Main.")
                            goToMain()
                        } else {
                            // 2. Kung incomplete, stay sa onboarding
                            UserPrefs.setOnboardingDone(this@OnboardingActivity, false)
                            Log.d("FLEXIFIT_DEBUG", "Profile Incomplete. Stay in Onboarding.")
                        }
                    }
                } else {
                    Log.e("FLEXIFIT_DEBUG", "Bootstrap Failed: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("FLEXIFIT_DEBUG", "Network Error in Bootstrap", e)
                // Kung offline, i-check ang local prefs as fallback
                if (UserPrefs.isOnboardingDone(this@OnboardingActivity)) {
                    goToMain()
                }
            }
        }
    }

    fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}