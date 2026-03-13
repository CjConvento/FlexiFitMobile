package com.example.flexifitapp.onboarding

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flexifitapp.R

class Pg4LocationFragment : BaseOnboardingFragment(
    layoutId = R.layout.obd_fragment_pg4_location,
    nextActionId = R.id.a5
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rvEnvironment)

        // Siguraduhin na ang IDs (home, gym, outdoor) ay match sa inaasahan ng backend
        val tiles = listOf(
            OptionTile("home", "Home", R.drawable.ic_home),
            OptionTile("gym", "Gym", R.drawable.ic_workout), // updated drawable name
            OptionTile("outdoor", "Outdoor", R.drawable.ic_sun) // updated drawable name
        )

        // --- HYDRATION: No fallback, direct from store ---
        val preselected = OnboardingStore.getStringSet(requireContext(), FlexiFitKeys.ENVIRONMENT)

        val glm = GridLayoutManager(requireContext(), 2)
        glm.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val count = tiles.size
                // Logic para mag-center ang huling item kung odd number
                return if (count % 2 == 1 && position == count - 1) 2 else 1
            }
        }

        rv.layoutManager = glm

        // Gamit ang MultiSelectTileAdapter na nire-restore ang state ng checkboxes/cards
        rv.adapter = MultiSelectTileAdapter(
            items = tiles,
            preselected = preselected
        ) { selectedIds ->
            // Auto-save every click
            OnboardingStore.putStringSet(requireContext(), FlexiFitKeys.ENVIRONMENT, selectedIds)
        }
    }

    override fun validateBeforeNext(): String? {
        val selected = OnboardingStore.getStringSet(requireContext(), FlexiFitKeys.ENVIRONMENT)
        return if (selected.isEmpty()) {
            "DEBUG: No environment selected. Please choose at least one."
        } else {
            null
        }
    }
}