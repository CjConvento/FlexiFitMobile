package com.example.flexifitapp.nutri

data class NutritionResponse(
    val targetCalories: Double,
    val consumedCalories: Double,
    val burnedCalories: Double,
    val netCalories: Double,
    val remainingCalories: Double,

    // Macro Totals (Galing na sa Engine mo!)
    val targetProtein: Double,
    val consumedProtein: Double,
    val targetCarbs: Double,
    val consumedCarbs: Double,
    val targetFats: Double,
    val consumedFats: Double,

    val waterConsumedMl: Int,
    val waterTargetMl: Int,
    val meals: List<MealGroupDto>
)

data class MealGroupDto(
    val templateMealId: Int,
    val mealType: String,
    val status: String,
    val foodItems: List<FoodItemDto>
)

data class FoodItemDto(
    val foodId: Int,
    val name: String,
    val description: String,
    val imageUrl: String,
    val qty: Double,
    val unit: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fats: Double
)