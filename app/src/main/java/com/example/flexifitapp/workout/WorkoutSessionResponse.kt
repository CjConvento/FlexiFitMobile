package com.example.flexifitapp.workout
import com.google.gson.annotations.SerializedName

data class WorkoutSessionResponse(
    val dayNo: Int,
    val dayType: String,
    val focusArea: String?,      // Upper Body, etc.
    val totalDuration: Int,      // 45 mins

    @SerializedName("totalCalories") // Eto yung bridge natin sa C# babe!
    val estimatedCalories: Int,

    val level: String?,          // Intermediate
    val message: String,
    val program: WorkoutProgram,
    val warmups: List<WorkoutItem>,
    val workouts: List<WorkoutItem>
)