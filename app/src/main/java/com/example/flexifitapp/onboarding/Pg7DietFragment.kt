package com.example.flexifitapp.onboarding

import android.os.Bundle
import android.util.Log
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

        // 1. OPTIONS: Siguraduhing match ang keys sa C# DTO / Database mo babe
        val diets = listOf(
            OptionTile("balanced", "Balanced", R.drawable.ic_diet_balanced),
            OptionTile("high_protein", "High Protein", R.drawable.ic_diet_protein),
            OptionTile("lactose_free", "Lactose Free", R.drawable.ic_diet_lactosefree),
            OptionTile("keto", "Keto", R.drawable.ic_diet_keto),
            OptionTile("vegetarian", "Vegetarian", R.drawable.ic_diet_veg),
            OptionTile("vegan", "Vegan", R.drawable.ic_diet_vegan)
        )

        // 2. HYDRATION: Restore from Store
        val savedDiet = OnboardingStore.getString(requireContext(), FlexiFitKeys.DIETARY_TYPE)

        Log.d("FLEXIFIT_DEBUG", "--- Page 7 Hydration ---")
        Log.d("FLEXIFIT_DEBUG", "Restored Diet Type: '$savedDiet'")

        // 3. LAYOUT LOGIC: Grid with 2 columns
        val glm = GridLayoutManager(requireContext(), 2)
        glm.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (diets.size % 2 == 1 && position == diets.size - 1) 2 else 1
            }
        }
        rv.layoutManager = glm

        // 4. ADAPTER: Single-select with Auto-save
        rv.adapter = OptionTileAdapter(
            items = diets,
            initiallySelectedId = savedDiet
        ) { selected ->
            Log.d("FLEXIFIT_DEBUG", "Diet Selected: ${selected.id}")
            OnboardingStore.putString(requireContext(), FlexiFitKeys.DIETARY_TYPE, selected.id)
        }
    }

    override fun validateBeforeNext(): String? {
        val diet = OnboardingStore.getString(requireContext(), FlexiFitKeys.DIETARY_TYPE)

        Log.d("FLEXIFIT_DEBUG", "--- Validating Page 7 ---")
        Log.d("FLEXIFIT_DEBUG", "Final Diet Type: '$diet'")

        return if (diet.isBlank()) {
            "Please select a dietary preference to help us customize your meals."
        } else {
            null
        }
    }
}