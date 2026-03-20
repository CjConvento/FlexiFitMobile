package com.example.flexifitapp

import com.google.gson.annotations.SerializedName

data class CalendarHistoryDto(
    @SerializedName("day")
    val day: Int,

    @SerializedName("week")
    val week: Int,

    @SerializedName("workoutStatus")
    val workoutStatus: String,

    @SerializedName("nutritionStatus")
    val nutritionStatus: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("summary")
    val summary: String?,

    @SerializedName("dayType")
    val dayType: String
)

data class ProgressResponse(
    @SerializedName("currentDay")
    val currentDay: Int,

    @SerializedName("currentWeek")
    val currentWeek: Int,

    @SerializedName("currentDayInWeek")
    val currentDayInWeek: Int,

    @SerializedName("totalDays")
    val totalDays: Int,

    @SerializedName("totalWeeks")
    val totalWeeks: Int,

    @SerializedName("completedWorkouts")
    val completedWorkouts: Int,

    @SerializedName("completedNutrition")
    val completedNutrition: Int,

    @SerializedName("progress")
    val progress: Double,

    @SerializedName("programStatus")
    val programStatus: String
)

data class LogFullDayRequest(
    @SerializedName("cycleId")
    val cycleId: Int,

    @SerializedName("meals")
    val meals: List<LogMealEntry>
)

data class LogMealEntry(
    @SerializedName("mealType")
    val mealType: String,

    @SerializedName("totalCalories")
    val totalCalories: Double,

    @SerializedName("totalProtein")
    val totalProtein: Double,

    @SerializedName("totalCarbs")
    val totalCarbs: Double,

    @SerializedName("totalFats")
    val totalFats: Double
)