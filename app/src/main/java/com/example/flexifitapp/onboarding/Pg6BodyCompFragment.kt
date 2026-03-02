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

        // ✅ SINGLE select body composition goal (palitan icons kung meron ka)
        val options = listOf(
            OptionTile("lose_weight", "Lean / Toned", R.drawable.ic_goal_cutting),
            OptionTile("gain_weight", "Muscle Gain", R.drawable.ic_goal_bulking),
            OptionTile("maintain", "Recomposition", R.drawable.ic_goal_leanbulk)
        )

        // restore saved selection
        val preselectedId = OnboardingStore.getString(requireContext(), KEY_BODYCOMP_GOAL)

        // 2-column grid + center last tile if odd count
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
            initiallySelectedId = preselectedId
        ) { selected ->
            // ✅ autosave selected ID
            OnboardingStore.putString(requireContext(), KEY_BODYCOMP_GOAL, selected.id)

            // optional enable next
            // setNextEnabled(true)
        }
    }

    override fun validateBeforeNext(): String? {
        val selected = OnboardingStore.getString(requireContext(), KEY_BODYCOMP_GOAL)
        return if (selected.isBlank()) "Please select your body composition goal." else null
    }

    companion object {
        private const val KEY_BODYCOMP_GOAL = "bodycomp_goal"
    }
}