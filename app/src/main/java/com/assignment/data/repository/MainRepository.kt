package com.assignment.data.repository

import com.assignment.data.api.ApiService
import com.assignment.data.model.UploadResponse
import okhttp3.MultipartBody
import retrofit2.Response
import javax.inject.Inject

class MainRepository @Inject constructor(private val apiService: ApiService) {

    suspend fun uploadImage(file: MultipartBody.Part, apiKey: String): Response<UploadResponse> {
        return apiService.uploadImage(file, apiKey)
    }

}