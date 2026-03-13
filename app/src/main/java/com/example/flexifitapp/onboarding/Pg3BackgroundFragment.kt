package com.example.flexifitapp.onboarding

import android.os.Bundle
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

    private data class WheelState(val options: List<String>, var index: Int = -1) // Start at -1 to detect abnormal state

    private lateinit var lifestyleState: WheelState
    private lateinit var levelState: WheelState

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val lifestyleRow = view.findViewById<View>(R.id.lifestyleRow)
        val tvLifestyleLeft = view.findViewById<TextView>(R.id.tvLifestyleLeft)
        val tvLifestyleSelected = view.findViewById<TextView>(R.id.tvLifestyleSelected)
        val tvLifestyleRight = view.findViewById<TextView>(R.id.tvLifestyleRight)

        val levelRow = view.findViewById<View>(R.id.levelRow)
        val tvLevelLeft = view.findViewById<TextView>(R.id.tvLevelLeft)
        val tvLevelSelected = view.findViewById<TextView>(R.id.tvLevelSelected)
        val tvLevelRight = view.findViewById<TextView>(R.id.tvLevelRight)

        val lifestyleOptions = listOf("Sedentary", "Lightly Active", "Active", "Very Active", "Extra Active")
        val levelOptions = listOf("Beginner", "Intermediate", "Advanced", "Elite")

        // --- HYDRATION: Strict Restore ---
        lifestyleState = WheelState(
            options = lifestyleOptions,
            index = lifestyleOptions.indexOfFirst { it.equals(OnboardingStore.getString(requireContext(), FlexiFitKeys.FITNESS_LIFESTYLE), true) }
        )

        levelState = WheelState(
            options = levelOptions,
            index = levelOptions.indexOfFirst { it.equals(OnboardingStore.getString(requireContext(), FlexiFitKeys.FITNESS_LEVEL), true) }
        )

        // --- Bind wheels ---
        bindSwipeWheelWrap(lifestyleState, lifestyleRow, tvLifestyleLeft, tvLifestyleSelected, tvLifestyleRight) { selected ->
            OnboardingStore.putString(requireContext(), FlexiFitKeys.FITNESS_LIFESTYLE, selected)
        }

        bindSwipeWheelWrap(levelState, levelRow, tvLevelLeft, tvLevelSelected, tvLevelRight) { selected ->
            OnboardingStore.putString(requireContext(), FlexiFitKeys.FITNESS_LEVEL, selected)
        }
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
            // ABNORMAL BEHAVIOR VISUALIZER:
            // Kung ang index ay -1 (ibig sabihin walang nahanap na match sa store),
            // ang UI ay magpapakita ng index 0 pero hindi mag-a-autosave.
            val i = if (state.index == -1) 0 else norm(state.index)

            tvSelected.text = if (state.index == -1) "NOT_FOUND" else state.options[i]
            tvLeft.text = state.options[norm(i - 1)]
            tvRight.text = state.options[norm(i + 1)]
        }

        val detector = GestureDetector(swipeTarget.context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, vx: Float, vy: Float): Boolean {
                if (e1 == null) return false
                val dx = e2.x - e1.x
                if (abs(dx) > 60 && abs(vx) > 60) {
                    // Start from 0 if it was -1
                    val currentIdx = if (state.index == -1) 0 else state.index
                    state.index = if (dx < 0) norm(currentIdx + 1) else norm(currentIdx - 1)
                    render()
                    onChanged(state.options[state.index])
                    return true
                }
                return false
            }
            override fun onDown(e: MotionEvent): Boolean = true
        })

        swipeTarget.setOnTouchListener { v, event ->
            val handled = detector.onTouchEvent(event)

            // Kapag natapos ang touch action (UP), tawagin ang performClick
            if (event.action == MotionEvent.ACTION_UP && !handled) {
                v.performClick()
            }

            true // Ibalik ang true para ma-consume ang touch events
        }
    }

    override fun validateBeforeNext(): String? {
        val lifestyle = OnboardingStore.getString(requireContext(), FlexiFitKeys.FITNESS_LIFESTYLE)
        val level = OnboardingStore.getString(requireContext(), FlexiFitKeys.FITNESS_LEVEL)

        return when {
            lifestyle.isBlank() -> "DEBUG: Lifestyle is empty!"
            level.isBlank() -> "DEBUG: Fitness level is empty!"
            else -> null
        }
    }
}