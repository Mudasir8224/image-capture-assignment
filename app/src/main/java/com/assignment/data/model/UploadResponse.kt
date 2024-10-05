package com.assignment.data.model


data class UploadResponse(
    val status_code: Int,
    val success: Success,
    val image: Image,
    val status_txt: String
)

data class Success(
    val message: String,
    val code: Int
)

data class Image(
    val name: String,
    val extension: String,
    val size: Int,
    val width: Int,
    val height: Int,
    val url: String,
    val url_viewer: String
    // Add more fields as needed based on the API response
)
