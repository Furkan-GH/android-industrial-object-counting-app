package com.plcoding.cameraxguide

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

// ViewModel, fotoğraf işlemleri yönetir
class PhotoViewModel : ViewModel() {
    var isLoading by mutableStateOf(false)
    var processedImage by mutableStateOf<Bitmap?>(null)
    var errorMessage by mutableStateOf<String?>(null)
    var showDialog by mutableStateOf(false)
    var prediction: String? by mutableStateOf(null)

    // Fotoğraf gönderme fonksiyonu
    fun sendPhoto(bitmap: Bitmap, context: Context,photoViewModel: PhotoViewModel,selectedType:String,token:String) {
        isLoading = true // Yükleme ekranını başlat
        Log.d("PhotoUpload",selectedType)
        GlobalScope.launch(Dispatchers.IO) {  // IO işlemleri için IO Dispatcher
            try {
                // Fotoğrafı byte array'e çevir
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()

                // Byte array'i MultipartBody.Part'e dönüştür
                val requestBody: RequestBody = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", "photo.jpg", requestBody)

                //Tip bilgisi body olarak oluşturma
                val typePart = selectedType.toRequestBody("text/plain".toMediaTypeOrNull())

                // API çağrısını yap
                val apiService = RetrofitClient.apiService
                Log.i("TAG", "Fotoğraf ve tür yükleniyor...")
                val authHeader = "Bearer $token"

                val call = apiService.uploadPhoto(body, typePart, authHeader)
                call.enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        if (response.isSuccessful && response.body() != null) {
                            Log.i("TAG", "Fotoğraf başarıyla yüklendi.")
                            val fileId = response.body().toString()

                            Log.i("TAG","File ID: $fileId")

                            saveFileIdToStorage(context,fileId)

                            GlobalScope.launch(Dispatchers.Main) {
                                downloadProcessedImage(
                                    context = context,
                                    fileId = fileId,
                                    photoViewModel = photoViewModel,
                                    token = token,
                                    onSuccess = { bitmap,prediction ->
                                        processedImage = bitmap
                                        photoViewModel.prediction = prediction
                                        isLoading = false
                                        showDialog = true
                                    },
                                    onError = { error ->
                                        isLoading = false
                                        errorMessage = error
                                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        } else {
                            Log.e("TAG", "Fotoğraf yüklenemedi: ${response.errorBody()?.string()}")
                            isLoading = false
                            errorMessage = "Tekrar deneyiniz."
                        }
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        Log.e("TAG", "Fotoğraf yükleme hatası: ${t.message}")
                        isLoading = false
                        errorMessage = "Tekrar deneyiniz."
                    }
                })
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("TAG", "Hata oluştu: ${e.message}")
                    isLoading = false
                    errorMessage = "Tekrar deneyiniz."
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Fotoğrafı indirme fonksiyonu (arka planda çalışacak şekilde)
    private suspend fun downloadProcessedImage(
        context: Context,
        fileId: String,
        photoViewModel: PhotoViewModel,
        token: String,
        onSuccess: (Bitmap,String) -> Unit,
        onError: (String) -> Unit
    ) {
        isLoading = true
        val apiService = RetrofitClient.apiService
        Log.i("TAG", "İşlenmiş fotoğraf indiriliyor....File ID: $fileId")
        try {
            val response = apiService.getProcessedImage(fileId,"Bearer $token")
            Log.e("TAG","İNİYOOOOR")
            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    Log.e("TAG","GELMISSSSS")
                    val imageBase64 = responseBody.data.image
                    val decodedBytes = android.util.Base64.decode(imageBase64, android.util.Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

                    val prediction = responseBody.data.prediction
                    Log.e("TAG","Tahmin: $prediction")
                    photoViewModel.prediction = prediction

                    isLoading = false
                    onSuccess(bitmap,prediction)
                } else {
                    isLoading = false
                    onError("Sunucudan boş yanıt döndü.")
                }
            } else {
                isLoading = false
                onError("Hata kodu: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            isLoading = false
            onError("Fotoğraf indirme hatası: ${e.message}")
        }
    }
    fun saveFileIdToStorage(context: Context, fileId: String) {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("file_id", fileId).apply()
    }


}
