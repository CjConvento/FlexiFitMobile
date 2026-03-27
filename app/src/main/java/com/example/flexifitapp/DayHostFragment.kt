package com.example.flexifitapp

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class DayHostFragment : Fragment(R.layout.fragment_day_host) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val day = arguments?.getInt(NavKeys.ARG_DAY, -1) ?: -1
        val month = arguments?.getInt(NavKeys.ARG_MONTH, 1) ?: 1
        val sourceTab = arguments?.getString(NavKeys.ARG_SOURCE_TAB) ?: "WORKOUT"

        // Back button (now standalone)
        val btnBack = view.findViewById<ImageButton>(R.id.btnBackFromDayHost)
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        val bottomNav = view.findViewById<BottomNavigationView>(R.id.bottomDayNav)

        fun showWorkout() {
            val frag = WorkoutTabRootFragment()
            frag.arguments = bundleOf(
                NavKeys.ARG_DAY to day,
                NavKeys.ARG_MONTH to month,
                NavKeys.ARG_FROM_HOST to true
            )
            childFragmentManager.beginTransaction()
                .replace(R.id.dayHostContainer, frag)
                .commit()
        }

        fun showNutrition() {
            val frag = NutritionTabRootFragment()
            frag.arguments = bundleOf(
                NavKeys.ARG_DAY to day,
                NavKeys.ARG_MONTH to month,
                NavKeys.ARG_FROM_HOST to true
            )
            childFragmentManager.beginTransaction()
                .replace(R.id.dayHostContainer, frag)
                .commit()
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_day_workout -> {
                    showWorkout()
                    true
                }
                R.id.nav_day_nutrition -> {
                    showNutrition()
                    true
                }
                else -> false
            }
        }

        // Initial load
        if (sourceTab == "NUTRITION") {
            bottomNav.selectedItemId = R.id.nav_day_nutrition
            showNutrition()
        } else {
            bottomNav.selectedItemId = R.id.nav_day_workout
            showWorkout()
        }
    }
}