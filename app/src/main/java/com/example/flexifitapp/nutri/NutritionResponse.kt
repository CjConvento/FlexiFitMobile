package com.example.flexifitapp.nutri

import com.google.gson.annotations.SerializedName

data class NutritionResponse(
    @SerializedName("targetCalories")
    val targetCalories: Double,

    @SerializedName("consumedCalories")
    val consumedCalories: Double,

    @SerializedName("burnedCalories")
    val burnedCalories: Double,

    @SerializedName("netCalories")
    val netCalories: Double,

    @SerializedName("remainingCalories")
    val remainingCalories: Double,

    @SerializedName("targetProtein")
    val targetProtein: Double,

    @SerializedName("consumedProtein")
    val consumedProtein: Double,

    @SerializedName("targetCarbs")
    val targetCarbs: Double,

    @SerializedName("consumedCarbs")
    val consumedCarbs: Double,

    @SerializedName("targetFats")
    val targetFats: Double,

    @SerializedName("consumedFats")
    val consumedFats: Double,

    @SerializedName("waterConsumedMl")
    val waterConsumedMl: Int,

    @SerializedName("waterTargetMl")
    val waterTargetMl: Int,

    @SerializedName("dailyLogId")
    val dailyLogId: Int,

    @SerializedName("meals")
    val meals: List<MealGroupDto>
)

data class MealGroupDto(
    @SerializedName("templateMealId")
    val templateMealId: Int,

    @SerializedName("mealType")
    val mealType: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("foodItems")
    val foodItems: List<FoodItemDto>
)

data class FoodItemDto(
    @SerializedName("foodId")
    val foodId: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("imageUrl")
    val imageUrl: String,

    @SerializedName("qty")
    val qty: Double,

    @SerializedName("unit")
    val unit: String,

    @SerializedName("calories")
    val calories: Double,

    @SerializedName("protein")
    val protein: Double,

    @SerializedName("carbs")
    val carbs: Double,

    @SerializedName("fats")
    val fats: Double
)

data class NutritionCompleteResultDto(
    @SerializedName("message")
    val message: String,

    @SerializedName("currentDay")
    val currentDay: Int,

    @SerializedName("isCompleted")
    val isCompleted: Boolean
)

data class  LogFullDayRequest(
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