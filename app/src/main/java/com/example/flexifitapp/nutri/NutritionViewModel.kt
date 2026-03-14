package com.example.flexifitapp.nutri

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flexifitapp.ApiClient
import kotlinx.coroutines.launch

class NutritionViewModel : ViewModel() {

    private val _sections = MutableLiveData<MutableList<MealSection>>(mutableListOf())
    val sections: LiveData<MutableList<MealSection>> = _sections

    // Para sa PieChart (Summary Data)
    private val _nutritionSummary = MutableLiveData<NutritionResponse>()
    val nutritionSummary: LiveData<NutritionResponse> = _nutritionSummary

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    /**
     * MISSION: FETCH TODAY PLAN
     * Ito ang hihigop ng data mula sa NutritionController.cs
     */
    fun fetchTodayPlan(context: Context) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Gamitin ang ApiClient mo para tawagin ang service
                val apiService = ApiClient.api(context)
                val response = apiService.getTodayPlan()

                if (response.isSuccessful) {
                    response.body()?.let { data ->
                        // 1. I-save ang summary para sa PieChart/Dashboard text
                        _nutritionSummary.value = data

                        // 2. I-map ang JSON list papunta sa UI MealSections
                        // Sa loob ng fetchTodayPlan...
                        val mappedSections = data.meals.map { meal ->
                            MealSection(
                                mealType = meal.mealType,
                                foods = meal.foodItems.map { food ->
                                    MealFood(
                                        mealItemId = food.foodId,
                                        foodId = food.foodId,
                                        name = food.name,
                                        description = food.description ?: "", // <--- DITO ANG FIX!
                                        imageUrl = food.imageUrl,
                                        servingLabel = "${food.qty} ${food.unit}",
                                        qty = food.qty.toInt(),
                                        calories = food.calories.toInt(),
                                        protein = food.protein.toInt(),
                                        carbs = food.carbs.toInt(),
                                        fats = food.fats.toInt()
                                    )
                                }.toMutableList()
                            )
                        }.toMutableList()

                        _sections.value = mappedSections
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * MISSION: UPDATE MACROS (Calculation from Server)
     */
    fun updateMacrosFromServer(mealItemId: Int, foodId: Int, newWeight: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // TODO: Dito mo itatawag yung apiService.calculateMacros(...)
                // Kapag nakuha ang response, i-assign lang ang values gaya ng logic mo dati
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}