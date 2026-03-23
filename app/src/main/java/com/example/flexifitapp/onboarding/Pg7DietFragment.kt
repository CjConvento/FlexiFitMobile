package com.example.flexifitapp.onboarding

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flexifitapp.R
import com.example.flexifitapp.UserPrefs

class Pg7DietFragment : BaseOnboardingFragment(
    layoutId = R.layout.obd_fragment_pg7_diet
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isUpdate = arguments?.getBoolean("isUpdate", false) ?: false

        val diets = listOf(
            OptionTile("balanced", "Balanced", R.drawable.ic_diet_balanced),
            OptionTile("high_protein", "High Protein", R.drawable.ic_diet_protein),
            OptionTile("lactose_free", "Lactose Free", R.drawable.ic_diet_lactosefree),
            OptionTile("keto", "Keto", R.drawable.ic_diet_keto),
            OptionTile("vegetarian", "Vegetarian", R.drawable.ic_diet_veg),
            OptionTile("vegan", "Vegan", R.drawable.ic_diet_vegan)
        )

        val savedDiet = if (isUpdate) {
            UserPrefs.getString(requireContext(), "dietary_type", "balanced")
        } else {
            OnboardingStore.getString(requireContext(), FlexiFitKeys.DIETARY_TYPE)
        }

        val rv = view.findViewById<RecyclerView>(R.id.rvdietgoal)
        val glm = GridLayoutManager(requireContext(), 2)
        glm.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (diets.size % 2 == 1 && position == diets.size - 1) 2 else 1
            }
        }
        rv.layoutManager = glm

        rv.adapter = OptionTileAdapter(
            items = diets,
            initiallySelectedId = savedDiet
        ) { selected ->
            if (isUpdate) {
                UserPrefs.putString(requireContext(), "dietary_type", selected.id)
            } else {
                OnboardingStore.putString(requireContext(), FlexiFitKeys.DIETARY_TYPE, selected.id)
            }
        }
    }

    override fun validateBeforeNext(): String? {
        val isUpdate = arguments?.getBoolean("isUpdate", false) ?: false
        val diet = if (isUpdate) {
            UserPrefs.getString(requireContext(), "dietary_type", "")
        } else {
            OnboardingStore.getString(requireContext(), FlexiFitKeys.DIETARY_TYPE)
        }
        return if (diet.isBlank()) {
            "Please select a dietary preference to help us customize your meals."
        } else {
            null
        }
    }
}