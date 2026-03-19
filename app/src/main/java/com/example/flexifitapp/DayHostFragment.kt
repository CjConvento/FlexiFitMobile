package com.example.flexifitapp

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class DayHostFragment : Fragment(R.layout.fragment_day_host) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- CCTV START ---
        val rawDay = arguments?.getInt("day", -1) ?: -1
        val rawMonth = arguments?.getInt("month", -1) ?: -1
        val rawSource = arguments?.getString("sourceTab") ?: "NOT_SET"

        android.util.Log.d("CCTV_DAYHOST", "🚨 DATA RECEIVED: Day=$rawDay, Month=$rawMonth, Source=$rawSource")
        // --- CCTV END ---

        // Sa onViewCreated ng DayHostFragment
        val day = arguments?.getInt(NavKeys.ARG_DAY, -1) ?: -1
        val month = arguments?.getInt(NavKeys.ARG_MONTH, 1) ?: 1
        val sourceTab = arguments?.getString(NavKeys.ARG_SOURCE_TAB) ?: "WORKOUT"

        android.util.Log.d("CCTV_DAYHOST", "📥 Received from Calendar: Day=$day, Month=$month")

        // I-set ang title gamit ang NavKeys value
        view.findViewById<TextView>(R.id.tvDayHostTitle).text = "Day $day"

        val tvTitle = view.findViewById<TextView>(R.id.tvDayHostTitle)
        val btnBack = view.findViewById<ImageButton>(R.id.btnBackFromDayHost)
        val bottomNav = view.findViewById<BottomNavigationView>(R.id.bottomDayNav)

        // I-set ang title (Dito mawawala ang Day 0 basta tama ang "day" key mo)
        tvTitle.text = "Day $day"

        btnBack.setOnClickListener {
            android.util.Log.d("CCTV_DAYHOST", "🔙 Back button clicked. Returning to Calendar.")
            findNavController().popBackStack()
        }

        // --- INNER FUNCTIONS WITH LOGS ---
        fun showWorkout() {
            android.util.Log.i("CCTV_DAYHOST", "🏋️ Loading Workout Fragment for Day $day")
            val frag = WorkoutTabRootFragment().apply {
                arguments = bundleOf(
                    "ARG_DAY" to day,
                    "ARG_MONTH" to month,
                    "ARG_FROM_HOST" to true
                )
            }
            childFragmentManager.beginTransaction()
                .replace(R.id.dayHostContainer, frag)
                .commit()
        }

        fun showNutrition() {
            android.util.Log.i("CCTV_DAYHOST", "🍎 Loading Nutrition Fragment for Day $day")
            val frag = NutritionTabRootFragment().apply {
                arguments = bundleOf(
                    "ARG_DAY" to day,
                    "ARG_MONTH" to month,
                    "ARG_FROM_HOST" to true
                )
            }
            childFragmentManager.beginTransaction()
                .replace(R.id.dayHostContainer, frag)
                .commit()
        }

        // --- NAVIGATION LOGIC ---
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_day_workout -> {
                    android.util.Log.v("CCTV_DAYHOST", "Tab Switched: WORKOUT")
                    showWorkout(); true
                }
                R.id.nav_day_nutrition -> {
                    android.util.Log.v("CCTV_DAYHOST", "Tab Switched: NUTRITION")
                    showNutrition(); true
                }
                else -> false
            }
        }

        // --- INITIAL LOAD LOGIC ---
        if (sourceTab == "NUTRITION") {
            android.util.Log.d("CCTV_DAYHOST", "Initial Tab: NUTRITION")
            bottomNav.selectedItemId = R.id.nav_day_nutrition
            showNutrition()
        } else {
            android.util.Log.d("CCTV_DAYHOST", "Initial Tab: WORKOUT")
            bottomNav.selectedItemId = R.id.nav_day_workout
            showWorkout()
        }
    }}