package com.example.flexifitapp.onboarding

import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import com.example.flexifitapp.R
import com.example.flexifitapp.UserPrefs
import kotlin.math.abs

class Pg3BackgroundFragment : BaseOnboardingFragment(
    layoutId = R.layout.obd_fragment_pg3_background
) {

    private lateinit var lifestyleState: WheelState
    private lateinit var levelState: WheelState

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isUpdate = arguments?.getBoolean("isUpdate", false) ?: false

        val lifestyleOptions = listOf("Sedentary", "Lightly Active", "Active", "Very Active")
        val levelOptions = listOf("Beginner", "Intermediate", "Advanced")

        // Load saved values
        val savedLifestyle = if (isUpdate) {
            UserPrefs.getString(requireContext(), "fitness_lifestyle", "Active")
        } else {
            OnboardingStore.getString(requireContext(), FlexiFitKeys.FITNESS_LIFESTYLE)
        }
        val savedLevel = if (isUpdate) {
            UserPrefs.getString(requireContext(), "fitness_level", "Beginner")
        } else {
            OnboardingStore.getString(requireContext(), FlexiFitKeys.FITNESS_LEVEL)
        }

        lifestyleState = WheelState(
            options = lifestyleOptions,
            index = lifestyleOptions.indexOfFirst { it.equals(savedLifestyle, true) }.let { if (it == -1) 0 else it }
        )
        levelState = WheelState(
            options = levelOptions,
            index = levelOptions.indexOfFirst { it.equals(savedLevel, true) }.let { if (it == -1) 0 else it }
        )

        bindSwipeWheelWrap(
            state = lifestyleState,
            swipeTarget = view.findViewById(R.id.lifestyleRow),
            tvLeft = view.findViewById(R.id.tvLifestyleLeft),
            tvSelected = view.findViewById(R.id.tvLifestyleSelected),
            tvRight = view.findViewById(R.id.tvLifestyleRight),
            onChanged = { selected ->
                if (isUpdate) {
                    UserPrefs.putString(requireContext(), "fitness_lifestyle", selected)
                } else {
                    OnboardingStore.putString(requireContext(), FlexiFitKeys.FITNESS_LIFESTYLE, selected)
                }
            }
        )

        bindSwipeWheelWrap(
            state = levelState,
            swipeTarget = view.findViewById(R.id.levelRow),
            tvLeft = view.findViewById(R.id.tvLevelLeft),
            tvSelected = view.findViewById(R.id.tvLevelSelected),
            tvRight = view.findViewById(R.id.tvLevelRight),
            onChanged = { selected ->
                if (isUpdate) {
                    UserPrefs.putString(requireContext(), "fitness_level", selected)
                } else {
                    OnboardingStore.putString(requireContext(), FlexiFitKeys.FITNESS_LEVEL, selected)
                }
            }
        )
    }

    // Keep the bindSwipeWheelWrap and WheelState classes as they are
    // (unchanged from your original code)
    // ...

    override fun validateBeforeNext(): String? {
        val isUpdate = arguments?.getBoolean("isUpdate", false) ?: false
        val lifestyle = if (isUpdate) {
            UserPrefs.getString(requireContext(), "fitness_lifestyle", "")
        } else {
            OnboardingStore.getString(requireContext(), FlexiFitKeys.FITNESS_LIFESTYLE)
        }
        val level = if (isUpdate) {
            UserPrefs.getString(requireContext(), "fitness_level", "")
        } else {
            OnboardingStore.getString(requireContext(), FlexiFitKeys.FITNESS_LEVEL)
        }
        return when {
            lifestyle.isBlank() -> "Please select your activity level."
            level.isBlank() -> "Please select your fitness level."
            else -> null
        }
    }
}