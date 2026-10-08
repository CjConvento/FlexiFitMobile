package com.example.flexifitapp.workout

data class WorkoutProgram(
    val programId: Int,
    val programName: String,
    val environment: String,
    val level: String,
    val description: String,
    val status: String,
    val month: Int,
    val week: Int,
    val day: Int,
    val programNumber: Int = 0,  // ← add this with a default

    // para malaman kung ilang programs lahat ng user (nakadefault na ng 1)
    val totalPrograms: Int = 1
)