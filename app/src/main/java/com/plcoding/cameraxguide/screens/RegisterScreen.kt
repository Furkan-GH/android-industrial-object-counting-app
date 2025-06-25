package com.plcoding.cameraxguide.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.plcoding.cameraxguide.RetrofitClient
import com.plcoding.cameraxguide.UserCreate
import retrofit2.Call


@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Kayıt ol", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Kullanıcı ismi") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Şifre") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(onClick = {
                registerUser(username, password) { success, response ->
                    if (success) {
                        message = "Kayıt başarılı! Giriş ekranına yönlendiriliyorsunuz..."
                        onNavigateToLogin()
                    } else {
                        message = response
                    }
                }
            }) {
                Text("Kayıt ol")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(text = message, color = Color.Red)

            Spacer(modifier = Modifier.height(20.dp))

            TextButton(onClick = onNavigateToLogin) {
                Text("Zaten hesabın var mı? Giriş yap")
            }
        }
    }
}
fun registerUser(username: String, password: String, callback: (Boolean, String) -> Unit) {
    val user = UserCreate(username, password)

    RetrofitClient.apiService.register(user).enqueue(object : retrofit2.Callback<Void> {
        override fun onResponse(call: Call<Void>, response: retrofit2.Response<Void>) {
            if (response.isSuccessful) {
                callback(true, "Kayıt başarılı")
            } else {
                callback(false, "Kayıt başarısız: ${response.code()}")
            }
        }

        override fun onFailure(call: Call<Void>, t: Throwable) {
            callback(false, "Sunucu hatası: ${t.message}")
        }
    })
}



