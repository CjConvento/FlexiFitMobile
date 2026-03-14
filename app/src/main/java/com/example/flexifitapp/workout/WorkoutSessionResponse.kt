package com.example.flexifitapp.workout

data class WorkoutSessionResponse(
    // Idagdag natin ito para alam ng app kung anong Day 1 o Day 2 na si user
    val dayNo: Int,
    val dayType: String,
    val message: String,

    // Eto yung mga dati mo nang fields
    val program: WorkoutProgram,
    val warmups: List<WorkoutItem>,
    val workouts: List<WorkoutItem>
)