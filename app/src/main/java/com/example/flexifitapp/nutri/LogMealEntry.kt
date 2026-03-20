package com.example.flexifitapp.nutri

import com.google.gson.annotations.SerializedName

data class LogMealEntry(
    @SerializedName("mealType")
    val mealType: String,

    @SerializedName("totalCalories")
    val totalCalories: Double,

    @SerializedName("totalProtein")
    val totalProtein: Double,

    @SerializedName("totalCarbs")
    val totalCarbs: Double,

    @SerializedName("totalFats")
    val totalFats: Double
)