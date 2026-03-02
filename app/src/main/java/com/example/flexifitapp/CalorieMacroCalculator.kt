package com.example.flexifitapp

import kotlin.math.roundToInt

object CalorieMacroCalculator {

    data class Result(val calories: Int, val proteinG: Int, val carbsG: Int, val fatsG: Int)

    fun compute(
        age: Int,
        gender: String,      // "Male" / "Female"
        heightCm: Int,
        weightKg: Int,
        activity: String,    // "Sedentary" / "Lightly Active" / "Active" / "Very Active"
        goal: String         // "Lose" / "Maintain" / "Gain"
    ): Result {

        val bmr = if (gender.equals("Male", true)) {
            10 * weightKg + 6.25 * heightCm - 5 * age + 5
        } else {
            10 * weightKg + 6.25 * heightCm - 5 * age - 161
        }

        val mult = when (activity) {
            "Sedentary" -> 1.2
            "Lightly Active" -> 1.375
            "Active" -> 1.55
            "Very Active" -> 1.725
            else -> 1.2
        }

        var tdee = bmr * mult

        tdee = when (goal) {
            "Lose" -> tdee - 500
            "Gain" -> tdee + 350
            else -> tdee
        }

        val calories = tdee.roundToInt().coerceAtLeast(1200)

        // Macro split MVP
        val (pPct, cPct, fPct) = when (goal) {
            "Lose" -> Triple(0.35, 0.35, 0.30)
            "Gain" -> Triple(0.30, 0.45, 0.25)
            else -> Triple(0.30, 0.40, 0.30)
        }

        val proteinG = ((calories * pPct) / 4.0).roundToInt()
        val carbsG = ((calories * cPct) / 4.0).roundToInt()
        val fatsG = ((calories * fPct) / 9.0).roundToInt()

        return Result(calories, proteinG, carbsG, fatsG)
    }
}