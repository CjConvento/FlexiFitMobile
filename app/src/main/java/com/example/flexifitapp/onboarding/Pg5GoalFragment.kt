package com.example.flexifitapp.onboarding

import android.os.Bundle
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

        // ✅ ID Check: Siguraduhin na ang "muscle_gain", "cardio", at "rehab"
        // ay eksaktong match sa tinatanggap ng iyong Laravel/Node backend.
        val goals = listOf(
            OptionTile("muscle_gain", "Muscle Gain", R.drawable.ic_goal_bulking),
            OptionTile("cardio", "Cardio", R.drawable.ic_goal_cutting),
            OptionTile("rehab", "Recovery", R.drawable.ic_goal_leanbulk)
        )

        // --- HYDRATION: Direct from store, no local companion keys ---
        val preselected = OnboardingStore.getStringSet(requireContext(), FlexiFitKeys.FITNESS_GOALS)

        val glm = GridLayoutManager(requireContext(), 2)
        glm.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val count = goals.size
                return if (count % 2 == 1 && position == count - 1) 2 else 1
            }
        }

        rv.layoutManager = glm
        rv.adapter = MultiSelectTileAdapter(goals, preselected) { selectedIds ->
            // ✅ AUTOSAVE gamit ang FlexiFitKeys
            OnboardingStore.putStringSet(requireContext(), FlexiFitKeys.FITNESS_GOALS, selectedIds)
        }
    }

    override fun validateBeforeNext(): String? {
        val selected = OnboardingStore.getStringSet(requireContext(), FlexiFitKeys.FITNESS_GOALS    )

        // DEBUG: Mas madaling makita kung bakit ayaw lumipat kung may prefix
        return if (selected.isEmpty()) "DEBUG: Fitness goal set is empty. Select one!" else null
    }
}