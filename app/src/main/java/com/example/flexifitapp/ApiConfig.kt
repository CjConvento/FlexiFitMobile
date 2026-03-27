package com.example.flexifitapp

object ApiConfig {
    // Port 5160 base sa setup mo
    const val BASE_URL = "http://10.222.99.247:5160/"

    // Eto ang "shortcut" para sa food images
    // Siguraduhin na match ito sa wwwroot folder structure mo sa C#
    const val FOOD_IMAGE_URL = "${BASE_URL}images/foods/"
}