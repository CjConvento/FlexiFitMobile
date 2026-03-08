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

        val tiles = listOf(
            OptionTile("home", "Home", R.drawable.ic_home),
            OptionTile("gym", "Gym", R.drawable.ic_home),
            OptionTile("outdoor", "Outdoor", R.drawable.ic_home)
        )

        val preselected = OnboardingStore.getStringSet(requireContext(), KEY_ENVIRONMENT)

        val glm = GridLayoutManager(requireContext(), 2)
        glm.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val count = tiles.size
                return if (count % 2 == 1 && position == count - 1) 2 else 1
            }
        }

        rv.layoutManager = glm
        rv.adapter = MultiSelectTileAdapter(
            items = tiles,
            preselected = preselected
        ) { selectedIds ->
            OnboardingStore.putStringSet(requireContext(), KEY_ENVIRONMENT, selectedIds)
        }
    }

    override fun validateBeforeNext(): String? {
        val selected = OnboardingStore.getStringSet(requireContext(), KEY_ENVIRONMENT)
        return if (selected.isEmpty()) {
            "Please select at least one workout environment."
        } else {
            null
        }
    }

    companion object {
        private const val KEY_ENVIRONMENT = "environment"
    }
}