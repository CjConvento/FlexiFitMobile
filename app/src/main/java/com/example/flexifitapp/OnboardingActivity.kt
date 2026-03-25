package com.example.flexifitapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.flexifitapp.onboarding.OnboardingStore
import kotlinx.coroutines.launch

class OnboardingActivity : AppCompatActivity() {

    private var isUpdateMode = false
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: OnboardingPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        isUpdateMode = intent.getBooleanExtra("isUpdate", false)

        // 1. Initialize ViewPager
        viewPager = findViewById(R.id.viewPager)

        // 2. Create adapter (pass the update flag)
        adapter = OnboardingPagerAdapter(this, isUpdateMode)

        // 3. Attach adapter to ViewPager
        viewPager.adapter = adapter

        viewPager.isUserInputEnabled = false

        initOnboardingFlow()
    }



    private fun initOnboardingFlow() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.api().bootstrap()

                if (response.isSuccessful) {
                    val data = response.body()

                    if (data != null) {
                        Log.d("BOOTSTRAP_DEBUG", "OnboardingActivity bootstrap: profileComplete=${data.profileComplete}, " +
                                "status=${data.status}, userId=${data.userId}, " +
                                "name=${data.name}, username=${data.username}")
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

            }
        }
    }

    // Add method to navigate between pages
    fun nextPage() {
        if (viewPager.currentItem < adapter.itemCount - 1) {
            viewPager.currentItem += 1
        }
    }

    fun previousPage() {
        if (viewPager.currentItem > 0) {
            viewPager.currentItem -= 1
        }
    }

    fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}