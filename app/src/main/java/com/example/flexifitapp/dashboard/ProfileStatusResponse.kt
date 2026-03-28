package com.example.flexifitapp.dashboard

import com.google.gson.annotations.SerializedName

data class ProfileStatusResponse(
    @SerializedName("name") val name: String?,          // ✅ Add this line
    @SerializedName("userName") val username: String?,
    @SerializedName("userEmail") val userEmail: String?,
    @SerializedName("userAvatar") val userAvatar: String?, // Dagdag para sa profile pic
    @SerializedName("fitnessLevel") val fitnessLevel: String?,
    @SerializedName("goal") val goal: String?,
    @SerializedName("bmiData") val bmiData: BmiData?,
    @SerializedName("nutrition") val nutrition: NutritionData?,
    @SerializedName("upcomingWorkouts") val upcomingWorkouts: List<WorkoutExerciseDto>?,
    @SerializedName("todayMeals") val todayMeals: List<MealGroupDto>?
)

data class BmiData(
    @SerializedName("value") val value: Double,
    @SerializedName("status") val status: String?
)

data class NutritionData(
    @SerializedName("targetCalories") val targetCalories: Int,
    @SerializedName("intake") val intake: Int,
    @SerializedName("burned") val burned: Double,
    @SerializedName("netCalories") val netCalories: Int,
    @SerializedName("remaining") val remaining: Int,
    @SerializedName("waterGlasses") val waterGlasses: Int,
    @SerializedName("waterTarget") val waterTarget: Int
)

// Eto yung para sa "Today Meals" section mo babe
data class FoodItemDto(
    @SerializedName("name") val name: String?,
    @SerializedName("calories") val calories: Int,
    @SerializedName("imageUrl") val imageUrl: String?,
    @SerializedName("qty") val qty: Int,
    @SerializedName("unit") val unit: String?,
)

// 2. Dagdagan ng MealGroupDto (Match sa C# mo babe!)
data class MealGroupDto(
    @SerializedName("mealType") val mealType: String?, // B, L, S, D
    @SerializedName("status") val status: String?,
    @SerializedName("foodItems") val foodItems: List<FoodItemDto>?
)

data class WorkoutExerciseDto(
    @SerializedName("name") val name: String?,
    @SerializedName("sets") val sets: Int,
    @SerializedName("reps") val reps: Int,
    @SerializedName("imageFileName") val imageFileName: String?
)