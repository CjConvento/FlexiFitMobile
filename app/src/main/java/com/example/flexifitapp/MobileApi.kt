package com.example.flexifitapp

import com.example.flexifitapp.BootstrapRequest
import com.example.flexifitapp.BootstrapResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface MobileApi {

    // ✅ BOOTSTRAP: used after login to decide Dashboard vs Onboarding
    @POST("api/mobile/bootstrap")
    suspend fun bootstrap(
        @Body body: BootstrapRequest
    ): Response<BootstrapResponse>
    @POST("api/mobile/onboarding/profile")
    suspend fun submitProfile(
        @Body body: OnboardingProfileRequest
    ): Response<Unit>
}