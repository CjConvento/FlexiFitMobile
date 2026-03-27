package com.example.flexifitapp.onboarding

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flexifitapp.R

class Pg5GoalFragment : BaseOnboardingFragment(
    layoutId = R.layout.obd_fragment_pg5_goal,
    nextActionId = R.id.a6
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rvFgoal)

        // 1. GOAL OPTIONS: Siguraduhing match ang "muscle_gain" at "cardio" sa folder names sa API
        val goals = listOf(
            OptionTile("MUSCLE_GAIN", "Muscle Gain", R.drawable.ic_goal_bulking),
            OptionTile("CARDIO", "Cardio", R.drawable.ic_goal_cutting),
            OptionTile("REHAB", "Recovery", R.drawable.ic_goal_leanbulk)
        )

        // 2. HYDRATION: Restore from Store
        val savedGoals = OnboardingStore.getStringSet(requireContext(), FlexiFitKeys.FITNESS_GOALS)
        val isRehabUser = OnboardingStore.getBoolean(requireContext(), FlexiFitKeys.IS_REHAB_USER)

        Log.d("FLEXIFIT_DEBUG", "--- Page 5 Hydration ---")
        Log.d("FLEXIFIT_DEBUG", "Restored Goals: $savedGoals")
        Log.d("FLEXIFIT_DEBUG", "Current User Status: ${if(isRehabUser) "REHAB" else "REGULAR"}")

        // 3. LAYOUT LOGIC: Centering the last item if odd
        val glm = GridLayoutManager(requireContext(), 2)
        glm.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (goals.size % 2 == 1 && position == goals.size - 1) 2 else 1
            }
        }
        rv.layoutManager = glm

        // 4. ADAPTER: Multi-select logic with Auto-save
        rv.adapter = MultiSelectTileAdapter(
            items = goals,
            preselected = savedGoals
        ) { selectedIds ->
            Log.d("FLEXIFIT_DEBUG", "Fitness Goals Updated: $selectedIds")
            OnboardingStore.putStringSet(requireContext(), FlexiFitKeys.FITNESS_GOALS, selectedIds)
        }
    }

    override fun validateBeforeNext(): String? {
        val selected = OnboardingStore.getStringSet(requireContext(), FlexiFitKeys.FITNESS_GOALS)

        Log.d("FLEXIFIT_DEBUG", "--- Validating Page 5 ---")
        Log.d("FLEXIFIT_DEBUG", "Final Goals to Save: $selected")

        return if (selected.isEmpty()) {
            "Please select at least one fitness goal to continue."
        } else {
            null
        }
    }
}