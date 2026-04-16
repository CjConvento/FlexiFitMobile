package com.example.flexifitapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.flexifitapp.onboarding.*
import com.example.flexifitapp.onboarding.allergy.OnboardingAllergyFragment

class OnboardingPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val isUpdate: Boolean
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 11

    override fun createFragment(position: Int): Fragment {
        val fragment = when (position) {
            0 -> Pg1ProfileFragment()
            1 -> Pg1p5MetricsFragment()
            2 -> Pg2HealthFragment()
            3 -> OnboardingAllergyFragment()        // bagong page
            4 -> Pg3BackgroundFragment()
            5 -> Pg4LocationFragment()
            6 -> Pg5GoalFragment()
            7 -> Pg6BodyCompFragment()
            8 -> Pg7DietFragment()
            9 -> Pg8ProgramsFragment()
            10 -> SummaryFragment()
            else -> throw IndexOutOfBoundsException()
        }
        // Pass the update flag to the fragment
        fragment.arguments = Bundle().apply {
            putBoolean("isUpdate", isUpdate)
        }
        return fragment
    }
}