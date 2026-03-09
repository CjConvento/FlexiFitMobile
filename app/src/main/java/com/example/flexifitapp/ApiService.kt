package com.example.flexifitapp

import com.example.flexifitapp.workout.WorkoutSessionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

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

    @GET("api/profile/status")
    suspend fun getProfileStatus(
        @Query("userId") userId: Int
    ): Response<ProfileStatusResponse>


    // =============================
    // WORKOUT MODULE
    // =============================

    @GET("api/workout/current-session")
    suspend fun getCurrentWorkoutSession(
        @Query("userId") userId: Int
    ): Response<WorkoutSessionResponse>

}