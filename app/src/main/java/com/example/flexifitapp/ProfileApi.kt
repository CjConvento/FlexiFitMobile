package com.example.flexifitapp

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ProfileApi {

    @Multipart
    @POST("api/profile/upload-avatar")
    suspend fun uploadAvatar(
        @Part file: MultipartBody.Part,
        @Part("userId") userId: Int
    ): Response<UploadAvatarResponse>
}

data class UploadAvatarResponse(
    val url: String
)