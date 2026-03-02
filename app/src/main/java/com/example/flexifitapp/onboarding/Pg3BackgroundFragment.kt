package com.example.flexifitapp.onboarding

import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
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

        // --- Views (IDs based on your wheel/swipe XML) ---
        val lifestyleRow = view.findViewById<View>(R.id.lifestyleRow)
        val tvLifestyleLeft = view.findViewById<TextView>(R.id.tvLifestyleLeft)
        val tvLifestyleSelected = view.findViewById<TextView>(R.id.tvLifestyleSelected)
        val tvLifestyleRight = view.findViewById<TextView>(R.id.tvLifestyleRight)

        val levelRow = view.findViewById<View>(R.id.levelRow)
        val tvLevelLeft = view.findViewById<TextView>(R.id.tvLevelLeft)
        val tvLevelSelected = view.findViewById<TextView>(R.id.tvLevelSelected)
        val tvLevelRight = view.findViewById<TextView>(R.id.tvLevelRight)

        // Optional: hide buttons if swipe-only (safe if they exist)
        view.findViewById<ImageButton?>(R.id.btnLifestylePrev)?.visibility = View.GONE
        view.findViewById<ImageButton?>(R.id.btnLifestyleNext)?.visibility = View.GONE
        view.findViewById<ImageButton?>(R.id.btnLevelPrev)?.visibility = View.GONE
        view.findViewById<ImageButton?>(R.id.btnLevelNext)?.visibility = View.GONE

        // --- Options ---
        val lifestyleOptions = listOf("Sedentary", "Lightly Active", "Active", "Very Active")
        val levelOptions = listOf("Beginner", "Intermediate", "Advanced")

        // --- Restore saved values (prefer saved string; fallback to saved index; fallback to default 1) ---
        lifestyleState = WheelState(
            options = lifestyleOptions,
            index = restoreIndex(
                options = lifestyleOptions,
                savedValue = OnboardingStore.getString(requireContext(), KEY_LIFESTYLE),
                savedIndex = OnboardingStore.getInt(requireContext(), KEY_LIFESTYLE_INDEX, 1),
                fallback = 1
            )
        )

        levelState = WheelState(
            options = levelOptions,
            index = restoreIndex(
                options = levelOptions,
                savedValue = OnboardingStore.getString(requireContext(), KEY_LEVEL),
                savedIndex = OnboardingStore.getInt(requireContext(), KEY_LEVEL_INDEX, 1),
                fallback = 1
            )
        )

        // --- Bind wheels ---
        bindSwipeWheelWrap(
            state = lifestyleState,
            swipeTarget = lifestyleRow,
            tvLeft = tvLifestyleLeft,
            tvSelected = tvLifestyleSelected,
            tvRight = tvLifestyleRight
        ) { selected ->
            // autosave (both index + string)
            OnboardingStore.putInt(requireContext(), KEY_LIFESTYLE_INDEX, lifestyleState.index)
            OnboardingStore.putString(requireContext(), KEY_LIFESTYLE, selected)
        }

        bindSwipeWheelWrap(
            state = levelState,
            swipeTarget = levelRow,
            tvLeft = tvLevelLeft,
            tvSelected = tvLevelSelected,
            tvRight = tvLevelRight
        ) { selected ->
            OnboardingStore.putInt(requireContext(), KEY_LEVEL_INDEX, levelState.index)
            OnboardingStore.putString(requireContext(), KEY_LEVEL, selected)
        }
    }

    override fun validateBeforeNext(): String? {
        val lifestyle = OnboardingStore.getString(requireContext(), KEY_LIFESTYLE)
        val level = OnboardingStore.getString(requireContext(), KEY_LEVEL)

        return when {
            lifestyle.isBlank() -> "Please choose your lifestyle/activity level."
            level.isBlank() -> "Please choose your fitness level."
            else -> null
        }
    }

    private fun restoreIndex(
        options: List<String>,
        savedValue: String,
        savedIndex: Int,
        fallback: Int
    ): Int {
        if (options.isEmpty()) return 0
        val byValue = options.indexOfFirst { it.equals(savedValue, ignoreCase = true) }
        return when {
            byValue != -1 -> byValue
            savedIndex in options.indices -> savedIndex
            else -> fallback.coerceIn(options.indices)
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
            val i = norm(state.index)
            state.index = i
            tvSelected.text = state.options[i]
            tvLeft.text = state.options[norm(i - 1)]
            tvRight.text = state.options[norm(i + 1)]
        }

        fun prev() {
            state.index = norm(state.index - 1)
            render()
            onChanged(state.options[state.index])
        }

        fun next() {
            state.index = norm(state.index + 1)
            render()
            onChanged(state.options[state.index])
        }

        val detector = GestureDetector(
            swipeTarget.context,
            object : GestureDetector.SimpleOnGestureListener() {
                private val SWIPE_DISTANCE = 60
                private val SWIPE_VELOCITY = 60

                override fun onDown(e: MotionEvent): Boolean = true

                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    if (e1 == null) return false
                    val dx = e2.x - e1.x
                    val dy = e2.y - e1.y

                    if (abs(dx) > abs(dy) &&
                        abs(dx) > SWIPE_DISTANCE &&
                        abs(velocityX) > SWIPE_VELOCITY
                    ) {
                        if (dx < 0) next() else prev()
                        return true
                    }
                    return false
                }
            }
        )

        swipeTarget.setOnTouchListener { _, event ->
            detector.onTouchEvent(event)
            true
        }

        // initial render + autosave current values
        render()
        onChanged(state.options[state.index])
    }

    companion object {
        private const val KEY_LIFESTYLE = "fitness_lifestyle"
        private const val KEY_LIFESTYLE_INDEX = "fitness_lifestyle_index"

        private const val KEY_LEVEL = "fitness_level"
        private const val KEY_LEVEL_INDEX = "fitness_level_index"
    }
}