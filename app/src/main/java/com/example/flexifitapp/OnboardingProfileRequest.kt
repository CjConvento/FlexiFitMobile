package com.example.flexifitapp

import com.google.gson.annotations.SerializedName

data class OnboardingProfileRequest(
    @SerializedName("Age") val age: Int,
    @SerializedName("Gender") val gender: String,

    @SerializedName("HeightCm") val heightCm: Double,
    @SerializedName("WeightKg") val weightKg: Double,
    @SerializedName("TargetWeightKg") val targetWeightKg: Double,

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

    @SerializedName("SelectedPrograms") val selectedPrograms: List<String>
)