package com.example.flexifitapp.onboarding

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flexifitapp.R

class Pg7DietFragment : BaseOnboardingFragment(
    layoutId = R.layout.obd_fragment_pg7_diet,
    nextActionId = R.id.a8
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rvdietgoal)

        // ✅ IDs should strictly match the values expected by your Backend
        val diets = listOf(
            OptionTile("balanced", "Balanced", R.drawable.ic_diet_balanced),
            OptionTile("high_protein", "High Protein", R.drawable.ic_diet_protein),
            OptionTile("lactose_free", "Lactose Free", R.drawable.ic_diet_lactosefree),
            OptionTile("keto", "Keto", R.drawable.ic_diet_keto),
            OptionTile("vegetarian", "Vegetarian", R.drawable.ic_diet_veg),
            OptionTile("vegan", "Vegan", R.drawable.ic_diet_vegan)
        )

        // --- HYDRATION: No local keys, use FlexiFitKeys.DIETARY_TYPE ---
        val preselectedId = OnboardingStore.getString(requireContext(), FlexiFitKeys.DIETARY_TYPE)

        val glm = GridLayoutManager(requireContext(), 2)
        glm.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val count = diets.size
                return if (count % 2 == 1 && position == count - 1) 2 else 1
            }
        }

        rv.layoutManager = glm

        // Single Select Adapter
        rv.adapter = OptionTileAdapter(
            items = diets,
            initiallySelectedId = preselectedId
        ) { selected ->
            // ✅ AUTOSAVE to store
            OnboardingStore.putString(requireContext(), FlexiFitKeys.DIETARY_TYPE, selected.id)
        }
    }

    override fun validateBeforeNext(): String? {
        val diet = OnboardingStore.getString(requireContext(), FlexiFitKeys.DIETARY_TYPE)

        // Fail-fast debug message para sa dev
        return if (diet.isBlank()) "DEBUG: No diet type selected. Please pick one!" else null
    }
}