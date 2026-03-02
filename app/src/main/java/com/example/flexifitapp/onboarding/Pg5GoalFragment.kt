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

        // Fitness Goal options (DB-aligned: Recovery saved as "rehab")
        val goals = listOf(
            OptionTile("muscle_gain", "Muscle Gain", R.drawable.ic_goal_bulking),
            OptionTile("cardio", "Cardio", R.drawable.ic_goal_cutting),
            OptionTile("rehab", "Recovery", R.drawable.ic_goal_leanbulk)
        )

        // Restore previous selection (autosave)
        val preselected = OnboardingStore.getStringSet(requireContext(), KEY_FITNESS_GOAL)

        // 2-column grid + center last tile if odd count
        val glm = GridLayoutManager(requireContext(), 2)
        glm.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val count = goals.size
                return if (count % 2 == 1 && position == count - 1) 2 else 1
            }
        }

        rv.layoutManager = glm
        rv.adapter = MultiSelectTileAdapter(goals, preselected) { selectedIds ->
            // ✅ AUTOSAVE
            OnboardingStore.putStringSet(requireContext(), KEY_FITNESS_GOAL, selectedIds)

            // optional: enable next if your base supports it
            // setNextEnabled(selectedIds.isNotEmpty())
        }
    }

    override fun validateBeforeNext(): String? {
        val selected = OnboardingStore.getStringSet(requireContext(), KEY_FITNESS_GOAL)
        return if (selected.isEmpty()) "Please select at least one fitness goal." else null
    }

    companion object {
        private const val KEY_FITNESS_GOAL = "fitness_goal"
    }
}