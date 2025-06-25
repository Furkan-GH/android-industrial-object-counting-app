package com.plcoding.cameraxguide

import com.plcoding.cameraxguide.TokenManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.gestures.transformable
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import android.content.ContentValues
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.compose.material.icons.filled.Download
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.media.session.MediaSession.Token
import androidx.compose.material3.RadioButton
import androidx.compose.ui.zIndex


// Fotograf seçme ve gönderme UI
@Composable
fun PhotoBottomSheetContent(
    bitmaps: List<Bitmap>,
    modifier: Modifier = Modifier
) {
    val photoViewModel: PhotoViewModel = viewModel()
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val context = LocalContext.current

    val getContent = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        uri: Uri? -> uri?.let{
        val bitmap = getBitmapFromUri(context, it)
            selectedBitmap = bitmap
        }
    }
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ){
            IconButton(
                onClick = {
                    getContent.launch("image/*")
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = "Galeriden seç"
                )
            }
        }
    }

    if (bitmaps.isEmpty()) {
        Box(
            modifier = modifier.padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Fotoğraf bulunamadı.")
        }
    } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalItemSpacing = 16.dp,
                contentPadding = PaddingValues(16.dp),
                modifier = modifier.fillMaxHeight()
            ) {
                items(bitmaps) { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                selectedBitmap = bitmap
                            }
                    )
                }
            }
        }

    if (selectedBitmap != null) {
        Dialog(
            onDismissRequest = { selectedBitmap = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                val configuration = LocalConfiguration.current
                val density = LocalDensity.current

                val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }
                val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }

                var scale by remember { mutableFloatStateOf(1f) }
                var offset by remember { mutableStateOf(Offset.Zero) }
                var selectedOption by remember { mutableStateOf<String?>(null) }

                val state = rememberTransformableState { zoomChange, panChange, _ ->
                    scale = (scale * zoomChange).coerceIn(1f, 5f)

                    val imageWidth = screenWidth * scale
                    val imageHeight = screenHeight * scale

                    val maxX = (imageWidth - screenWidth) / 2
                    val maxY = (imageHeight - screenHeight) / 2

                    val newOffset = offset + panChange * scale

                    offset = Offset(
                        x = newOffset.x.coerceIn(-maxX, maxX),
                        y = newOffset.y.coerceIn(-maxY, maxY)
                    )
                }

                val doubleTapModifier = Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            scale = if (scale == 1f) 2f else 1f
                            offset = Offset.Zero
                        }
                    )
                }

                Image(
                    bitmap = selectedBitmap!!.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .transformable(state)
                        .then(doubleTapModifier),
                    contentScale = ContentScale.Fit
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White.copy(alpha = 0.7f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = selectedOption == "Palet",
                                onClick = { selectedOption = "Palet" }
                            )
                            Text(
                                text = "Palet",
                                fontWeight = if (selectedOption == "Palet") FontWeight.Bold else FontWeight.Normal
                            )


                            RadioButton(
                                selected = selectedOption == "Odun",
                                onClick = { selectedOption = "Odun" }
                            )
                            Text(
                                text = "Kereste",
                                fontWeight = if (selectedOption == "Odun") FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    val isButtonEnabled = selectedOption != null
                    val tokenManager = TokenManager(context)
                    val token = tokenManager.getToken() ?: ""
                    Button(
                        onClick = {
                            if (selectedOption != null) {
                                selectedBitmap?.let { bitmap ->
                                    photoViewModel.viewModelScope.launch {
                                        photoViewModel.sendPhoto(
                                            bitmap = bitmap,
                                            context = context,
                                            photoViewModel = photoViewModel,
                                            selectedType = selectedOption!!,
                                            token
                                        )
                                    }
                                }
                                selectedBitmap = null
                            }
                        },
                        enabled = isButtonEnabled,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isButtonEnabled) Color.Red else Color.Gray
                        )
                    ) {
                        Text("Nesneleri say")
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { selectedBitmap = null },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Kapat")
                    }
                }
            }
        }
    }


    if (photoViewModel.isLoading) {
        LoadingDialog(isLoading = true)
    }

    if (photoViewModel.processedImage != null && photoViewModel.showDialog) {
        DisplayProcessedImage(
            bitmap = photoViewModel.processedImage!!,
            prediction = photoViewModel.prediction ?:"") {
            photoViewModel.showDialog = false
        }
    }

    photoViewModel.errorMessage?.let { error ->
        Text(
            text = "Error: $error",
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.wrapContentSize(Alignment.Center)
        )
    }
}

fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            // Use BitmapFactory.decodeStream for better compatibility and handling
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                BitmapFactory.decodeStream(inputStream)
            } else {
                null
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
// Yükleniyor dialogu
@Composable
fun LoadingDialog(isLoading: Boolean) {
    if (isLoading) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Hesaplama yapılıyor...") },
            text = { Text("Fotoğrafınız işlenirken lütfen bekleyin.") },
            confirmButton = {

            }
        )
    }
}

// İşlenmiş fotoğrafı gösteren UI
@Composable
fun DisplayProcessedImage(bitmap: Bitmap, prediction: String, onClose: () -> Unit) {
    Dialog(
        onDismissRequest = { onClose() },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            var scale by remember { mutableFloatStateOf(1f) }
            var offset by remember { mutableStateOf(Offset.Zero) }

            val screenWidth = with(LocalDensity.current) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
            val screenHeight = with(LocalDensity.current) { LocalConfiguration.current.screenHeightDp.dp.toPx() }
            val context = LocalContext.current
            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    saveBitmapToGallery(context, bitmap)
                } else {
                    Toast.makeText(context, "İzin reddedildi", Toast.LENGTH_SHORT).show()
                }
            }

            val state = rememberTransformableState { zoomChange, panChange, _ ->
                scale = (scale * zoomChange).coerceIn(1f, 5f)

                val imageWidth = screenWidth * scale
                val imageHeight = screenHeight * scale

                val maxX = (imageWidth - screenWidth) / 2
                val maxY = (imageHeight - screenHeight) / 2

                val newOffset = offset + panChange * scale

                offset = Offset(
                    x = newOffset.x.coerceIn(-maxX, maxX),
                    y = newOffset.y.coerceIn(-maxY, maxY)
                )
            }

            val doubleTapModifier = Modifier.pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        scale = if (scale == 1f) 2f else 1f
                        offset = Offset.Zero
                    }
                )
            }

            IconButton(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        saveBitmapToGallery(context, bitmap)
                    } else {
                        permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        Log.e("Warning","Olmadı")
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
                    .zIndex(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "İndir",
                    tint = Color.Black
                )
            }

            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
                    .transformable(state)
                    .then(doubleTapModifier),
                contentScale = ContentScale.Fit
            )


            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.8f))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Tespit edilen sayı: $prediction",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Button(
                    onClick = { onClose() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Kapat")
                }
            }
        }
    }
}

fun saveBitmapToGallery(context: Context, bitmap: Bitmap): Boolean {
    return try {
        val filename = "IMG_${System.currentTimeMillis()}.jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ) ?: run {
            Toast.makeText(context, "Galeriye erişilemedi", Toast.LENGTH_SHORT).show()
            return false
        }

        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)) {
                Toast.makeText(context, "Fotoğraf sıkıştırılamadı", Toast.LENGTH_SHORT).show()
                return false
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                context.contentResolver.update(uri, contentValues, null, null)
            }

            Toast.makeText(context, "Fotoğraf galeriye kaydedildi", Toast.LENGTH_SHORT).show()
            true
        } ?: run {
            Toast.makeText(context, "Dosya oluşturulamadı", Toast.LENGTH_SHORT).show()
            false
        }
    } catch (e: Exception) {
        Log.e("SaveImage", "Fotoğraf kaydedilirken hata: ${e.message}")
        Toast.makeText(context, "Hata: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        false
    }
}




