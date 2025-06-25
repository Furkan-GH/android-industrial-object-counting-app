@file:OptIn(ExperimentalMaterial3Api::class)

package com.plcoding.cameraxguide.screens

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.camera.view.CameraController
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.*
import android.content.Context
import androidx.compose.material.icons.filled.History
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.plcoding.cameraxguide.CameraPreview
import com.plcoding.cameraxguide.MainViewModel
import com.plcoding.cameraxguide.PhotoBottomSheetContent
import com.plcoding.cameraxguide.nav.Routes
import com.plcoding.cameraxguide.ui.theme.CameraXGuideTheme


@Composable
fun CameraScreen(navController: NavHostController) {
    CameraXGuideTheme {
        val scope = rememberCoroutineScope()
        val scaffoldState = rememberBottomSheetScaffoldState()
        val snackbarHostState = remember { SnackbarHostState() }
        val context = LocalContext.current

        val controller = remember {
            LifecycleCameraController(context).apply {
                setEnabledUseCases(
                    CameraController.IMAGE_CAPTURE or
                            CameraController.VIDEO_CAPTURE
                )
            }
        }
        val viewModel = viewModel<MainViewModel>()
        val bitmaps by viewModel.bitmaps.collectAsState()
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Camera Screen Loaded!") // <-- bu satırı EKRANA yaz
        }
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = 0.dp,
            sheetContent = {
                PhotoBottomSheetContent(
                    bitmaps = bitmaps,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 740.dp)
                ) { snackbarData ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(500)) +
                                slideInVertically(animationSpec = tween(500)) { -40 },
                        exit = fadeOut(animationSpec = tween(1950)) +
                                slideOutVertically(animationSpec = tween(1950)) { -90 }
                    ) {
                        Snackbar(
                            snackbarData = snackbarData,
                            containerColor = Color.White,
                            contentColor = Color.Black,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                CameraPreview(
                    controller = controller,
                    modifier = Modifier.fillMaxSize()
                )

                IconButton(
                    onClick = {
                        controller.cameraSelector =
                            if (controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                                CameraSelector.DEFAULT_FRONT_CAMERA
                            } else CameraSelector.DEFAULT_BACK_CAMERA
                    },
                    modifier = Modifier.offset(16.dp, 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Switch camera"
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                scaffoldState.bottomSheetState.expand()
                            }
                        },
                        modifier = Modifier
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(50)
                            )
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Photo,
                            contentDescription = "Galeriyi aç"
                        )
                    }
                    IconButton(
                        onClick = {
                            takePhoto(
                                context = context,
                                controller = controller,
                                onPhotoTaken = viewModel::onTakePhoto,
                                snackbarHostState = snackbarHostState
                            )
                        },
                        modifier = Modifier
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(50)
                            )
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Fotoğraf çek"
                        )
                    }
                    IconButton(
                        onClick = {
                            navController.navigate(Routes.LOG)
                        },
                        modifier = Modifier
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(50)
                            )
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History, // veya Description, Article, ListAlt
                            contentDescription = "Kayıtlar",
                            tint = Color.Black
                        )
                    }
                }
            }
        }
    }
}

private fun takePhoto(
    context: Context,
    controller: LifecycleCameraController,
    onPhotoTaken: (Bitmap) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val scope = CoroutineScope(Dispatchers.Default)
    controller.takePicture(
        ContextCompat.getMainExecutor(context.applicationContext),
        object : OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)

                scope.launch(Dispatchers.Default) {
                    val matrix = Matrix().apply {
                        postRotate(image.imageInfo.rotationDegrees.toFloat())
                    }
                    val rotatedBitmap = Bitmap.createBitmap(
                        image.toBitmap(),
                        0,
                        0,
                        image.width,
                        image.height,
                        matrix,
                        true
                    )
                    image.close()
                    withContext(Dispatchers.Main) {
                        onPhotoTaken(rotatedBitmap)
                        val snackbarJob = launch {
                            snackbarHostState.showSnackbar(
                                message = "Fotoğraf çekildi!",
                                duration = SnackbarDuration.Indefinite
                            )
                        }
                        delay(1000)
                        snackbarJob.cancel()
                    }
                }
            }

            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
                Log.e("Camera", "Couldn't take photo: ", exception)
            }
        }
    )
}
