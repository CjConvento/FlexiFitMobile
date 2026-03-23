package com.example.flexifitapp.onboarding

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flexifitapp.R
import com.example.flexifitapp.UserPrefs

class Pg5GoalFragment : BaseOnboardingFragment(
    layoutId = R.layout.obd_fragment_pg5_goal
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isUpdate = arguments?.getBoolean("isUpdate", false) ?: false

        val goals = listOf(
            OptionTile("MUSCLE_GAIN", "Muscle Gain", R.drawable.ic_goal_bulking),
            OptionTile("CARDIO", "Cardio", R.drawable.ic_goal_cutting),
            OptionTile("REHAB", "Recovery", R.drawable.ic_goal_leanbulk)
        )

        val savedGoals = if (isUpdate) {
            UserPrefs.getStringSet(requireContext(), UserPrefs.KEY_FITNESS_GOAL_SET) ?: emptySet()
        } else {
            OnboardingStore.getStringSet(requireContext(), FlexiFitKeys.FITNESS_GOALS)
        }

        val rv = view.findViewById<RecyclerView>(R.id.rvFgoal)
        val glm = GridLayoutManager(requireContext(), 2)
        glm.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (goals.size % 2 == 1 && position == goals.size - 1) 2 else 1
            }
        }
        rv.layoutManager = glm

        rv.adapter = MultiSelectTileAdapter(
            items = goals,
            preselected = savedGoals
        ) { selectedIds ->
            if (isUpdate) {
                UserPrefs.putStringSet(requireContext(), UserPrefs.KEY_FITNESS_GOAL_SET, selectedIds)
            } else {
                OnboardingStore.putStringSet(requireContext(), FlexiFitKeys.FITNESS_GOALS, selectedIds)
            }
        }
    }

    override fun validateBeforeNext(): String? {
        val isUpdate = arguments?.getBoolean("isUpdate", false) ?: false
        val selected = if (isUpdate) {
            UserPrefs.getStringSet(requireContext(), UserPrefs.KEY_FITNESS_GOAL_SET) ?: emptySet()
        } else {
            OnboardingStore.getStringSet(requireContext(), FlexiFitKeys.FITNESS_GOALS)
        }
        return if (selected.isEmpty()) {
            "Please select at least one fitness goal to continue."
        } else {
            null
        }
    }
}