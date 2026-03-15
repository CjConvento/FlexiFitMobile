package com.example.flexifitapp

import com.example.flexifitapp.auth.AuthResponse
import com.example.flexifitapp.auth.LoginRequest
import com.example.flexifitapp.OnboardingProfileRequest
import com.example.flexifitapp.auth.RegisterRequest
import com.example.flexifitapp.auth.UserAccountDto
import com.example.flexifitapp.dashboard.ProfileStatusResponse
import com.example.flexifitapp.workout.WorkoutSessionResponse
import com.example.flexifitapp.UploadAvatarResponse
import com.example.flexifitapp.nutri.NutritionResponse
import com.example.flexifitapp.profile.UpdateOnboardingRequest
import com.example.flexifitapp.profile.UserManagementResponse
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

    // Sa ApiService interface mo, hanapin ang bootstrap
    @POST("api/mobile/bootstrap")
    suspend fun bootstrap(): Response<BootstrapResponse> // <--- DAPAT GANITO, HINDI ProfileStatusResponse

    // --- ONBOARDING ---
    @POST("api/profile/complete")
    suspend fun submitProfile(@Body body: OnboardingProfileRequest): Response<Unit>

    // Gawin nating ganito para match sa C# dashboard endpoint mo:
    @GET("api/mobile/dashboard")
    suspend fun getDashboardData(): Response<ProfileStatusResponse>

    //    CALENDAR
    @GET("api/profile/calendar-history")
    suspend fun getCalendarHistory(
    @Query("month") month: Int,
    @Query("year") year: Int,
    @Query("type") type: String // "WORKOUT" o "NUTRITION"
    ): Response<List<CalendarHistoryDto>>

    // --- PROFILE & AVATAR ---
    @GET("api/profile/status")
    suspend fun getProfileStatus(): Response<ProfileStatusResponse>

    // User Management
    @GET("api/mobile/profile-full")
    suspend fun getFullProfile(): Response<UserManagementResponse>

    @PUT("api/profile/update-full")
    suspend fun updateFullProfile(@Body request: UpdateOnboardingRequest): Response<ResponseBody>

    @Multipart
    @POST("api/profile/upload-avatar")
    suspend fun uploadAvatar(
        @Part file: MultipartBody.Part
    ): Response<UploadAvatarResponse>

    // --- WORKOUT ENGINE ---
    @GET("api/workout/today")
    suspend fun getTodayWorkout(): Response<WorkoutSessionResponse>

    // DAGDAG MO ITO BABE:
    @GET("api/workout/history-detail")
    suspend fun getWorkoutHistoryDetail(
        @Query("day") day: Int,
        @Query("month") month: Int
    ): Response<WorkoutSessionResponse>

    // --- NUTRITION ENGINE ---
    @GET("api/nutrition/today-plan")
    suspend fun getTodayPlan(): Response<NutritionResponse>

    // DAGDAG MO RIN ITO:
    @GET("api/nutrition/history-detail")
    suspend fun getNutritionHistoryDetail(
        @Query("day") day: Int,
        @Query("month") month: Int
    ): Response<NutritionResponse>

    //    NOTIFICATION
    @GET("api/notifications") // Endpoint para sa ASP.NET mo
    suspend fun getNotifications(): List<NotificationItem>

    // PROGRESS TRACKER
    @GET("api/workout/stats") // Siguraduhin na match sa route ng C# mo
    suspend fun getProgressStats(
        @Query("range") range: String
    ): Response<ProgressTrackerDto>

    // --- TEST/ME ---
    @GET("api/test/me")
    suspend fun getAccountInfo(): Response<UserAccountDto>
}