package com.assignment.data.api

import com.assignment.data.model.UploadResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @Multipart
    @POST("api/1/upload")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part,
        @Part("key") apiKey: String,
        @Part("action") action: String = "upload",
        @Part("format") format: String = "json"
    ): Response<UploadResponse>

}