package com.example.flexifitapp.onboarding

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flexifitapp.R
import com.example.flexifitapp.UserPrefs

class Pg4LocationFragment : BaseOnboardingFragment(
    layoutId = R.layout.obd_fragment_pg4_location
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isUpdate = arguments?.getBoolean("isUpdate", false) ?: false

        val tiles = listOf(
            OptionTile("HOME", "Home", R.drawable.ic_home),
            OptionTile("GYM", "Gym", R.drawable.ic_workout),
            OptionTile("OUTDOOR", "Outdoor", R.drawable.ic_sun)
        )

        val savedEnvironments = if (isUpdate) {
            UserPrefs.getStringSet(requireContext(), "environment") ?: emptySet()
        } else {
            OnboardingStore.getStringSet(requireContext(), FlexiFitKeys.ENVIRONMENT)
        }

        val rv = view.findViewById<RecyclerView>(R.id.rvEnvironment)
        val glm = GridLayoutManager(requireContext(), 2)
        glm.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (tiles.size % 2 == 1 && position == tiles.size - 1) 2 else 1
            }
        }
        rv.layoutManager = glm

        rv.adapter = MultiSelectTileAdapter(
            items = tiles,
            preselected = savedEnvironments
        ) { selectedIds ->
            if (isUpdate) {
                UserPrefs.putStringSet(requireContext(), "environment", selectedIds)
            } else {
                OnboardingStore.putStringSet(requireContext(), FlexiFitKeys.ENVIRONMENT, selectedIds)
            }
        }
    }

    override fun validateBeforeNext(): String? {
        val isUpdate = arguments?.getBoolean("isUpdate", false) ?: false
        val selected = if (isUpdate) {
            UserPrefs.getStringSet(requireContext(), "environment") ?: emptySet()
        } else {
            OnboardingStore.getStringSet(requireContext(), FlexiFitKeys.ENVIRONMENT)
        }
        return if (selected.isEmpty()) {
            "Please choose at least one environment where you can workout."
        } else {
            null
        }
    }
}