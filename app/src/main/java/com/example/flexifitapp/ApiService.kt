package com.example.flexifitapp

import com.example.flexifitapp.dashboard.CalorieResponse
import com.example.flexifitapp.workout.WorkoutSessionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @POST("api/auth/register")
    suspend fun register(
        @Body body: RegisterRequest
    ): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(
        @Body body: LoginRequest
    ): Response<AuthResponse>

    @POST("api/mobile/bootstrap")
    suspend fun bootstrap(): Response<BootstrapResponse>

    /**
     * Ginagamit para sa Profile Screen at Nutrition Plan Screen.
     * Ang backend na ang magka-calculate ng BMI, TDEE, at Macros
     * base sa database record ng user.
     */
    @GET("api/profile/status")
    suspend fun getProfileStatus(): Response<ProfileStatusResponse>

    /**
     * Kukunin ang workout session para sa araw na ito.
     * Ang backend na ang mag-bubuo ng full Media URLs (IP + Filename).
     */
    @GET("api/workout/today")
    suspend fun getTodayWorkout(): Response<WorkoutSessionResponse>

    // Kung kailangan mo pa rin ng manual calculator (optional):
    // @POST("api/calculator/compute")
    // suspend fun computeCalculator(@Body body: CalculatorRequest): Response<CalorieResponse>
}