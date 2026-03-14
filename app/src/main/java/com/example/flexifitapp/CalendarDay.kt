package com.example.flexifitapp
    data class  CalendarDay(
        val dayNumber: Int?,     // null = blank cell
        val isClickable: Boolean // false for blanks
    )

data class CalendarHistoryDto(
    val day: Int,
    val isCompleted: Boolean,
    val summary: String? // Halimbawa: "Chest Day" o "1,500 kcal"
)
