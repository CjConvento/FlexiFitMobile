package com.example.flexifitapp

import com.example.flexifitapp.auth.AuthResponse
import com.example.flexifitapp.auth.LoginRequest
import com.example.flexifitapp.auth.RegisterRequest
import com.example.flexifitapp.auth.UserAccountDto
import com.example.flexifitapp.dashboard.ProfileStatusResponse
import com.example.flexifitapp.workout.WorkoutSessionResponse
import com.example.flexifitapp.workout.WorkoutSessionCompleteDto
import com.example.flexifitapp.workout.WorkoutSessionResultDto
import com.example.flexifitapp.workout.CanSkipResponse
import com.example.flexifitapp.workout.WorkoutHistoryDto
import com.example.flexifitapp.notification.NotificationItemDto
import com.example.flexifitapp.nutri.AddWaterRequest
import com.example.flexifitapp.nutri.FoodDetailsResponse

import com.example.flexifitapp.profile.UpdateOnboardingRequest
import com.example.flexifitapp.profile.UserManagementResponse

import com.example.flexifitapp.nutri.LogFullDayRequest  // ✅ Import from nutri package
import com.example.flexifitapp.nutri.NutritionCompleteResultDto
import com.example.flexifitapp.nutri.NutritionResponse
import com.example.flexifitapp.nutri.SwapFoodRequest
import com.example.flexifitapp.nutri.UpdateMealItemRequest
import com.example.flexifitapp.nutri.WaterResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // --- AUTHENTICATION ---
    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>

    @GET("api/mobile/bootstrap")
    suspend fun bootstrap(): Response<BootstrapResponse>

    @POST("api/mobile/onboarding/profile")
    suspend fun submitProfile(
        @Body body: OnboardingProfileRequest
    ): Response<ResponseBody>

    @GET("api/mobile/dashboard")
    suspend fun getDashboardData(): Response<ProfileStatusResponse>

    // ✅ FIXED: Calendar endpoint - NO parameters (consistent with C#)
    @GET("api/calendar/history")
    suspend fun getCalendarHistory(): Response<List<CalendarHistoryDto>>

    // Optional: Keep this if you need month filtering
    @GET("api/calendar/history")
    suspend fun getCalendarHistoryWithMonth(
        @Query("month") month: Int
    ): Response<List<CalendarHistoryDto>>

    // ✅ ADD THIS - Program progress
    @GET("api/calendar/progress")
    suspend fun getProgramProgress(): Response<ProgressResponse>

    // --- WORKOUT ENGINE ---
    @GET("api/workout/today")
    suspend fun getTodayWorkout(): Response<WorkoutSessionResponse>

    @GET("api/workout/history-detail")
    suspend fun getWorkoutHistoryDetail(
        @Query("day") day: Int,
        @Query("month") month: Int
    ): Response<WorkoutSessionResponse>

    @POST("api/workout/complete")
    suspend fun completeWorkout(
        @Body request: WorkoutSessionCompleteDto
    ): Response<WorkoutSessionResultDto>

    @GET("api/workout/can-skip")
    suspend fun canSkipToday(): Response<CanSkipResponse>

    @GET("api/workout/history")
    suspend fun getWorkoutHistory(): Response<List<WorkoutHistoryDto>>

    // --- NUTRITION ENGINE ---
    @GET("api/nutrition/today")
    suspend fun getTodayNutrition(): Response<NutritionResponse>

    @GET("api/nutrition/history-detail")
    suspend fun getNutritionHistoryDetail(
        @Query("day") day: Int,
        @Query("month") month: Int
    ): Response<NutritionResponse>

    @POST("api/nutrition/complete")
    suspend fun completeNutrition(
        @Body request: LogFullDayRequest
    ): Response<NutritionCompleteResultDto>

    // ✅ ADD FOOD DETAILS
    @GET("api/nutrition/food/{foodId}")
    suspend fun getFoodDetails(@Path("foodId") foodId: Int): Response<FoodDetailsResponse>

    // ✅ ADD MEAL ITEM UPDATE
    @PUT("api/nutrition/meal-item/{mealItemId}")
    suspend fun updateMealItem(
        @Path("mealItemId") mealItemId: Int,
        @Body request: UpdateMealItemRequest
    ): Response<ResponseBody>

    @POST("api/nutrition/meal-item/{mealItemId}/swap")
    suspend fun swapFoodItem(
        @Path("mealItemId") mealItemId: Int,
        @Body request: SwapFoodRequest
    ): Response<ResponseBody>

    // ✅ ADD WATER ENDPOINTS
    @POST("api/nutrition/water/add")
    suspend fun addWater(@Body request: AddWaterRequest): Response<WaterResponse>

    @GET("api/nutrition/water/today")
    suspend fun getWaterToday(): Response<WaterResponse>

    @DELETE("api/nutrition/water/reset")
    suspend fun resetWater(): Response<ResponseBody>

    // --- PROFILE & AVATAR ---
    @GET("api/profile/status")
    suspend fun getProfileStatus(): Response<ProfileStatusResponse>

    @GET("api/mobile/profile-full")
    suspend fun getFullProfile(): Response<UserManagementResponse>

    @PUT("api/profile/update-full")
    suspend fun updateFullProfile(@Body request: UpdateOnboardingRequest): Response<ResponseBody>

    @Multipart
    @POST("api/profile/upload-avatar")
    suspend fun uploadAvatar(
        @Part file: MultipartBody.Part
    ): Response<UploadAvatarResponse>

    @GET("api/progress/stats")
    suspend fun getProgressStats(
        @Query("range") range: String
    ): Response<ProgressTrackerDto>

    // --- NOTIFICATION ENDPOINTS ---
    @GET("api/notifications/settings")
    suspend fun getNotificationSettings(): Response<NotificationSettingsDto>

    @PUT("api/notifications/settings")
    suspend fun updateNotificationSettings(
        @Body request: UpdateNotificationSettingsRequest
    ): Response<ResponseBody>

    @GET("api/notifications")
    suspend fun getNotifications(): Response<List<NotificationItemDto>>

    @DELETE("api/notifications/{notificationId}")
    suspend fun deleteNotification(
        @Path("notificationId") notificationId: Int
    ): Response<ResponseBody>

    @DELETE("api/notifications/clear-all")
    suspend fun clearAllNotifications(): Response<ResponseBody>

    // --- TEST/ME ---
    @GET("api/test/me")
    suspend fun getAccountInfo(): Response<UserAccountDto>
}