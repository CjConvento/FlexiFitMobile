package com.example.flexifitapp.profile

import com.google.gson.annotations.SerializedName

data class UpdateWeightRequest(
    @SerializedName("newWeight") val newWeight: Double
)

