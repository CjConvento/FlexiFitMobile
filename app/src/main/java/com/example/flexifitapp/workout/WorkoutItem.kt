package com.example.flexifitapp.workout

data class WorkoutItem(
    val id: Int,
    val name: String,
    val imageFileName: String,
    val muscleGroup: String?,
    val sets: Int,
    val reps: Int,
    val restSeconds: Int,
    val durationMinutes: Int,
    val calories: Int,
    val description: String,
    val videoUrl: String?,
    // DAGDAG NATIN ITO:
    val order: Int?,           // Para sa tamang pagkakasunod-sunod
    val isCompleted: Boolean?,  // Para sa UI checkmark/strikethrough
    val loadKg: Double?       // Para sa progressive overload tracking
)