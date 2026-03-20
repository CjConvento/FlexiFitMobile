package com.example.flexifitapp.nutri

import com.google.gson.annotations.SerializedName

data class FoodDetailsResponse(
    @SerializedName("foodId")
    val foodId: Int,

    @SerializedName("foodName")
    val foodName: String,

    @SerializedName("description")
    val description: String?,

    @SerializedName("calories")
    val calories: Double,

    @SerializedName("proteinG")
    val proteinG: Double,

    @SerializedName("carbsG")
    val carbsG: Double,

    @SerializedName("fatsG")
    val fatsG: Double,

    @SerializedName("servingUnit")
    val servingUnit: String,

    @SerializedName("imgFilename")
    val imgFilename: String?
)