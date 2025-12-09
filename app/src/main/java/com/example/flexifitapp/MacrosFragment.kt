package com.example.flexifitapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import java.util.Locale

class MacrosFragment : Fragment() {

    // ==== UI refs ====
    private lateinit var tvDailyCalories: TextView
    private lateinit var tvDailyStatus: TextView

    private lateinit var tvProteinValue: TextView
    private lateinit var tvCarbsValue: TextView
    private lateinit var tvFatsValue: TextView
    private lateinit var tvKcalValue: TextView

    private lateinit var progressProtein: ProgressBar
    private lateinit var progressCarbs: ProgressBar
    private lateinit var progressFats: ProgressBar
    private lateinit var progressKcal: ProgressBar

    private lateinit var btnUpdateIntake: MaterialButton
    private lateinit var btnViewMealPlan: MaterialButton
    private lateinit var tvMealPlanOutput: TextView

    // ==== Demo data (TODO: palitan from Firebase / calculations) ====
    private var caloriesTarget = 1920.0
    private var proteinTarget = 120.0   // g
    private var carbsTarget = 225.0     // g
    private var fatsTarget = 60.0       // g

    private var caloriesToday = 1695.0
    private var proteinToday = 120.0
    private var carbsToday = 180.0
    private var fatsToday = 55.0

