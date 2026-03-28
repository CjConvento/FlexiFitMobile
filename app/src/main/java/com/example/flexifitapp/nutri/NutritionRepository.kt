package com.example.flexifitapp.nutri

import android.util.Log
import com.example.flexifitapp.ApiService

class NutritionRepository(private val apiService: ApiService) {

    private val TAG = "NUTRITION_REPO"

    suspend fun getTodayNutrition(): NutritionResponse? {
        return try {
            Log.d(TAG, "Fetching Today's Nutrition...")
            val response = apiService.getTodayNutrition()
            if (response.isSuccessful) {
                Log.d(TAG, "Today's Nutrition Success")
                response.body()
            } else {
                Log.e(TAG, "Today's Nutrition Failed: Code ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Today's Nutrition Exception: ${e.message}")
            null
        }
    }

    suspend fun getNutritionByDate(day: Int, month: Int): NutritionResponse? {
        return try {
            Log.d(TAG, "Fetching Nutrition for Day: $day, Month: $month")
            val response = apiService.getNutritionHistoryDetail(day, month)
            if (response.isSuccessful) {
                Log.d(TAG, "Nutrition Detail Success")
                response.body()
            } else {
                Log.e(TAG, "Nutrition Detail Failed: Code ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Nutrition Detail Exception: ${e.message}")
            null
        }
    }

    suspend fun completeNutritionDay(
        cycleId: Int,
        meals: List<LogMealEntry>
    ): NutritionCompleteResultDto? {
        return try {
            Log.d(TAG, "Completing Nutrition Day...")
            val request = LogFullDayRequest(cycleId, meals)
            val response = apiService.completeNutrition(request)
            if (response.isSuccessful) {
                Log.d(TAG, "Nutrition Day Completed")
                response.body()
            } else {
                Log.e(TAG, "Complete Nutrition Failed: Code ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Complete Nutrition Exception: ${e.message}")
            null
        }
    }

    // ✅ ADD WATER METHODS
    suspend fun addWater(amountMl: Int = 250): WaterResponse? {
        return try {
            Log.d(TAG, "Adding water: $amountMl ml")
            val response = apiService.addWater(AddWaterRequest(amountMl))
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e(TAG, "Add water failed: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Add water exception: ${e.message}")
            null
        }
    }

    suspend fun getWaterToday(): WaterResponse? {
        return try {
            Log.d(TAG, "Getting today's water intake")
            val response = apiService.getWaterToday()
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e(TAG, "Get water failed: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get water exception: ${e.message}")
            null
        }
    }

    suspend fun resetWater(): Boolean {
        return try {
            Log.d(TAG, "Resetting water")
            val response = apiService.resetWater()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Reset water exception: ${e.message}")
            false
        }
    }

    // ✅ ADD FOOD DETAILS METHOD
    suspend fun getFoodDetails(foodId: Int): FoodDetailsResponse? {
        return try {
            Log.d(TAG, "Getting food details for ID: $foodId")
            val response = apiService.getFoodDetails(foodId)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e(TAG, "Get food details failed: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get food details exception: ${e.message}")
            null
        }
    }

    // ✅ ADD MEAL ITEM UPDATE METHODS
    suspend fun updateMealItem(mealItemId: Int, newQty: Double): Boolean {
        return try {
            Log.d(TAG, "Updating meal item $mealItemId to quantity $newQty")
            val response = apiService.updateMealItem(mealItemId, UpdateMealItemRequest(newQty))
            if (response.isSuccessful) {
                true
            } else {
                Log.e(TAG, "Update failed: ${response.code()} - ${response.errorBody()?.string()}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Update exception: ${e.message}")
            false
        }
    }

    suspend fun swapFoodItem(mealItemId: Int, newFoodId: Int): Boolean {
        return try {
            Log.d(TAG, "Swapping meal item $mealItemId to food $newFoodId")
            val response = apiService.swapFoodItem(mealItemId, SwapFoodRequest(newFoodId))
            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Swap food item exception: ${e.message}")
            false
        }
    }
}