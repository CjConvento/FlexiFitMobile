package com.example.flexifitapp

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class NutritionTabRootFragment : Fragment(R.layout.fragment_nutri) {

    private lateinit var sectionAdapter: MealSectionAdapter
    private val sections = mutableListOf<MealSection>()

    companion object {
        private const val REQ_FOOD_EDIT = "req_food_edit"
        private const val KEY_MEAL_ITEM_ID = "mealItemId"
        private const val KEY_QTY = "qty"
        private const val KEY_SERVING_LABEL = "servingLabel"
        private const val KEY_CALORIES = "calories"
        private const val KEY_PROTEIN = "protein"
        private const val KEY_CARBS = "carbs"
        private const val KEY_FATS = "fats"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCalendarButton(view)
        setupMealPlanRecyclerView(view)
        setupFoodEditResultListener()
        updateTotalsFromSections(view)
    }

    private fun setupCalendarButton(view: View) {
        view.findViewById<ImageButton>(R.id.btnOpenCalendar).setOnClickListener {
            val b = bundleOf(NavKeys.ARG_SOURCE_TAB to "NUTRITION")
            findNavController().navigate(R.id.unifiedCalendarFragment, b)
        }
    }

    private fun setupMealPlanRecyclerView(view: View) {
        val rv = view.findViewById<RecyclerView>(R.id.rvMealSections)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.isNestedScrollingEnabled = false

        loadDemoSections()

        sectionAdapter = MealSectionAdapter(sections) { mealType, food ->
            val b = bundleOf(
                KEY_MEAL_ITEM_ID to food.mealItemId,
                "foodId" to food.foodId,
                "mealType" to mealType,
                KEY_QTY to food.qty,
                KEY_SERVING_LABEL to food.servingLabel,
                KEY_CALORIES to food.calories,
                KEY_PROTEIN to food.protein,
                KEY_CARBS to food.carbs,
                KEY_FATS to food.fats
            )
            findNavController().navigate(R.id.foodDetailsFragment, b)
        }

        rv.adapter = sectionAdapter
    }

    private fun loadDemoSections() {
        sections.clear()

        sections.add(
            MealSection(
                "Breakfast",
                mutableListOf(
                    MealFood(
                        mealItemId = 101,
                        foodId = 1,
                        name = "Oatmeal Pancakes",
                        imageUrl = null,
                        servingLabel = "1 serving (2 pancakes)",
                        qty = 1,
                        calories = 180,
                        protein = 10,
                        carbs = 25,
                        fats = 6
                    )
                )
            )
        )

        sections.add(
            MealSection(
                "Lunch",
                mutableListOf(
                    MealFood(
                        mealItemId = 102,
                        foodId = 2,
                        name = "Chicken Rice",
                        imageUrl = null,
                        servingLabel = "1 plate",
                        qty = 1,
                        calories = 450,
                        protein = 35,
                        carbs = 55,
                        fats = 10
                    )
                )
            )
        )

        sections.add(
            MealSection(
                "Snacks",
                mutableListOf()
            )
        )

        sections.add(
            MealSection(
                "Dinner",
                mutableListOf()
            )
        )
    }

    private fun setupFoodEditResultListener() {
        parentFragmentManager.setFragmentResultListener(
            REQ_FOOD_EDIT,
            viewLifecycleOwner
        ) { _, bundle ->

            val mealItemId = bundle.getInt(KEY_MEAL_ITEM_ID)
            val qty = bundle.getInt(KEY_QTY)
            val servingLabel = bundle.getString(KEY_SERVING_LABEL).orEmpty()
            val calories = bundle.getInt(KEY_CALORIES)
            val protein = bundle.getInt(KEY_PROTEIN)
            val carbs = bundle.getInt(KEY_CARBS)
            val fats = bundle.getInt(KEY_FATS)

            val item = sections
                .flatMap { it.foods }
                .firstOrNull { it.mealItemId == mealItemId }

            if (item != null) {
                item.qty = qty
                item.servingLabel = servingLabel
                item.calories = calories
                item.protein = protein
                item.carbs = carbs
                item.fats = fats

                sectionAdapter.notifyDataSetChanged()

                view?.let { root ->
                    updateTotalsFromSections(root)
                }
            }
        }
    }

    private fun updateTotalsFromSections(root: View) {
        val allFoods = sections.flatMap { it.foods }

        val caloriesConsumed = allFoods.sumOf { it.calories * it.qty }
        val totalProtein = allFoods.sumOf { it.protein * it.qty }
        val totalCarbs = allFoods.sumOf { it.carbs * it.qty }
        val totalFats = allFoods.sumOf { it.fats * it.qty }

        // Placeholder muna habang wala pang real workout/progress API
        val caloriesBurned = 320

        // TODO: replace these with actual targets from onboarding/profile/API
        val targetCal = 2200
        val targetProtein = 120
        val targetCarbs = 220
        val targetFats = 80

        // right-side summary
        root.findViewById<TextView>(R.id.tvCaloriesBurned).text =
            "Calories Burned: ${caloriesBurned} kcal"

        // intake vs target
        root.findViewById<TextView>(R.id.tvProteinValue).text =
            "${totalProtein}g / ${targetProtein}g"

        root.findViewById<TextView>(R.id.tvCarbsValue).text =
            "${totalCarbs}g / ${targetCarbs}g"

        root.findViewById<TextView>(R.id.tvFatsValue).text =
            "${totalFats}g / ${targetFats}g"

        root.findViewById<TextView>(R.id.tvCaloriesValue).text =
            "${caloriesConsumed} / ${targetCal} kcal"

        // progress bars
        root.findViewById<ProgressBar>(R.id.pbProtein).progress =
            percent(totalProtein, targetProtein)

        root.findViewById<ProgressBar>(R.id.pbCarbs).progress =
            percent(totalCarbs, targetCarbs)

        root.findViewById<ProgressBar>(R.id.pbFats).progress =
            percent(totalFats, targetFats)

        root.findViewById<ProgressBar>(R.id.pbCalories).progress =
            percent(caloriesConsumed, targetCal)
    }

    private fun percent(value: Int, target: Int): Int {
        if (target <= 0) return 0
        return ((value * 100f) / target).toInt().coerceIn(0, 100)
    }
}