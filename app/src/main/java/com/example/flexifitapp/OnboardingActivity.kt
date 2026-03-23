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

        // Simulan ang pag-check ng user status pagkabukas ng app

        isUpdateMode = intent.getBooleanExtra("isUpdate", false)

        // Initialize ViewPager
        viewPager = findViewById(R.id.viewPager)
        adapter = OnboardingPagerAdapter(this, isUpdateMode)
        viewPager.adapter = adapter

        // If you want to disable swiping (only use next/prev buttons inside fragments)
        viewPager.isUserInputEnabled = false

        // If you have a TabLayout for dots:
        // val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        // TabLayoutMediator(tabLayout, viewPager) { tab, position ->
        //     // optional: set tab text if needed
        // }.attach()


        initOnboardingFlow()
    }



    private fun initOnboardingFlow() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.api().bootstrap()

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