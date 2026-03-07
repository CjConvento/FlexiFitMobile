package com.example.flexifitapp.onboarding

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flexifitapp.R

class Pg7DietFragment : BaseOnboardingFragment(
    layoutId = R.layout.obd_fragment_pg7_diet,   // ⚠️ palitan kung iba name ng layout mo
    nextActionId = R.id.a8                      // ⚠️ palitan kung iba action mo
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rvdietgoal)

        // ✅ diet options (IDs = values you want saved to DB)
        val diets = listOf(
            OptionTile("balanced", "Balanced", R.drawable.ic_diet_balanced),
            OptionTile("high_protein", "High Protein", R.drawable.ic_diet_protein),
            OptionTile("lactose_free", "Lactose Free", R.drawable.ic_diet_lactosefree),
            OptionTile("keto", "Keto", R.drawable.ic_diet_keto),
            OptionTile("vegetarian", "Vegetarian", R.drawable.ic_diet_veg),
            OptionTile("vegan", "Vegan", R.drawable.ic_diet_vegan)
        )

        val preselectedId = OnboardingStore.getString(requireContext(), KEY_DIET)

        // 2-column grid + center last tile if odd count
        val glm = GridLayoutManager(requireContext(), 2)
        glm.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val count = diets.size
                return if (count % 2 == 1 && position == count - 1) 2 else 1
            }
        }

        rv.layoutManager = glm
        rv.adapter = OptionTileAdapter(
            items = diets,
            initiallySelectedId = preselectedId
        ) { selected ->
            // ✅ autosave selected diet ID
            OnboardingStore.putString(requireContext(), KEY_DIET, selected.id)

            // optional: enable next
            // setNextEnabled(true)
        }
    }

    override fun validateBeforeNext(): String? {
        val diet = OnboardingStore.getString(requireContext(), KEY_DIET)
        return if (diet.isBlank()) "Please select your preferred diet." else null
    }

    companion object {
        private const val KEY_DIET = "dietary_type"
    }
}