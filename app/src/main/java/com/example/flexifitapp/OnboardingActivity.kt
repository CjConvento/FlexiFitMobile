package com.example.flexifitapp

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
                // 1. Tawagin ang Bootstrap API
                val response = ApiClient.api(this@OnboardingActivity).bootstrap()

                if (response.isSuccessful) {
                    val data = response.body()

                    if (data != null) {
                        // 2. HYDRATION: Kung may existing data na sa backend, i-save sa local storage
                        data.existingProfile?.let { profile ->
                            Log.d("OnboardingActivity", "Existing profile found. Hydrating store...")
                            OnboardingStore.hydrateFromProfile(this@OnboardingActivity, profile)
                        }

                        // 3. CHECK STATUS: Kung tapos na talaga ang onboarding, diretso sa Main
                        if (data.profileComplete) {
                            goToMain()
                        } else {
                            // Dito papasok ang NavHostFragment mo para sa Page 1
                            Log.d("OnboardingActivity", "Profile incomplete. Starting onboarding pages...")
                        }
                    }
                } else {
                    Log.e("OnboardingActivity", "Bootstrap failed with code: ${response.code()}")
                    // Fallback: Hayaan silang mag-onboarding kahit offline/error ang sync
                }
            } catch (e: Exception) {
                Log.e("OnboardingActivity", "Network error during bootstrap", e)
                Toast.makeText(this@OnboardingActivity, "Offline mode: Progress might not sync", Toast.LENGTH_SHORT).show()
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