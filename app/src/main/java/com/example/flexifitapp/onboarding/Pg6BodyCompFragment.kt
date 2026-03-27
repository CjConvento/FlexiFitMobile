package com.example.flexifitapp.onboarding

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flexifitapp.R

class Pg6BodyCompFragment : BaseOnboardingFragment(
    layoutId = R.layout.obd_fragment_pg6_bodycomp,
    nextActionId = R.id.a7
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rvbcmpgoal)

        // ✅ IDs should match your backend's expected strings for Body Comp goals
        val options = listOf(
            OptionTile("lose_weight", "Lean / Toned", R.drawable.ic_goal_cutting),
            OptionTile("gain_weight", "Muscle Gain", R.drawable.ic_goal_bulking),
            OptionTile("maintain", "Recomposition", R.drawable.ic_goal_leanbulk)
        )

        // --- HYDRATION: Restore using FlexiFitKeys ---
        val preselectedId = OnboardingStore.getString(requireContext(), FlexiFitKeys.BODYCOMP_GOAL)

        val glm = GridLayoutManager(requireContext(), 2)
        glm.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val count = options.size
                return if (count % 2 == 1 && position == count - 1) 2 else 1
            }
        }

        rv.layoutManager = glm

        // Gagamit tayo ng OptionTileAdapter (Single Select logic)
        rv.adapter = OptionTileAdapter(
            items = options,
            initiallySelectedId = preselectedId
        ) { selected ->
            // ✅ AUTOSAVE using FlexiFitKeys
            OnboardingStore.putString(requireContext(), FlexiFitKeys.BODYCOMP_GOAL, selected.id)
        }
    }

    override fun validateBeforeNext(): String? {
        val selected = OnboardingStore.getString(requireContext(), FlexiFitKeys.BODYCOMP_GOAL)

        // Fail-fast debug message
        return if (selected.isBlank()) "DEBUG: Body composition goal is missing. Pick one!" else null
    }
}