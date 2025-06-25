package com.plcoding.cameraxguide


import android.content.Context
import com.plcoding.cameraxguide.TokenManager
import com.plcoding.cameraxguide.RetrofitClient
import retrofit2.Response

class AuthRepository(context: Context) {
    private val tokenManager = TokenManager(context)
    private val api = RetrofitClient.apiService

    suspend fun login(username: String, password: String): Response<LoginResponse> {
        val response = api.login(LoginRequest(username, password))
        if (response.isSuccessful && response.body()?.token != null) {
            tokenManager.saveToken(response.body()!!.token)
        }
        return response
    }

    fun getToken(): String? = tokenManager.getToken()
}
