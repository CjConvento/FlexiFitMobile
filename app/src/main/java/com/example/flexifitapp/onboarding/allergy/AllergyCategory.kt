package com.example.flexifitapp.onboarding.allergy

data class AllergyCategory(
    val name: String,
    val subAllergies: MutableList<SubAllergy>,
    var isExpanded: Boolean = false,
    var isCategorySelected: Boolean = false
)