package com.example.flexifitapp

import com.google.gson.annotations.SerializedName

data class DetailedProgram(
    @SerializedName("Category") val category: String,
    @SerializedName("Level") val level: String,
    @SerializedName("Environment") val environment: String,
    @SerializedName("RawName") val rawName: String
)

data class OnboardingProfileRequest(
    // Match sa C# [JsonPropertyName("Name")] at [JsonPropertyName("Username")]
    @SerializedName("Name") val name: String = "",
    @SerializedName("Username") val username: String = "",

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

    // 🔥 FIXED: Dapat "FitnessLifestyle" para match sa C# JsonPropertyName
    @SerializedName("FitnessLifestyle") val activityLevel: String,
    @SerializedName("FitnessLevel") val fitnessLevel: String,

    @SerializedName("Environment") val environment: List<String>,
    @SerializedName("FitnessGoals") val fitnessGoals: List<String>,

    @SerializedName("BodyGoal") val bodyGoal: String,
    @SerializedName("DietType") val dietType: String,

    @SerializedName("SelectedPrograms") val selectedPrograms: List<DetailedProgram>,
    @SerializedName("IsRehab") val isRehab: Boolean,

    // ADDED: Allergies field
    @SerializedName("Allergies") val allergies: List<String> = emptyList()
)