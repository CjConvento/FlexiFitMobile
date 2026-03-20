package com.example.flexifitapp.nutri

import com.google.gson.annotations.SerializedName

data class LogFullDayRequest(
    @SerializedName("cycleId")
    val cycleId: Int,

    @SerializedName("meals")
    val meals: List<LogMealEntry>
)