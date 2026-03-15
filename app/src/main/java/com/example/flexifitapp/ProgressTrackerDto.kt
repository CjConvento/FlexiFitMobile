package com.example.flexifitapp

data class ProgressTrackerDto(
    val compliancePercentage: Double,
    val complianceSessions: String,
    val avgCalories: Int,
    val avgWaterIntake: Double,
    val mealsCompleted: Int,
    val totalMeals: Int,
    val currentStreak: Int,
    val currentWeight: Double,
    val weightChange: Double,

    // Para sa mga Graphs natin babe
    val weightHistory: List<ChartEntryDto>,
    val calorieHistory: List<ChartEntryDto>
)

data class ChartEntryDto(
    val label: String, // e.g., "Mon", "W1"
    val value: Float
)