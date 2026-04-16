package com.example.flexifitapp.onboarding

import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import com.example.flexifitapp.R
import kotlin.math.abs

class Pg3BackgroundFragment : BaseOnboardingFragment(
    layoutId = R.layout.obd_fragment_pg3_background,
    nextActionId = R.id.a4
) {

    private data class WheelState(val options: List<String>, var index: Int = 0)

    private lateinit var lifestyleState: WheelState
    private lateinit var levelState: WheelState

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. OPTIONS DEFINITION (Dapat match sa database categories mo babe)
        val lifestyleOptions = listOf("Sedentary", "Lightly Active", "Active", "Very Active")
        val levelOptions = listOf("Beginner", "Intermediate", "Advanced")

        // 2. HYDRATION: Restore from Store with Logs
        val savedLifestyle = OnboardingStore.getString(requireContext(), FlexiFitKeys.FITNESS_LIFESTYLE)
        val savedLevel = OnboardingStore.getString(requireContext(), FlexiFitKeys.FITNESS_LEVEL)

        Log.d("FLEXIFIT_DEBUG", "--- Page 3 Hydration ---")
        Log.d("FLEXIFIT_DEBUG", "Restoring Lifestyle: '$savedLifestyle', Level: '$savedLevel'")

        lifestyleState = WheelState(
            options = lifestyleOptions,
            index = lifestyleOptions.indexOfFirst { it.equals(savedLifestyle, true) }.let { if (it == -1) 0 else it }
        )

        levelState = WheelState(
            options = levelOptions,
            index = levelOptions.indexOfFirst { it.equals(savedLevel, true) }.let { if (it == -1) 0 else it }
        )

        // 3. BIND LIFESTYLE WHEEL
        bindSwipeWheelWrap(
            state = lifestyleState,
            swipeTarget = view.findViewById(R.id.lifestyleRow),
            tvLeft = view.findViewById(R.id.tvLifestyleLeft),
            tvSelected = view.findViewById(R.id.tvLifestyleSelected),
            tvRight = view.findViewById(R.id.tvLifestyleRight)
        ) { selected ->
            Log.d("FLEXIFIT_DEBUG", "Lifestyle Swiped -> $selected")
            OnboardingStore.putString(requireContext(), FlexiFitKeys.FITNESS_LIFESTYLE, selected)
        }

        // 4. BIND LEVEL WHEEL
        bindSwipeWheelWrap(
            state = levelState,
            swipeTarget = view.findViewById(R.id.levelRow),
            tvLeft = view.findViewById(R.id.tvLevelLeft),
            tvSelected = view.findViewById(R.id.tvLevelSelected),
            tvRight = view.findViewById(R.id.tvLevelRight)
        ) { selected ->
            Log.d("FLEXIFIT_DEBUG", "Level Swiped -> $selected")
            OnboardingStore.putString(requireContext(), FlexiFitKeys.FITNESS_LEVEL, selected)
        }

        // 5. INITIAL SAVE: Para siguradong may data kahit 'di mag-swipe
        saveCurrentStates()
    }

    private fun saveCurrentStates() {
        val lifestyle = lifestyleState.options[lifestyleState.index]
        val level = levelState.options[levelState.index]

        OnboardingStore.putString(requireContext(), FlexiFitKeys.FITNESS_LIFESTYLE, lifestyle)
        OnboardingStore.putString(requireContext(), FlexiFitKeys.FITNESS_LEVEL, level)

        Log.d("FLEXIFIT_DEBUG", "Initial State Saved: Lifestyle=$lifestyle, Level=$level")
    }

    private fun bindSwipeWheelWrap(
        state: WheelState,
        swipeTarget: View,
        tvLeft: TextView,
        tvSelected: TextView,
        tvRight: TextView,
        onChanged: (selected: String) -> Unit
    ) {
        val last = state.options.lastIndex

        fun norm(i: Int): Int {
            if (last < 0) return 0
            var x = i % (last + 1)
            if (x < 0) x += (last + 1)
            return x
        }

        fun render() {
            val i = norm(state.index)
            tvSelected.text = state.options[i]
            tvLeft.text = state.options[norm(i - 1)]
            tvRight.text = state.options[norm(i + 1)]
        }

        render()

        val detector = GestureDetector(swipeTarget.context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, vx: Float, vy: Float): Boolean {
                if (e1 == null) return false
                val dx = e2.x - e1.x
                if (abs(dx) > 60 && abs(vx) > 60) {
                    state.index = if (dx < 0) norm(state.index + 1) else norm(state.index - 1)
                    render()
                    onChanged(state.options[norm(state.index)])
                    return true
                }
                return false
            }
            override fun onDown(e: MotionEvent): Boolean = true
        })

        swipeTarget.setOnTouchListener { v, event ->
            // Prevent parent (ViewPager) from intercepting when we start a touch
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.parent?.requestDisallowInterceptTouchEvent(true)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.parent?.requestDisallowInterceptTouchEvent(false)
                }
            }
            val handled = detector.onTouchEvent(event)
            if (event.action == MotionEvent.ACTION_UP && !handled) {
                v.performClick()
            }
            true // Consume all touch events so parent doesn't steal
        }
    }

    override fun validateBeforeNext(): String? {
        val lifestyle = OnboardingStore.getString(requireContext(), FlexiFitKeys.FITNESS_LIFESTYLE)
        val level = OnboardingStore.getString(requireContext(), FlexiFitKeys.FITNESS_LEVEL)

        Log.d("FLEXIFIT_DEBUG", "--- Validating Page 3 ---")
        Log.d("FLEXIFIT_DEBUG", "Final Data: Lifestyle='$lifestyle', Level='$level'")

        return when {
            lifestyle.isBlank() -> "Please select your activity level."
            level.isBlank() -> "Please select your fitness level."
            else -> null
        }
    }
}