package com.example.flexifitapp.dashboard

import com.google.gson.annotations.SerializedName

data class ProfileStatusResponse(
    @SerializedName("userName") val userName: String?,
    @SerializedName("fitnessLevel") val fitnessLevel: String?,
    @SerializedName("goal") val fitnessGoal: String?,

    // 1. BMI Card Data
    @SerializedName("bmiData") val bmiData: BmiData?,

    // 2. Nutrition & Calories Card Data
    @SerializedName("nutrition") val nutrition: NutritionData?,

    // 3. Program/Workout Info
    @SerializedName("program") val program: ProgramData?,

    // Dagdag natin 'to para sa "Current Day Meals" RecyclerView mo
    @SerializedName("todayMeals") val todayMeals: List<DashboardMealDto>? = emptyList()
)

data class BmiData(
    @SerializedName("value") val value: Double,
    @SerializedName("status") val status: String // E.g., "You have a normal weight"
)

data class NutritionData(
    @SerializedName("targetCalories") val target: Double,
    @SerializedName("intake") val intake: Double,    // For Intake Bar
    @SerializedName("burned") val burned: Double,    // For Burned Bar
    @SerializedName("netCalories") val net: Double,  // For Circle Value
    @SerializedName("remaining") val remaining: Double, // For Circle "Left"
    @SerializedName("waterGlasses") val waterGlasses: Int,
    @SerializedName("waterTarget") val waterTarget: Int
)

data class ProgramData(
    @SerializedName("name") val programName: String?,
    @SerializedName("dayNo") val dayNo: Int,
    @SerializedName("isWorkoutDay") val isWorkoutDay: Boolean
)

data class DashboardMealDto(
    @SerializedName("name") val name: String,
    @SerializedName("kcal") val kcal: Double,
    @SerializedName("serving") val serving: String,
    @SerializedName("imageUrl") val imageUrl: String
)