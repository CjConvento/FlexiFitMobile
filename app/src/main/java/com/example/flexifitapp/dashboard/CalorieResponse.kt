package com.example.flexifitapp.dashboard // Siguraduhin na tama ang package name mo

data class CalorieResponse(
    val bmi: Double,
    val tdee: Double,
    val calorieTarget: Double,
    val macroPercents: MacroPercents,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double
)

data class MacroPercents(
    val protein: Double,
    val carbs: Double,
    val fat: Double
)