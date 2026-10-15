package com.example.flexifitapp

import com.google.gson.annotations.SerializedName

data class ProgressTrackerDto(
    // ⚖️ Weight Metrics
    @SerializedName("currentWeight") val currentWeight: Double = 0.0,
    @SerializedName("weightChange") val weightChange: Double = 0.0,
    @SerializedName("weightHistory") val weightHistory: List<ChartEntryDto> = emptyList(),

    // 🏋️ Workout Metrics
    @SerializedName("currentStreak") val currentStreak: Int = 0,
    @SerializedName("complianceSessions") val complianceSessions: String = "0/0",
    @SerializedName("totalWorkoutDays") val totalWorkoutDays: Int = 0,             // ✅ IDINAGDAG
    @SerializedName("completedWorkoutDays") val completedWorkoutDays: Int = 0,     // ✅ IDINAGDAG
    @SerializedName("workoutCompletionRate") val workoutCompletionRate: Double = 0.0, // ✅ IDINAGDAG

    // 🥗 Basic Nutrition Metrics
    @SerializedName("mealsCompleted") val mealsCompleted: Int = 0,
    @SerializedName("totalMeals") val totalMeals: Int = 0,
    @SerializedName("avgWaterIntake") val avgWaterIntake: Double = 0.0,

    // 📈 Advanced Nutrition Stats
    @SerializedName("averageTargetCalories") val averageTargetCalories: Double = 0.0,         // ✅ IDINAGDAG
    @SerializedName("averageConsumedCalories") val averageConsumedCalories: Double = 0.0,     // ✅ IDINAGDAG
    @SerializedName("averageBurnedCalories") val averageBurnedCalories: Double = 0.0,         // ✅ IDINAGDAG
    @SerializedName("averageNetCalories") val averageNetCalories: Double = 0.0,               // ✅ IDINAGDAG
    @SerializedName("totalDaysLogged") val totalDaysLogged: Int = 0,                          // ✅ IDINAGDAG
    @SerializedName("daysGoalMet") val daysGoalMet: Int = 0,                                  // ✅ IDINAGDAG
    @SerializedName("nutritionAdherencePercentage") val nutritionAdherencePercentage: Double = 0.0, // ✅ IDINAGDAG
    @SerializedName("averageCalorieDifference") val averageCalorieDifference: Double = 0.0,   // ✅ IDINAGDAG

    // 📊 Chart Data & Compliance
    @SerializedName("compliancePercentage") val compliancePercentage: Double = 0.0,
    @SerializedName("avgCalories") val avgCalories: Int = 0,
    @SerializedName("calorieHistory") val calorieHistory: List<ChartEntryDto> = emptyList()
)

data class ChartEntryDto(
    @SerializedName("label") val label: String = "",
    @SerializedName("value") val value: Float = 0f
)