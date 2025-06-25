package com.plcoding.cameraxguide

// ApiService.kt
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Path
import retrofit2.http.Body
import retrofit2.http.Header


interface ApiService {
    @Multipart
    @POST("/app")
    fun uploadPhoto(
        @Part file: MultipartBody.Part,
        @Part("item_type") type: RequestBody,
        @Header("Authorization") token: String
    ): Call<String>

    @GET("/app/{file_id}")
    suspend fun getProcessedImage(@Path("file_id") fileId: String,
                                  @Header("Authorization") token: String): Response<ProcessedImageResponse>

    @POST("/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("/register")
    fun register(@Body user: UserCreate): Call<Void>


    @GET("detections/")
    suspend fun getDetections(
        @Header("Authorization") token: String
    ): List<Detection>





}



