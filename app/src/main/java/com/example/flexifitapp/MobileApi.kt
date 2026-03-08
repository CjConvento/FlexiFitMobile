package com.example.flexifitapp

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface MobileApi {

    @POST("api/mobile/bootstrap")
    suspend fun bootstrap(): Response<BootstrapResponse>

    @POST("api/mobile/onboarding/profile")
    suspend fun submitProfile(
        @Body body: OnboardingProfileRequest
    ): Response<Unit>
}