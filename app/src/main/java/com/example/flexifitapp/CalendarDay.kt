package com.example.flexifitapp

data class CalendarDay(
    val dayNumber: Int?,
    val isClickable: Boolean,
    val status: String = "NOT_STARTED" // Ito yung default natin babe
)

data class CalendarHistoryDto(
    val day: Int,
    val status: String, // String na ito babe ha, hindi na boolean
    val summary: String? // Halimbawa: "Chest Day" o "1,500 kcal"
)
