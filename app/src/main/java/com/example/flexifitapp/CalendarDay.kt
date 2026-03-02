package com.example.flexifitapp
    data class CalendarDay(
        val dayNumber: Int?,     // null = blank cell
        val isClickable: Boolean // false for blanks
    )
