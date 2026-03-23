package com.example.flexifitapp.profile

data class UpdateOnboardingRequest(
    val name: String,          // new
    val username: String,      // new
    val age: Int,
    val gender: String,
    val heightCm: Double,
    val weightKg: Double,
    val targetWeightKg: Double,

    // Health Flags
    val upperBodyInjury: Boolean,
    val lowerBodyInjury: Boolean,
    val jointProblems: Boolean,
    val shortBreath: Boolean,

    // Lifestyle & Goals
    val fitnessLifestyle: String,
    val fitnessLevel: String,
    val environment: String,
    val bodyCompGoal: String,
    val dietaryType: String,

    // Multi-selection (Gawing empty list as default para hindi mag-null)
    val fitnessGoals: List<String> = emptyList(),
    val selectedPrograms: List<String> = emptyList(),

    // Rehab Flag
    val isRehabUser: Boolean
)