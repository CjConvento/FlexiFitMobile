package com.example.flexifitapp

data class  CalendarDay(
    val dayNumber: Int?,     // null = blank cell
    val isClickable: Boolean, // false for blanks
    val isCompleted: Boolean = false // Idagdag ito para mawala ang error sa line 44
)

data class CalendarHistoryDto(
    val day: Int,
    val isCompleted: Boolean,
    val summary: String? // Halimbawa: "Chest Day" o "1,500 kcal"
)
