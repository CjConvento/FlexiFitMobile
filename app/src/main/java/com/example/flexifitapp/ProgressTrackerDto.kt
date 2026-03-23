package com.example.flexifitapp

import com.google.gson.annotations.SerializedName

data class ProgressTrackerDto(
    @SerializedName("compliancePercentage")
    val compliancePercentage: Double,

    @SerializedName("complianceSessions")
    val complianceSessions: String,

    @SerializedName("avgCalories")
    val avgCalories: Int,

    @SerializedName("avgWaterIntake")
    val avgWaterIntake: Double,

    @SerializedName("mealsCompleted")
    val mealsCompleted: Int,

    @SerializedName("totalMeals")
    val totalMeals: Int,

    @SerializedName("currentStreak")
    val currentStreak: Int,

    @SerializedName("currentWeight")
    val currentWeight: Double,

    @SerializedName("weightChange")
    val weightChange: Double,

    @SerializedName("weightHistory")
    val weightHistory: List<ChartEntryDto>,

    @SerializedName("calorieHistory")
    val calorieHistory: List<ChartEntryDto>
)

data class ChartEntryDto(
    @SerializedName("label")
    val label: String,

    @SerializedName("value")
    val value: Float
)