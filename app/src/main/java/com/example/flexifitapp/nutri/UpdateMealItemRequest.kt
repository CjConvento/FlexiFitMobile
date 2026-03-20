package com.example.flexifitapp.nutri

import com.google.gson.annotations.SerializedName

data class UpdateMealItemRequest(
    @SerializedName("newQty")
    val newQty: Double
)

data class SwapFoodRequest(
    @SerializedName("newFoodId")
    val newFoodId: Int
)