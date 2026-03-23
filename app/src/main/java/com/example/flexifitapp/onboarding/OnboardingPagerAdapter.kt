package com.example.flexifitapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.flexifitapp.onboarding.*

class OnboardingPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val isUpdate: Boolean
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 10

    override fun createFragment(position: Int): Fragment {
        val fragment = when (position) {
            0 -> Pg1ProfileFragment()
            1 -> Pg1p5MetricsFragment()
            2 -> Pg2HealthFragment()
            3 -> Pg3BackgroundFragment()
            4 -> Pg4LocationFragment()
            5 -> Pg5GoalFragment()
            6 -> Pg6BodyCompFragment()
            7 -> Pg7DietFragment()
            8 -> Pg8ProgramsFragment()
            9 -> SummaryFragment()
            else -> throw IndexOutOfBoundsException()
        }
        // Pass the update flag to the fragment
        fragment.arguments = Bundle().apply {
            putBoolean("isUpdate", isUpdate)
        }
        return fragment
    }
}