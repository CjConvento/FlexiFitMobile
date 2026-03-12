package com.example.flexifitapp

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flexifitapp.dashboard.GoalsAdapter
import com.example.flexifitapp.custom.WaterGlassView
import com.example.flexifitapp.UserPrefs
import com.example.flexifitapp.CalorieMacroCalculator
import com.example.flexifitapp.onboarding.FlexiFitKeys
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.progressindicator.LinearProgressIndicator

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    // ===== Dashboard Data (Clean Version) =====
    private var intakeCalories: Int = 0
    private var burnedCalories: Int = 0
    private var goalCalories: Int = 0

    private var workoutDayWeek: String = ""
    private var workoutStatus: String = ""
    private var workoutPrograms: String = ""
    private var workoutNames: String = ""

    private var nutritionStatus: String = ""
    private var mealPlanType: String = ""
    private var breakfast: String = ""
    private var lunch: String = ""
    private var snacks: String = ""
    private var dinner: String = ""
    private var mealTotalCalories: Int = 0
    private var waterCurrent = 0
    private val waterMax = 8

    // ===== Views =====
    private var txtLevel: TextView? = null
    private var imgLevelIcon: ImageView? = null
    private var rvGoals: RecyclerView? = null
    private var txtCalorieIntake: TextView? = null
    private var txtCaloriesBurned: TextView? = null
    private var tvNetCalories: TextView? = null
    private var tvLeftCalories: TextView? = null
    private var progressRing: CircularProgressIndicator? = null
    private var progressIntake: LinearProgressIndicator? = null
    private var progressBurned: LinearProgressIndicator? = null

    private var txtDayWeek: TextView? = null
    private var txtStatus: TextView? = null
    private var txtPrograms: TextView? = null
    private var txtWorkouts: TextView? = null
    private var txtNutriStatus: TextView? = null
    private var txtMealPlanType: TextView? = null
    private var txtBreakfast: TextView? = null
    private var txtLunch: TextView? = null
    private var txtSnacks: TextView? = null
    private var txtDinner: TextView? = null
    private var txtMealTotalCalories: TextView? = null
    private var txtWaterCount: TextView? = null
    private var btnAddWater: MaterialButton? = null
    private var waterGlass: WaterGlassView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setupWaterCard()
        renderDynamicUI(view)
    }

    private fun bindViews(view: View) {
        txtLevel = view.findViewById(R.id.txtLevel)
        imgLevelIcon = view.findViewById(R.id.imgLevelIcon)
        rvGoals = view.findViewById(R.id.rvGoals)
        txtCalorieIntake = view.findViewById(R.id.txtCalorieIntake)
        txtCaloriesBurned = view.findViewById(R.id.txtCaloriesBurned)
        progressIntake = view.findViewById(R.id.progressIntake)
        progressBurned = view.findViewById(R.id.progressBurned)
        tvNetCalories = view.findViewById(R.id.tvNetCalories)
        tvLeftCalories = view.findViewById(R.id.tvLeftCalories)
        progressRing = view.findViewById(R.id.progressRing)
        txtDayWeek = view.findViewById(R.id.txtDayWeek)
        txtStatus = view.findViewById(R.id.txtStatus)
        txtPrograms = view.findViewById(R.id.txtPrograms)
        txtWorkouts = view.findViewById(R.id.txtWorkouts)
        txtNutriStatus = view.findViewById(R.id.txt_nutriStatus)
        txtMealPlanType = view.findViewById(R.id.txtMealPlanType)
        txtBreakfast = view.findViewById(R.id.txtdshb_Bfast)
        txtLunch = view.findViewById(R.id.txtdshb_Lunch)
        txtSnacks = view.findViewById(R.id.txtdshb_Snacks)
        txtDinner = view.findViewById(R.id.txtdshb_Dinner)
        txtMealTotalCalories = view.findViewById(R.id.txtMealTotalCalories)
        txtWaterCount = view.findViewById(R.id.txtWaterCount)
        btnAddWater = view.findViewById(R.id.btnAddWater)
        waterGlass = view.findViewById(R.id.waterGlass)
    }

    private fun renderDynamicUI(view: View) {
        val ctx = requireContext()
        val age = UserPrefs.getInt(ctx, FlexiFitKeys.AGE)
        val gender = UserPrefs.getString(ctx, FlexiFitKeys.GENDER)
        val height = UserPrefs.getInt(ctx, FlexiFitKeys.HEIGHT_CM)
        val weight = UserPrefs.getInt(ctx, FlexiFitKeys.WEIGHT_KG)
        val userLevel = UserPrefs.getString(ctx, FlexiFitKeys.FITNESS_LEVEL)

        val goalsString = UserPrefs.getString(ctx, FlexiFitKeys.FITNESS_GOAL)
        val goalsList = goalsString.split(",").map { it.trim() }
        setupGoalsRecyclerView(goalsList)

        val primaryGoal = when {
            goalsList.contains("Muscle Gain") -> "Gain"
            goalsList.contains("Cardio") -> "Lose"
            else -> "Recovery" // Default para sa Rehab
        }

        val calcResult = CalorieMacroCalculator.compute(age, gender, height, weight, userLevel, primaryGoal)
        this.goalCalories = calcResult.calories

        txtLevel?.text = "Level: $userLevel"
        updateLevelIcon(userLevel)

        bindCalories()
        bindWorkoutSummary()
        bindNutritionSummary()
        renderWater()
    }

    private fun setupGoalsRecyclerView(goals: List<String>) {
        rvGoals?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvGoals?.adapter = GoalsAdapter(goals)
    }

    private fun updateLevelIcon(level: String) {
        when (level.lowercase()) {
            "beginner" -> {
                imgLevelIcon?.setColorFilter(Color.parseColor("#4CAF50"))
            }
            "intermediate" -> {
                imgLevelIcon?.setColorFilter(Color.parseColor("#FF9800"))
            }
            "advanced" -> {
                imgLevelIcon?.setColorFilter(Color.parseColor("#F44336"))
            }
            "rehab level" -> {
                imgLevelIcon?.setColorFilter(Color.parseColor("#2196F3"))
            }
        }
    }

    private fun bindCalories() {
        val netCalories = intakeCalories - burnedCalories
        val remaining = (goalCalories - intakeCalories).coerceAtLeast(0)
        txtCalorieIntake?.text = "$intakeCalories / $goalCalories kcal"
        txtCaloriesBurned?.text = "$burnedCalories kcal"
        tvNetCalories?.text = "$netCalories kcal"
        tvLeftCalories?.text = "$remaining kcal\nleft"
        progressRing?.max = goalCalories.coerceAtLeast(1)
        progressRing?.setProgress(intakeCalories.coerceIn(0, goalCalories), true)
        progressIntake?.max = goalCalories
        progressIntake?.setProgress(intakeCalories, true)
    }

    private fun bindWorkoutSummary() {
        txtDayWeek?.text = workoutDayWeek
        txtStatus?.text = workoutStatus
        txtPrograms?.text = workoutPrograms
        txtWorkouts?.text = workoutNames
    }

    private fun bindNutritionSummary() {
        txtNutriStatus?.text = nutritionStatus
        txtMealPlanType?.text = mealPlanType
        txtBreakfast?.text = breakfast
        txtLunch?.text = lunch
        txtSnacks?.text = snacks
        txtDinner?.text = dinner
        txtMealTotalCalories?.text = "$mealTotalCalories kcal"
    }

    private fun setupWaterCard() {
        waterGlass?.setMaxGlasses(waterMax)
        btnAddWater?.setOnClickListener {
            if (waterCurrent < waterMax) {
                waterCurrent++
                renderWater()
            }
        }
    }

    private fun renderWater() {
        waterGlass?.setCurrentGlasses(waterCurrent)
        txtWaterCount?.text = "$waterCurrent/$waterMax"
    }

    override fun onDestroyView() {
        // Cleanup all views
        txtLevel = null
        imgLevelIcon = null
        rvGoals = null
        txtCalorieIntake = null
        txtCaloriesBurned = null
        tvNetCalories = null
        tvLeftCalories = null
        progressRing = null
        progressIntake = null
        progressBurned = null
        txtDayWeek = null
        txtStatus = null
        txtPrograms = null
        txtWorkouts = null
        txtNutriStatus = null
        txtMealPlanType = null
        txtBreakfast = null
        txtLunch = null
        txtSnacks = null
        txtDinner = null
        txtMealTotalCalories = null
        txtWaterCount = null
        btnAddWater = null
        waterGlass = null
        super.onDestroyView()
    }
}