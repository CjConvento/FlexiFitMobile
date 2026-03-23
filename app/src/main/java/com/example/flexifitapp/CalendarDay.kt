package com.example.flexifitapp

data class CalendarDay(
    val dayNumber: Int?,
    val isClickable: Boolean,
    val status: String = "NOT_STARTED",
    val workoutStatus: String? = null,
    val nutritionStatus: String? = null,
    val dayType: String? = null,
    val summary: String? = null,
    val isCurrentDay: Boolean = false
)