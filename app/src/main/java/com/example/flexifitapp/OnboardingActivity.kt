package com.example.flexifitapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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

        viewPager.isUserInputEnabled = true

        // Setup page indicator
        setupPageIndicator()

        initOnboardingFlow()
    }

    private fun setupPageIndicator() {
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateDots(position)
            }
        })
        viewPager.post { updateDots(viewPager.currentItem) }
    }

    private fun updateDots(position: Int) {
        // Find the current fragment's view using the default tag format "f<position>"
        val fragmentTag = "f$position"
        val fragment = supportFragmentManager.findFragmentByTag(fragmentTag)
        val fragmentView = fragment?.view ?: return

        val dotsContainer = fragmentView.findViewById<LinearLayout>(R.id.dotsContainer) ?: return

        // Create dots if not already created
        if (dotsContainer.childCount == 0) {
            val totalPages = adapter.itemCount
            for (i in 0 until totalPages) {
                val dot = View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(dpToPx(8), dpToPx(8)).apply {
                        marginEnd = dpToPx(8)
                        marginStart = dpToPx(8)
                    }
                    background = ContextCompat.getDrawable(this@OnboardingActivity, R.drawable.dot_inactive)
                }
                dotsContainer.addView(dot)
            }
        }

        // Update each dot's background based on current position
        for (i in 0 until dotsContainer.childCount) {
            val dot = dotsContainer.getChildAt(i)
            val drawable = if (i == position) {
                ContextCompat.getDrawable(this, R.drawable.dot_active)
            } else {
                ContextCompat.getDrawable(this, R.drawable.dot_inactive)
            }
            dot.background = drawable
        }
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

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

                        // ✅ FIX: Do NOT auto‑redirect when in update mode
                        if (data.profileComplete && !isUpdateMode) {
                            UserPrefs.setOnboardingDone(this@OnboardingActivity, true)
                            Log.d("FLEXIFIT_DEBUG", "Server says Profile is Complete and not in update mode. Going to Main.")
                            goToMain()
                        } else {
                            // Stay in onboarding (either incomplete or update mode)
                            UserPrefs.setOnboardingDone(this@OnboardingActivity, false)
                            Log.d("FLEXIFIT_DEBUG", "Profile Incomplete or Update Mode. Stay in Onboarding.")
                        }
                    }
                } else {
                    Log.e("FLEXIFIT_DEBUG", "Bootstrap Failed: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("FLEXIFIT_DEBUG", "Network Error in Bootstrap", e)
                // Keep user in onboarding (local check could be added)
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