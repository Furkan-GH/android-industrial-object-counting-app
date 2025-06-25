package com.plcoding.cameraxguide

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class LogViewModel(private val tokenManager: TokenManager) : ViewModel() {
    private val _logs = mutableStateOf<List<Detection>>(emptyList())
    val logs: State<List<Detection>> = _logs

    init {
        getLogs()
    }

    private fun getLogs() {
        viewModelScope.launch {
            try {
                val token = tokenManager.getToken()
                if (token != null) {
                    _logs.value = RetrofitClient.apiService.getDetections("Bearer $token")
                }
            } catch (e: Exception) {
                Log.e("LogViewModel", "Logları çekerken hata: ${e.message}")
            }
        }
    }
}


class LogViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val tokenManager = TokenManager(context)
        return LogViewModel(tokenManager) as T
    }
}