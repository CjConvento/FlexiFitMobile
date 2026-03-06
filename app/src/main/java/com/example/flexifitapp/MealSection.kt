package com.example.flexifitapp

data class MealSection(
    val mealType: String,              // Breakfast, Lunch, Snacks, Dinner
    val foods: MutableList<MealFood>,
    var expanded: Boolean = true
)

data class MealFood(
    val mealItemId: Int,               // id ng daily plan item (important for updates)
    val foodId: Int,
    val name: String,
    val imageUrl: String?,             // from ASP.NET /images/foods/...
    var servingLabel: String,          // "1 serving (2 pancakes)"
    var qty: Int,
    var calories: Int,
    var protein: Int,
    var carbs: Int,
    var fats: Int
)