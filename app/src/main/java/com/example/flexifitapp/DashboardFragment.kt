package com.example.flexifitapp

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.flexifitapp.custom.WaterGlassView
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    // ===== Dashboard Data (placeholder habang wala pang API) =====

    private var level = "Beginner"
    private var goal = "Muscle Gain"

    private var intakeCalories = 760
    private var burnedCalories = 100
    private var goalCalories = 2000

    private var workoutDayWeek = "Day 1 - Week 1"
    private var workoutStatus = "In Progress"
    private var workoutPrograms =
        "Beginner Outdoor Cardio\nBeginner Outdoor Strength Push"
    private var workoutNames =
        "Pushups, Pullups, Squats"

    private var nutritionStatus = "Planned"
    private var mealPlanType = "Gain Weight • Balanced Meal Plan"

    private var breakfast = "Oat Pancakes, Eggs"
    private var lunch = "Chicken Adobo, Rice"
    private var snacks = "Apple, Boiled Eggs"
    private var dinner = "Grilled Fish, Vegetables"

    private var mealTotalCalories = 2000

    private var waterCurrent = 0
    private val waterMax = 8

    // ===== Views =====

    private var txtLevel: TextView? = null
    private var txtGoal: TextView? = null

    private var txtCalorieIntake: TextView? = null
    private var txtCaloriesBurned: TextView? = null
    private var tvNetCalories: TextView? = null
    private var tvLeftCalories: TextView? = null
    private var progressRing: CircularProgressIndicator? = null

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

    // ===== Lifecycle =====

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindViews(view)

        setupWaterCard()

        renderDashboard()
    }

    // ===== Bind Views =====

    private fun bindViews(view: View) {

        txtLevel = view.findViewById(R.id.txtLevel)
        txtGoal = view.findViewById(R.id.txtGoal)

        txtCalorieIntake = view.findViewById(R.id.txtCalorieIntake)
        txtCaloriesBurned = view.findViewById(R.id.txtCaloriesBurned)

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

    // ===== Render Dashboard =====

    private fun renderDashboard() {

        bindTopCards()

        bindCalories()

        bindWorkoutSummary()

        bindNutritionSummary()

        renderWater()
    }

    // ===== Top Cards =====

    private fun bindTopCards() {

        txtLevel?.text = "Level: $level"

        txtGoal?.text = "Goal: $goal"
    }

    // ===== Calories =====

    private fun bindCalories() {

        val netCalories = intakeCalories - burnedCalories

        val remainingCalories = (goalCalories - intakeCalories)
            .coerceAtLeast(0)

        txtCalorieIntake?.text = "$intakeCalories kcal"

        txtCaloriesBurned?.text = "$burnedCalories kcal"

        tvNetCalories?.text = "$netCalories kcal"

        tvLeftCalories?.text = "$remainingCalories kcal\nleft"

        progressRing?.max = goalCalories.coerceAtLeast(1)

        progressRing?.setProgress(
            intakeCalories.coerceIn(0, goalCalories),
            true
        )
    }

    // ===== Workout Summary =====

    private fun bindWorkoutSummary() {

        txtDayWeek?.text = workoutDayWeek

        txtStatus?.text = workoutStatus

        txtPrograms?.text = workoutPrograms

        txtWorkouts?.text = workoutNames
    }

    // ===== Nutrition Summary =====

    private fun bindNutritionSummary() {

        txtNutriStatus?.text = nutritionStatus

        txtMealPlanType?.text = mealPlanType

        txtBreakfast?.text = breakfast

        txtLunch?.text = lunch

        txtSnacks?.text = snacks

        txtDinner?.text = dinner

        txtMealTotalCalories?.text = "$mealTotalCalories kcal"
    }

    // ===== Water Card =====

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

    // ===== Cleanup =====

    override fun onDestroyView() {

        txtLevel = null
        txtGoal = null

        txtCalorieIntake = null
        txtCaloriesBurned = null

        tvNetCalories = null
        tvLeftCalories = null

        progressRing = null

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