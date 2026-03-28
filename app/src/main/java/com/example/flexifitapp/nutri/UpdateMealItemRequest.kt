package com.example.flexifitapp.nutri

import com.google.gson.annotations.SerializedName

data class UpdateMealItemRequest(
    @SerializedName("NewQty")   // ✅ Capital N to match C# DTO
    val newQty: Double
)

data class SwapFoodRequest(
    @SerializedName("NewFoodId")   // ✅ Capital N, F to match server
    val newFoodId: Int
)