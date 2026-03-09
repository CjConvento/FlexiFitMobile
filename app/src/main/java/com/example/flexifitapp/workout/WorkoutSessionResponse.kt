package com.example.flexifitapp.workout

data class WorkoutSessionResponse(
    val program: WorkoutProgram,
    val warmups: List<WorkoutItem>,
    val workouts: List<WorkoutItem>
)