package com.example.flexifitapp

import com.google.gson.annotations.SerializedName

// 1. Structure para sa Program details (Walang bago dito, okay na 'to)
data class DetailedProgram(
    @SerializedName("Category") val category: String,    // e.g. "CARDIO"
    @SerializedName("Level") val level: String,          // e.g. "Beginner"
    @SerializedName("Environment") val environment: String, // e.g. "GYM"
    @SerializedName("RawName") val rawName: String       // e.g. "Cardio Beginner Gym Program"
)

data class OnboardingProfileRequest(
    @SerializedName("Age") val age: Int,
    @SerializedName("Gender") val gender: String,

    @SerializedName("HeightCm") val heightCm: Int,
    @SerializedName("WeightKg") val weightKg: Int,
    @SerializedName("TargetWeightKg") val targetWeightKg: Int,

    @SerializedName("UpperBodyInjury") val upperBodyInjury: Boolean,
    @SerializedName("LowerBodyInjury") val lowerBodyInjury: Boolean,
    @SerializedName("JointProblems") val jointProblems: Boolean,
    @SerializedName("ShortBreath") val shortBreath: Boolean,
    @SerializedName("HealthNone") val healthNone: Boolean,

    @SerializedName("ActivityLevel") val activityLevel: String,
    @SerializedName("FitnessLevel") val fitnessLevel: String,

    @SerializedName("Environment") val environment: List<String>,
    @SerializedName("FitnessGoals") val fitnessGoals: List<String>,

    @SerializedName("BodyGoal") val bodyGoal: String,
    @SerializedName("DietType") val dietType: String,

    @SerializedName("SelectedPrograms") val selectedPrograms: List<DetailedProgram>,

    // ITO ANG DINAGDAG NATIN BABE PARA MAWALA ANG ERROR:
    @SerializedName("IsRehab") val isRehab: Boolean
)