    // Workout vs rest day flag (TODO: set based on schedule)
    private var isWorkoutToday = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_macros, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        // TODO: dito mag-load from Firebase then i-update ang values
        updateSummaryTexts()
        updateMacroUI()
        setupListeners()
    }

    private fun initViews(view: View) {
        tvDailyCalories = view.findViewById(R.id.tv_daily_calories)
        tvDailyStatus = view.findViewById(R.id.tv_daily_status)

        tvProteinValue = view.findViewById(R.id.tv_protein_value)
        tvCarbsValue = view.findViewById(R.id.tv_carbs_value)
        tvFatsValue = view.findViewById(R.id.tv_fats_value)
        tvKcalValue = view.findViewById(R.id.tv_kcal_value)

        progressProtein = view.findViewById(R.id.progress_protein)
        progressCarbs = view.findViewById(R.id.progress_carbs)
        progressFats = view.findViewById(R.id.progress_fats)
        progressKcal = view.findViewById(R.id.progress_kcal)

        btnUpdateIntake = view.findViewById(R.id.btn_update_intake)
        btnViewMealPlan = view.findViewById(R.id.btn_view_meal_plan)
        tvMealPlanOutput = view.findViewById(R.id.tv_meal_plan_output)
    }

    private fun setupListeners() {
        btnUpdateIntake.setOnClickListener {
            Toast.makeText(requireContext(),
                "TODO: Open intake logging screen.",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnViewMealPlan.setOnClickListener {
            generateMealPlan()
        }
    }

    // ====== SUMMARY + MACROS UI ======

    private fun updateSummaryTexts() {
        tvDailyCalories.text = String.format(
            Locale.getDefault(),
            "%.0f / %.0f kcal",
            caloriesToday,
            caloriesTarget
        )

        val diff = caloriesToday - caloriesTarget
        tvDailyStatus.text = if (isWorkoutToday) {
            when {
                diff < -100 -> "Workout day: you are slightly under your calorie goal. Good for fat loss, but watch performance."
                diff > 100 -> "Workout day: you’re above your calorie goal. Good for muscle gain if planned."
                else -> "Workout day: you are near your calorie goal. Nice balance for performance and progress."
            }
        } else {
            when {
                diff < -100 -> "Rest day: you are under your calorie goal. This supports fat loss."
                diff > 100 -> "Rest day: you are slightly above your goal. Reduce snacking to stay on track."
                else -> "Rest day: you are close to your calorie goal. Maintain consistency."
            }
        }
    }

    private fun updateMacroUI() {
        tvProteinValue.text = formatMacro(proteinToday, proteinTarget, "g")
        tvCarbsValue.text = formatMacro(carbsToday, carbsTarget, "g")
        tvFatsValue.text = formatMacro(fatsToday, fatsTarget, "g")
        tvKcalValue.text = formatMacro(caloriesToday, caloriesTarget, "kcal")

        progressProtein.progress = calcPercent(proteinToday, proteinTarget)
        progressCarbs.progress = calcPercent(carbsToday, carbsTarget)
        progressFats.progress = calcPercent(fatsToday, fatsTarget)
        progressKcal.progress = calcPercent(caloriesToday, caloriesTarget)
    }

    private fun formatMacro(value: Double, target: Double, unit: String): String {
        return String.format(
            Locale.getDefault(),
            "%.0f %s / %.0f %s",
            value, unit, target, unit
        )
    }

    private fun calcPercent(value: Double, target: Double): Int {
        if (target <= 0) return 0
        var pct = (value / target) * 100.0
        if (pct < 0) pct = 0.0
        if (pct > 100) pct = 100.0
        return pct.toInt()
    }

    // ====== MEAL PLAN ======

    private fun generateMealPlan() {
        if (caloriesTarget <= 0 || proteinTarget <= 0 ||
            carbsTarget <= 0 || fatsTarget <= 0) {

            Toast.makeText(
                requireContext(),
                "No macro targets set. Complete your profile first.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // 1) adjust carbs based on workout day
        val carbFactor = if (isWorkoutToday) 1.10 else 0.90

        val proteinKcal = proteinTarget * 4
        val fatsKcal = fatsTarget * 9
        var remainingKcal = caloriesTarget - (proteinKcal + fatsKcal)
        if (remainingKcal < 0) remainingKcal = caloriesTarget * 0.40

        val carbsKcal = remainingKcal * carbFactor
        val carbsPlanTarget = carbsKcal / 4.0   // not displayed directly but part of logic
        // (pwede nyo itong ilagay sa documentation bilang rule-based adjustment)

        // 2) split calories per meal
        val breakfastCal = caloriesTarget * 0.25
        val lunchCal = caloriesTarget * 0.30
        val dinnerCal = caloriesTarget * 0.30
        val snackCal = caloriesTarget * 0.15

        val plan = StringBuilder()

        plan.append("Today’s Suggested Meal Plan\n")
        if (isWorkoutToday) {
            plan.append("(Workout day: slightly higher carbs to support performance.)\n\n")
        } else {
            plan.append("(Rest day: slightly lower carbs, focus on protein and healthy fats.)\n\n")
        }

        plan.append(buildBreakfast(breakfastCal, isWorkoutToday))
        plan.append("\n")
        plan.append(buildLunch(lunchCal, isWorkoutToday))
        plan.append("\n")
        plan.append(buildDinner(dinnerCal, isWorkoutToday))
        plan.append("\n")
        plan.append(buildSnack(snackCal, isWorkoutToday))

        tvMealPlanOutput.text = plan.toString()
    }

    private fun buildBreakfast(kcal: Double, workoutDay: Boolean): String {
        val sb = StringBuilder()
        sb.append(String.format(Locale.getDefault(), "Breakfast (≈%.0f kcal)\n", kcal))
        if (workoutDay) {
            sb.append("• Oatmeal – 60 g (boiled)\n")
            sb.append("• 1 medium banana\n")
            sb.append("• 2 whole eggs (pan-fried with minimal oil)\n")
            sb.append("≈ P 20g  C 60g  F 12g\n")
        } else {
            sb.append("• Greek yogurt – 150 g (plain)\n")
            sb.append("• 10–15 almonds (raw)\n")
            sb.append("• 1 small apple\n")
            sb.append("≈ P 18g  C 30g  F 10g\n")
        }
        return sb.toString()
    }

    private fun buildLunch(kcal: Double, workoutDay: Boolean): String {
        val sb = StringBuilder()
        sb.append(String.format(Locale.getDefault(), "Lunch (≈%.0f kcal)\n", kcal))
        if (workoutDay) {
            sb.append("• Grilled chicken breast – 150 g\n")
            sb.append("• Brown rice – 120 g (steamed)\n")
            sb.append("• Mixed vegetables – 1 cup (steamed)\n")
            sb.append("≈ P 35g  C 55g  F 8g\n")
        } else {
            sb.append("• Grilled chicken breast – 130 g\n")
            sb.append("• Mixed salad – 1.5 cups\n")
            sb.append("• Olive oil dressing – 1 tbsp\n")
            sb.append("≈ P 30g  C 20g  F 12g\n")
        }
        return sb.toString()
    }

    private fun buildDinner(kcal: Double, workoutDay: Boolean): String {
        val sb = StringBuilder()
        sb.append(String.format(Locale.getDefault(), "Dinner (≈%.0f kcal)\n", kcal))
        if (workoutDay) {
            sb.append("• Baked fish fillet – 140 g\n")
            sb.append("• Quinoa or brown rice – 100 g (boiled)\n")
            sb.append("• Steamed broccoli – 1 cup\n")
            sb.append("≈ P 32g  C 45g  F 7g\n")
        } else {
            sb.append("• Baked fish fillet – 150 g\n")
            sb.append("• Steamed vegetables – 1.5 cups\n")
            sb.append("• Sweet potato – 80 g (boiled)\n")
            sb.append("≈ P 30g  C 30g  F 6g\n")
        }
        return sb.toString()
    }

    private fun buildSnack(kcal: Double, workoutDay: Boolean): String {
        val sb = StringBuilder()
        sb.append(String.format(Locale.getDefault(), "Snack (≈%.0f kcal)\n", kcal))
        if (workoutDay) {
            sb.append("• 1 scoop whey protein (with water)\n")
            sb.append("• 2 rice cakes\n")
            sb.append("≈ P 25g  C 22g  F 2g\n")
        } else {
            sb.append("• Cottage cheese – 100 g\n")
            sb.append("• 1 small pear or apple\n")
            sb.append("≈ P 18g  C 20g  F 4g\n")
        }
        return sb.toString()
    }
}
