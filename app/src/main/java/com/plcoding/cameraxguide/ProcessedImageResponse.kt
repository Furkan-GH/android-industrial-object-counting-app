package com.plcoding.cameraxguide

import com.google.gson.annotations.SerializedName


data class ProcessedImageResponse(
    val status: String,
    val data: ProcessedImageData
)

data class ProcessedImageData(
    val image: String, // Base64 string
    val prediction: String
)


data class LoginRequest(val username: String, val password: String)

data class LoginResponse(
    @SerializedName("access_token") val token: String,
    @SerializedName("token_type") val tokenType: String
)

data class UserCreate(
    val username: String,
    val password: String
)


data class Detection(
    val id: Int,
    val timestamp: String,
    val object_type: String,
    val detected_count: Int,
    val image_url: String
)
