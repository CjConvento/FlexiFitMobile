package com.example.flexifitapp

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flexifitapp.nutri.*  // ✅ This imports everything including LogMealEntry
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.time.LocalDate

class NutritionTabRootFragment : Fragment(R.layout.fragment_nutri) {

    private var day: Int = -1
    private var fromHost: Boolean = false
    private var monthArg: Int = 1
    private var currentCycleId: Int = 0
    private var currentNutritionResponse: NutritionResponse? = null

    // UI Components
    private lateinit var btnBack: ImageButton
    private lateinit var btnCalendar: ImageButton
    private lateinit var btnComplete: MaterialButton
    private lateinit var btnSkip: MaterialButton
    private lateinit var btnRetry: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var rvMeals: RecyclerView
    private lateinit var tvCalories: TextView
    private lateinit var tvCaloriesBurned: TextView
    private lateinit var tvProtein: TextView
    private lateinit var tvCarbs: TextView
    private lateinit var tvFats: TextView
    private lateinit var pbCalories: ProgressBar
    private lateinit var pbProtein: ProgressBar
    private lateinit var pbCarbs: ProgressBar
    private lateinit var pbFats: ProgressBar
    private lateinit var tvMacrosTag: TextView

    // CHART
    private lateinit var nutritionPieChart: PieChart

    // ✅ ADD WATER UI COMPONENTS
    private lateinit var btnAddWater: MaterialButton
    private lateinit var btnResetWater: MaterialButton
    private lateinit var tvWaterValue: TextView
    // private lateinit var waterGlass: WaterGlassView // Uncomment if you have this custom view

    private lateinit var mealAdapter: MealSectionAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        readArgs()
        initViews(view)
        setupButtons()
        setupRecyclerView()
        setupWaterButtons()  // ✅ Add this

