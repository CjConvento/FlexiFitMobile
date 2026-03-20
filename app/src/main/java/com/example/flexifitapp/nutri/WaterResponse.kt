package com.example.flexifitapp.nutri

import com.google.gson.annotations.SerializedName

data class WaterResponse(
    @SerializedName("waterMl")
    val waterMl: Int
)

data class AddWaterRequest(
    @SerializedName("amountMl")
    val amountMl: Int = 250
)