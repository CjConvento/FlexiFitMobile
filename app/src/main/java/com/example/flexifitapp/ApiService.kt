package com.example.flexifitapp

import com.example.flexifitapp.dashboard.ProfileStatusResponse // Gamitin ang merged model
import com.example.flexifitapp.workout.WorkoutSessionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    // --- AUTHENTICATION ---
    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>

    // --- ONBOARDING & BOOTSTRAP ---
    @POST("api/mobile/bootstrap")
    suspend fun bootstrap(): Response<BootstrapResponse>

    @POST("api/mobile/onboarding/profile")
    suspend fun submitProfile(@Body body: OnboardingProfileRequest): Response<Unit>

    // --- USER PROFILE & NUTRITION ---
    // Ngayon, ito na ang gagamitin natin sa Dashboard
    @GET("api/profile/status")
    suspend fun getProfileStatus(): Response<ProfileStatusResponse>

    // --- WORKOUT ENGINE ---
    @GET("api/workout/today")
    suspend fun getTodayWorkout(): Response<WorkoutSessionResponse>
}