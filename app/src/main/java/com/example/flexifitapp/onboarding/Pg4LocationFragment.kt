package com.example.flexifitapp.onboarding

import android.os.Bundle
import android.util.Log
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

        // 1. OPTIONS: Match sa folder/category logic natin sa backend
        val tiles = listOf(
            OptionTile("HOME", "Home", R.drawable.athome),
            OptionTile("GYM", "Gym", R.drawable.ic_workout),
            OptionTile("OUTDOOR", "Outdoor", R.drawable.outside)
        )

        // 2. HYDRATION: Restore from Store with Logs
        val savedEnvironments = OnboardingStore.getStringSet(requireContext(), FlexiFitKeys.ENVIRONMENT)

        Log.d("FLEXIFIT_DEBUG", "--- Page 4 Hydration ---")
        Log.d("FLEXIFIT_DEBUG", "Restored Environments: $savedEnvironments")

        // 3. LAYOUT LOGIC: Centering the last item if odd
        val glm = GridLayoutManager(requireContext(), 2)
        glm.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (tiles.size % 2 == 1 && position == tiles.size - 1) 2 else 1
            }
        }
        rv.layoutManager = glm

        // 4. ADAPTER: Multi-select logic
        rv.adapter = MultiSelectTileAdapter(
            items = tiles,
            preselected = savedEnvironments
        ) { selectedIds ->
            Log.d("FLEXIFIT_DEBUG", "Environment Selection Updated: $selectedIds")
            // Auto-save using the optimized OnboardingStore
            OnboardingStore.putStringSet(requireContext(), FlexiFitKeys.ENVIRONMENT, selectedIds)
        }
    }

    override fun validateBeforeNext(): String? {
        val selected = OnboardingStore.getStringSet(requireContext(), FlexiFitKeys.ENVIRONMENT)

        Log.d("FLEXIFIT_DEBUG", "--- Validating Page 4 ---")
        Log.d("FLEXIFIT_DEBUG", "Final Environments: $selected")

        return if (selected.isEmpty()) {
            "Please choose at least one environment where you can workout."
        } else {
            null
        }
    }
}