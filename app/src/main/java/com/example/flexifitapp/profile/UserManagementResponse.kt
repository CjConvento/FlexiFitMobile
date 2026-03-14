package com.example.flexifitapp.profile

import com.google.gson.annotations.SerializedName

data class UserManagementResponse(
    val name: String,
    val username: String,
    val gender: String,
    val age: Int,
    val heightCm: Double,
    val weightKg: Double,
    val bmi: Double,
    val bmiCategory: String,
    val goalSubtitle: String,

    // Workout Progress: Binabasa ang 'completedSessions' pero 'totalSessions' ang tawag sa Kotlin
    @SerializedName("completedSessions")
    val totalSessions: Int,

    val totalProgramSessions: Int,

    // Achievements: Binabasa ang listahan at isasave sa 'unlockedBadges'
    val achievementCount: Int,

    @SerializedName("unlockedBadgeKeys")
    val unlockedBadges: List<String>,

    // Nutritional
    val dailyCalorieTarget: Int,
    val proteinG: Double,
    val carbsG: Double,
    val fatsG: Double
)