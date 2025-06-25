package com.plcoding.cameraxguide

// RetrofitClient.kt
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.content.Context
import com.plcoding.cameraxguide.TokenManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient



object RetrofitClient {

    private lateinit var tokenManager: TokenManager

    fun init(context: Context) {
        tokenManager = TokenManager(context)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain: Interceptor.Chain ->
            val requestBuilder = chain.request().newBuilder()
            tokenManager.getToken()?.let {
                requestBuilder.addHeader("Authorization", "Bearer $it")
            }
            chain.proceed(requestBuilder.build())
        }
        .build()
    //private const val BASE_URL = "http://127.0.0.1:8000" // Sunucu URL'si
    //private const val BASE_URL = "http://10.0.2.2:8000" // Emulator'dan bağlanırken
    private const val BASE_URL = "http://raspery.duckdns.org:8000/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
