package com.example.flexifitapp.profile

data class ProfileStatusResponse(
    val bmi: Double,
    val bmiCategory: String,
    val dailyCalorieTarget: Int,
    val proteinGrams: Int,
    val carbGrams: Int,
    val fatGrams: Int,
    val currentWeight: Double,
    val targetWeight: Double,
    val fitnessGoal: String
)