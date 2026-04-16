package com.example.flexifitapp.onboarding.allergy

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.flexifitapp.OnboardingActivity
import com.example.flexifitapp.R
import com.example.flexifitapp.onboarding.BaseOnboardingFragment
import com.example.flexifitapp.onboarding.FlexiFitKeys
import com.example.flexifitapp.onboarding.OnboardingStore

class OnboardingAllergyFragment : BaseOnboardingFragment(
    layoutId = R.layout.obd_fragment_pg_allergy,
    nextActionId = null,   // not used with ViewPager2
    isFirst = false
) {

    private lateinit var adapter: AllergyCategoryAdapter
    private val categories = mutableListOf<AllergyCategory>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategories()
        adapter = AllergyCategoryAdapter(
            categories,
            onCategoryChecked = { pos, checked ->
                toggleCategory(pos, checked)
            },
            onSubChecked = { catPos, subPos, checked ->
                categories[catPos].subAllergies[subPos].isSelected = checked
                val allSelected = categories[catPos].subAllergies.all { it.isSelected }
                categories[catPos].isCategorySelected = allSelected
                adapter.notifyItemChanged(catPos)
            }
        )
        view.findViewById<RecyclerView>(R.id.rvAllergies).adapter = adapter

        loadSavedSelections()
    }

    // ✅ ADD THIS METHOD
    override fun onResume() {
        super.onResume()
        val hasAllergies = OnboardingStore.getBoolean(requireContext(), FlexiFitKeys.HAS_ALLERGIES)
        if (!hasAllergies) {
            (requireActivity() as OnboardingActivity).nextPage()
        }
    }

    private fun setupCategories() {
        categories.clear()

        // --- SEAFOODS ---
        categories.add(AllergyCategory("Seafood", mutableListOf(
            SubAllergy("Shrimp", "Shrimp"),
            SubAllergy("Crab", "Crab"),
            SubAllergy("Lobster", "Lobster"),
            SubAllergy("Prawns", "Prawns"),
            SubAllergy("Squid", "Squid"),
            SubAllergy("Mussels", "Mussels"),
            SubAllergy("Clams", "Clams"),
            SubAllergy("Tuna", "Tuna"),
            SubAllergy("Mackerel", "Mackerel"),
            SubAllergy("Tilapia", "Tilapia"),
            SubAllergy("Anchovies", "Anchovies"),
            SubAllergy("Fish", "Fish"),
            SubAllergy("Scallops", "Scallops"),
            SubAllergy("oysters", "Oysters")   // added
        )))

        // --- DAIRY & EGGS ---
        categories.add(AllergyCategory("Dairy & Eggs", mutableListOf(
            SubAllergy("Cheese", "Cheese"),
            SubAllergy("Yogurt", "Yogurt"),
            SubAllergy("Eggs", "Eggs"),
            SubAllergy("Cow's Milk", "Cow's Milk"),
            SubAllergy("Cream", "Cream"),
            SubAllergy("Butter", "Butter")
        )))

        // --- LEGUMES ---
        categories.add(AllergyCategory("Legumes", mutableListOf(
            SubAllergy("Peanuts", "Peanuts (Mani)"),
            SubAllergy("Soybeans", "Soybeans"),
            SubAllergy("Tofu", "Tofu / Tokwa"),
            SubAllergy("Miso", "Miso"),
            SubAllergy("Mung Beans (Monggo)", "Mung Beans (Monggo)"),
            SubAllergy("Beans", "Beans"),
            SubAllergy("Chickpeas", "Chickpeas")
        )))

        // --- TREE NUTS ---
        categories.add(AllergyCategory("Nuts", mutableListOf(
            SubAllergy("Cashews", "Cashews (Kasuoy)"),
            SubAllergy("Pili Nuts", "Pili Nuts"),
            SubAllergy("Almonds", "Almonds"),
            SubAllergy("Walnuts", "Walnuts"),
            SubAllergy("Hazelnuts", "Hazelnuts"),
            SubAllergy("Pistachio", "Pistachio"),
            SubAllergy("Pecans", "Pecans")
        )))

        // --- WHEAT ---
        categories.add(AllergyCategory("Wheat", mutableListOf(
            SubAllergy("Pandesal", "Pandesal"),
            SubAllergy("Cakes", "Cakes"),
            SubAllergy("Pastries", "Pastries"),
            SubAllergy("Crackers", "Crackers"),
            SubAllergy("Biscuits", "Biscuits"),
            SubAllergy("Noodles", "Noodles"),
            SubAllergy("Pasta", "Pasta"),
            SubAllergy("Lumpia Wrappers", "Lumpia Wrappers"),
            SubAllergy("Soy Sauce", "Soy Sauce"),
            SubAllergy("Gravy", "Gravy")
        )))
    }

    private fun loadSavedSelections() {
        val savedAllergies = OnboardingStore.getStringSet(requireContext(), FlexiFitKeys.ALLERGIES_LIST)
        if (savedAllergies.isNotEmpty()) {
            categories.forEach { category ->
                category.subAllergies.forEach { sub ->
                    sub.isSelected = savedAllergies.contains(sub.id)   // compare id
                }
                category.isCategorySelected = category.subAllergies.all { it.isSelected }
            }
            adapter.notifyDataSetChanged()
        }
    }

    private fun toggleCategory(pos: Int, checked: Boolean) {
        val category = categories[pos]
        category.isCategorySelected = checked
        category.subAllergies.forEach { it.isSelected = checked }
        adapter.notifyItemChanged(pos)
    }

    override fun validateBeforeNext(): String? {
        val hasAllergiesFlag = OnboardingStore.getBoolean(requireContext(), FlexiFitKeys.HAS_ALLERGIES)
        if (hasAllergiesFlag) {
            val selected = categories.flatMap { category ->
                category.subAllergies.filter { it.isSelected }.map { it.id }   // ← change to id
            }
            if (selected.isEmpty()) {
                return "Please select at least one allergy."
            }
        }
        return null
    }

    override fun onDestroyView() {
        // Save selections when leaving (so back navigation retains)
        val selectedAllergies = categories.flatMap { category ->
            category.subAllergies.filter { it.isSelected }.map { it.id }   // ← change to id
        }.toSet()
        OnboardingStore.putStringSet(requireContext(), FlexiFitKeys.ALLERGIES_LIST, selectedAllergies)
        super.onDestroyView()
    }
}