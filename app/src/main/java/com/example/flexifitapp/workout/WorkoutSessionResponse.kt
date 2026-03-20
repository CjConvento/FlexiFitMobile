package com.example.flexifitapp.workout

import com.google.gson.annotations.SerializedName

data class WorkoutSessionResponse(
    val dayNo: Int,
    val dayType: String,
    val message: String,
    val status: String,
    val totalDuration: Int,
    @SerializedName("totalCalories")
    val totalCalories: Int,
    val focusArea: String?,
    val level: String?,
    val canSkip: Boolean,           // ✅ ADD THIS
    val skipMessage: String?,       // ✅ ADD THIS
    @SerializedName("sessionId")
    val sessionId: Int,             // ✅ ADD THIS
    val program: WorkoutProgram,
    val warmups: List<WorkoutItem>,
    val workouts: List<WorkoutItem>
)