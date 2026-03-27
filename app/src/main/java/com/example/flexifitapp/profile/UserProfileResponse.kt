package com.example.flexifitapp.profile

import com.google.gson.annotations.SerializedName

data class UserProfileResponse(
    @SerializedName("name") val name: String,
    @SerializedName("username") val username: String,
    @SerializedName("avatarUrl") val avatarUrl: String? = null,
    @SerializedName("age") val age: Int,
    @SerializedName("gender") val gender: String,
    @SerializedName("heightCm") val heightCm: Double,
    @SerializedName("weightKg") val weightKg: Double,
    @SerializedName("targetWeightKg") val targetWeightKg: Double? = null,
    @SerializedName("bmi") val bmi: Double,
    @SerializedName("bmiCategory") val bmiCategory: String,
    @SerializedName("nutritionGoal") val nutritionGoal: String? = null,
    @SerializedName("goalSubtitle") val goalSubtitle: String,
    @SerializedName("totalSessions") val totalSessions: Int,
    @SerializedName("totalWorkouts") val totalWorkouts: Int,
    @SerializedName("completedSessions") val completedSessions: Int,
    @SerializedName("totalProgramSessions") val totalProgramSessions: Int,
    @SerializedName("selectedPrograms") val selectedPrograms: List<String> = emptyList(),
    @SerializedName("fitnessGoals") val fitnessGoals: List<String> = emptyList(),
    @SerializedName("dailyCalorieTarget") val dailyCalorieTarget: Int,
    @SerializedName("proteinG") val proteinG: Double,
    @SerializedName("carbsG") val carbsG: Double,
    @SerializedName("fatsG") val fatsG: Double,
    @SerializedName("achievementCount") val achievementCount: Int,
    @SerializedName("unlockedBadges") val unlockedBadges: List<String> = emptyList(),
    @SerializedName("unlockedBadgeKeys") val unlockedBadgeKeys: List<String> = emptyList()
)