        if (fromHost && day > 0) {
            fetchNutritionByDate()
        } else {
            fetchTodayNutrition()
        }
    }

    private fun readArgs() {
        day = arguments?.getInt(NavKeys.ARG_DAY, -1) ?: -1
        monthArg = arguments?.getInt(NavKeys.ARG_MONTH, 1) ?: 1
        fromHost = arguments?.getBoolean(NavKeys.ARG_FROM_HOST, false) ?: false
    }

    private fun initViews(view: View) {
        btnBack = view.findViewById(R.id.btnBackNutrition)
        btnCalendar = view.findViewById(R.id.btnOpenCalendar)
        btnComplete = view.findViewById(R.id.btnCompleteNutritionDay)
        btnSkip = view.findViewById(R.id.btnSkipNutritionDay)
        btnRetry = view.findViewById(R.id.btnRetryNutrition)
        progressBar = view.findViewById(R.id.progressNutritionLoading)
        tvError = view.findViewById(R.id.tvNutritionError)
        rvMeals = view.findViewById(R.id.rvMealSections)
        tvMacrosTag = view.findViewById(R.id.tvMacrosTag)

        nutritionPieChart = view.findViewById(R.id.nutritionPieChart)

        tvCalories = view.findViewById(R.id.tvCaloriesValue)
        tvCaloriesBurned = view.findViewById(R.id.tvCaloriesBurned)
        tvProtein = view.findViewById(R.id.tvProteinValue)
        tvCarbs = view.findViewById(R.id.tvCarbsValue)
        tvFats = view.findViewById(R.id.tvFatsValue)

        pbCalories = view.findViewById(R.id.pbCalories)
        pbProtein = view.findViewById(R.id.pbProtein)
        pbCarbs = view.findViewById(R.id.pbCarbs)
        pbFats = view.findViewById(R.id.pbFats)

        // ✅ INIT WATER UI COMPONENTS
        btnAddWater = view.findViewById(R.id.btnAddWater)
        btnResetWater = view.findViewById(R.id.btnResetWater)
        tvWaterValue = view.findViewById(R.id.tvWaterValue)
        // waterGlass = view.findViewById(R.id.waterGlass)

            btnCalendar.isVisible = !fromHost
        Log.d("NUTRITION_TAB", "Calendar button visibility set to: ${btnCalendar.isVisible}, fromHost=$fromHost")
    }

    private fun setupButtons() {
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        btnCalendar.setOnClickListener {
            if (!fromHost) {
                val bundle = bundleOf(
                    NavKeys.ARG_SOURCE_TAB to "NUTRITION",
                    NavKeys.ARG_MONTH to monthArg
                )
                findNavController().navigate(R.id.action_nutritionTabRootFragment_to_unifiedCalendarFragment, bundle)
            } else {
                Log.d("NUTRITION_TAB", "Calendar button clicked but fragment is in host mode – ignoring.")
            }
        }

        btnComplete.setOnClickListener {
            completeNutritionDay()
        }

        btnSkip.setOnClickListener {
            Toast.makeText(requireContext(), "Skip functionality coming soon", Toast.LENGTH_SHORT).show()
        }

        btnRetry.setOnClickListener {
            if (fromHost) {
                fetchNutritionByDate()
            } else {
                fetchTodayNutrition()
            }
        }
    }

    private fun setupRecyclerView() {
        rvMeals.layoutManager = LinearLayoutManager(requireContext())
        mealAdapter = MealSectionAdapter(mutableListOf()) { mealType, food ->
            openFoodDetail(food)
        }
        rvMeals.adapter = mealAdapter
    }

    private fun updateMacroPieChart(proteinG: Double, carbsG: Double, fatsG: Double) {
        val entries = ArrayList<PieEntry>()

        if (proteinG > 0) entries.add(PieEntry(proteinG.toFloat(), "Protein"))
        if (carbsG > 0) entries.add(PieEntry(carbsG.toFloat(), "Carbs"))
        if (fatsG > 0) entries.add(PieEntry(fatsG.toFloat(), "Fats"))

        // If all zero, show a placeholder "No Data" slice
        if (entries.isEmpty()) {
            entries.add(PieEntry(1f, "No Data"))
        }

        val dataSet = PieDataSet(entries, "Macros")
        dataSet.colors = listOf(
            resources.getColor(R.color.gradientEnd, null),
            resources.getColor(R.color.gradientStart, null),
            resources.getColor(R.color.gradientStart_alt, null),
            resources.getColor(R.color.gradientEnd_alt, null)
        )
        dataSet.valueTextColor = resources.getColor(R.color.white, null)
        dataSet.valueTextSize = 12f
        dataSet.valueLinePart1Length = 0.5f
        dataSet.valueLinePart2Length = 0.5f
        dataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE

        val data = PieData(dataSet)
        nutritionPieChart.data = data
        nutritionPieChart.description.isEnabled = false
        nutritionPieChart.isDrawHoleEnabled = true
        nutritionPieChart.holeRadius = 40f
        nutritionPieChart.transparentCircleRadius = 45f
        nutritionPieChart.setUsePercentValues(true)
        nutritionPieChart.legend.isEnabled = true
        nutritionPieChart.legend.textColor = resources.getColor(R.color.textPrimary, null)
        nutritionPieChart.invalidate()
    }

    private fun openFoodDetail(food: MealFood) {
        val bundle = bundleOf(
            "mealItemId" to food.mealItemId,
            "foodId" to food.foodId,
            "name" to food.name,
            "description" to food.description,
            "imageUrl" to food.imageUrl,
            "servingLabel" to food.servingLabel,
            "qty" to food.qty,
            "calories" to food.calories.toDouble(),
            "protein" to food.protein.toDouble(),
            "carbs" to food.carbs.toDouble(),
            "fats" to food.fats.toDouble()
        )
        findNavController().navigate(R.id.action_nutritionTabRootFragment_to_foodDetailsFragment, bundle)
    }

    private fun fetchTodayNutrition() {
        lifecycleScope.launch {
            showLoading()
            try {
                val api = ApiClient.api()
                val repository = NutritionRepository(api)
                val response = repository.getTodayNutrition()

                if (response != null) {
                    currentNutritionResponse = response
                    currentCycleId = response.dailyLogId
                    updateUI(response)
                    showContent()
                } else {
                    showError("No nutrition data found")
                }
            } catch (e: Exception) {
                showError("Connection error: ${e.message}")
            }
        }
    }

    private fun fetchNutritionByDate() {
        lifecycleScope.launch {
            showLoading()
            try {
                val api = ApiClient.api()
                val repository = NutritionRepository(api)
                val response = repository.getNutritionByDate(day, monthArg)

                if (response != null) {
                    currentNutritionResponse = response
                    currentCycleId = response.dailyLogId
                    updateUI(response)
                    showContent()

                    if (day < LocalDate.now().dayOfMonth) {
                        btnComplete.isEnabled = false
                        btnComplete.text = "Completed"
                        btnSkip.isEnabled = false
                    }
                } else {
                    showError("No nutrition data for Day $day")
                }
            } catch (e: Exception) {
                showError("Connection error: ${e.message}")
            }
        }
    }

    private fun completeNutritionDay() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.api()
                val repository = NutritionRepository(api)

                val meals = currentNutritionResponse?.meals?.map { meal ->
                    com.example.flexifitapp.nutri.LogMealEntry(  // ✅ Fully qualified name
                        mealType = meal.mealType,
                        totalCalories = meal.foodItems.sumOf { it.calories },
                        totalProtein = meal.foodItems.sumOf { it.protein },
                        totalCarbs = meal.foodItems.sumOf { it.carbs },
                        totalFats = meal.foodItems.sumOf { it.fats }
                    )
                } ?: emptyList()

                val result = repository.completeNutritionDay(
                    cycleId = currentCycleId,
                    meals = meals
                )

                if (result != null) {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                    if (fromHost) {
                        fetchNutritionByDate()
                    } else {
                        findNavController().popBackStack()
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to complete nutrition", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUI(response: NutritionResponse) {
        tvMacrosTag.text = "Day ${response.dailyLogId}"
        tvCaloriesBurned.text = "${response.burnedCalories.toInt()} kcal"

        tvCalories.text = "${response.consumedCalories.toInt()} / ${response.targetCalories.toInt()} kcal"
        tvProtein.text = "${response.consumedProtein.toInt()}g / ${response.targetProtein.toInt()}g"
        tvCarbs.text = "${response.consumedCarbs.toInt()}g / ${response.targetCarbs.toInt()}g"
        tvFats.text = "${response.consumedFats.toInt()}g / ${response.targetFats.toInt()}g"

        updateMacroPieChart(response.consumedProtein, response.consumedCarbs, response.consumedFats)

        pbCalories.progress = percent(response.consumedCalories.toInt(), response.targetCalories.toInt())
        pbProtein.progress = percent(response.consumedProtein.toInt(), response.targetProtein.toInt())
        pbCarbs.progress = percent(response.consumedCarbs.toInt(), response.targetCarbs.toInt())
        pbFats.progress = percent(response.consumedFats.toInt(), response.targetFats.toInt())

        val sections = response.meals.map { meal ->
            MealSection(
                mealType = getMealTypeName(meal.mealType),
                foods = meal.foodItems.map { food ->
                    MealFood(
                        mealItemId = food.foodId,
                        foodId = food.foodId,
                        name = food.name,
                        description = food.description,
                        imageUrl = food.imageUrl,
                        servingLabel = "${food.qty} ${food.unit}",
                        qty = food.qty.toInt(),
                        calories = food.calories.toInt(),
                        protein = food.protein.toInt(),
                        carbs = food.carbs.toInt(),
                        fats = food.fats.toInt()
                    )
                }.toMutableList(),
                expanded = true
            )
        }.toMutableList()

        mealAdapter.updateData(sections)

        val allCompleted = response.meals.all { it.status == "DONE" }
        if (allCompleted) {
            btnComplete.isEnabled = false
            btnComplete.text = "Completed"
            btnSkip.isEnabled = false
        }

        loadWaterIntake()
    }

    private fun getMealTypeName(mealType: String): String {
        return when (mealType) {
            "B" -> "Breakfast"
            "L" -> "Lunch"
            "S" -> "Snacks"
            "D" -> "Dinner"
            else -> "Meal"
        }
    }

    private fun percent(value: Int, target: Int): Int {
        if (target <= 0) return 0
        return ((value * 100f) / target).toInt().coerceIn(0, 100)
    }

    private fun showLoading() {
        progressBar.isVisible = true
        rvMeals.isVisible = false
        tvError.isVisible = false
        btnRetry.isVisible = false
        btnComplete.isEnabled = false
        btnSkip.isEnabled = false
    }

    private fun showContent() {
        progressBar.isVisible = false
        rvMeals.isVisible = true
        tvError.isVisible = false
        btnRetry.isVisible = false
        btnComplete.isEnabled = true
        btnSkip.isEnabled = true
    }

    private fun showError(message: String) {
        progressBar.isVisible = false
        rvMeals.isVisible = false
        tvError.isVisible = true
        tvError.text = message
        btnRetry.isVisible = true
        btnComplete.isEnabled = false
        btnSkip.isEnabled = false
    }

    // ✅ WATER TRACKING METHODS
    private fun setupWaterButtons() {
        btnAddWater.setOnClickListener {
            addWater()
        }

        btnResetWater.setOnClickListener {
            resetWater()
        }
    }

    private fun loadWaterIntake() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.api()
                val repository = NutritionRepository(api)
                val water = repository.getWaterToday()

                water?.let {
                    val glasses = it.waterMl / 250
                    tvWaterValue.text = "$glasses/8"
                }
            } catch (e: Exception) {
                Log.e("NUTRITION", "Error loading water: ${e.message}")
            }
        }
    }

    private fun addWater() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.api()
                val repository = NutritionRepository(api)
                val result = repository.addWater(250)

                if (result != null) {
                    val glasses = result.waterMl / 250
                    tvWaterValue.text = "$glasses/8"
                    Toast.makeText(requireContext(), "Added 250ml water", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to add water", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun resetWater() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.api()
                val repository = NutritionRepository(api)
                val success = repository.resetWater()

                if (success) {
                    tvWaterValue.text = "0/8"
                    Toast.makeText(requireContext(), "Water reset", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to reset water", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}