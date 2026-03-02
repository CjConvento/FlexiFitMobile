package com.example.flexifitapp

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("api/profile/status")
    suspend fun getProfileStatus(
        @Query("userId") userId: Int
    ): Response<ProfileStatusResponse>
}