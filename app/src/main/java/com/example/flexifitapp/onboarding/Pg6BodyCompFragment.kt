package com.example.flexifitapp.onboarding

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flexifitapp.R
import com.example.flexifitapp.UserPrefs

class Pg6BodyCompFragment : BaseOnboardingFragment(
    layoutId = R.layout.obd_fragment_pg6_bodycomp
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isUpdate = arguments?.getBoolean("isUpdate", false) ?: false

        val options = listOf(
            OptionTile("lose_weight", "Lean / Toned", R.drawable.ic_goal_cutting),
            OptionTile("gain_weight", "Muscle Gain", R.drawable.ic_goal_bulking),
            OptionTile("maintain", "Recomposition", R.drawable.ic_goal_leanbulk)
        )

        val savedGoal = if (isUpdate) {
            UserPrefs.getString(requireContext(), UserPrefs.KEY_BODYCOMP_GOAL, "")
        } else {
            OnboardingStore.getString(requireContext(), FlexiFitKeys.BODYCOMP_GOAL)
        }

        val rv = view.findViewById<RecyclerView>(R.id.rvbcmpgoal)
        val glm = GridLayoutManager(requireContext(), 2)
        glm.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val count = options.size
                return if (count % 2 == 1 && position == count - 1) 2 else 1
            }
        }
        rv.layoutManager = glm

        rv.adapter = OptionTileAdapter(
            items = options,
            initiallySelectedId = savedGoal
        ) { selected ->
            if (isUpdate) {
                UserPrefs.putString(requireContext(), UserPrefs.KEY_BODYCOMP_GOAL, selected.id)
            } else {
                OnboardingStore.putString(requireContext(), FlexiFitKeys.BODYCOMP_GOAL, selected.id)
            }
        }
    }

    override fun validateBeforeNext(): String? {
        val isUpdate = arguments?.getBoolean("isUpdate", false) ?: false
        val selected = if (isUpdate) {
            UserPrefs.getString(requireContext(), UserPrefs.KEY_BODYCOMP_GOAL, "")
        } else {
            OnboardingStore.getString(requireContext(), FlexiFitKeys.BODYCOMP_GOAL)
        }
        return if (selected.isBlank()) "Please select a body composition goal." else null
    }
}