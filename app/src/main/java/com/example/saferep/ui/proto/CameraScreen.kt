// ui/proto/CameraScreen.kt

package com.example.saferep.ui.proto

import android.Manifest
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.saferep.model.PhotoSettingViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.media.MediaActionSound
import androidx.compose.runtime.DisposableEffect

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(navController: NavController, viewModel: PhotoSettingViewModel) { // NavController를 파라미터로 받음
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val mediaActionSound = remember { MediaActionSound() }

    // 화면이 사라질 때 리소스를 해제하기 위한 DisposableEffect
    DisposableEffect(Unit) {
        mediaActionSound.load(MediaActionSound.SHUTTER_CLICK)
        onDispose {
            mediaActionSound.release()
        }
    }

    // 권한이 승인되었는지 확인합니다.
    if (cameraPermissionState.status.isGranted) {
        // 권한이 있으면 카메라 UI를 표시합니다.
        CameraView(
            navController = navController,
            onImageCaptured = { uri ->
                imageUris = imageUris + uri
                viewModel.addPhoto(uri)
            },
            onError = { error ->
                Log.e("CameraScreen", "View error:", error)
            },
            mediaActionSound = mediaActionSound
        )
    } else {
        // 권한이 없으면 권한을 요청하는 화면을 표시합니다.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("카메라 권한이 필요합니다. 권한을 허용해주세요.")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                Text("권한 요청")
            }
        }
    }
}

@Composable
private fun CameraView(
    navController: NavController,
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit,
    mediaActionSound: MediaActionSound
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    // ✅ 플래시 상태 관리를 위한 변수들
    var showFlashMenu by remember { mutableStateOf(false) }
    var flashMode by remember { mutableStateOf(ImageCapture.FLASH_MODE_OFF) }
    val flashModeText = when (flashMode) {
        ImageCapture.FLASH_MODE_ON -> "ON"
        ImageCapture.FLASH_MODE_AUTO -> "AUTO"
        else -> "OFF"
    }

    // ✅ flashMode 상태가 변경될 때마다 실제 카메라에 적용
    LaunchedEffect(flashMode, imageCapture) {
        imageCapture?.flashMode = flashMode
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 카메라 미리보기
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val executor = ContextCompat.getMainExecutor(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner, cameraSelector, preview, imageCapture
                        )
                    } catch (e: Exception) {
                        Log.e("CameraView", "Use case binding failed", e)
                    }
                }, executor)
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // ✅ 상단 플래시 설정 버튼 및 메뉴
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Button(
                onClick = { showFlashMenu = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.FlashOn, contentDescription = "Flash")
                Spacer(Modifier.width(8.dp))
                Text("플래시 $flashModeText")
            }
            DropdownMenu(
                expanded = showFlashMenu,
                onDismissRequest = { showFlashMenu = false }
            ) {
                DropdownMenuItem(text = { Text("AUTO") }, onClick = {
                    flashMode = ImageCapture.FLASH_MODE_AUTO
                    showFlashMenu = false
                })
                DropdownMenuItem(text = { Text("ON") }, onClick = {
                    flashMode = ImageCapture.FLASH_MODE_ON
                    showFlashMenu = false
                })
                DropdownMenuItem(text = { Text("OFF") }, onClick = {
                    flashMode = ImageCapture.FLASH_MODE_OFF
                    showFlashMenu = false
                })
            }
        }

        // ✅ 하단 컨트롤 버튼 (촬영, 종료)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(16.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 촬영 종료 버튼
            Button(
                onClick = { navController.navigate("photo_preview") },
                modifier = Modifier
                    .height(56.dp)
                    .width(150.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("촬영 종료", color = Color.White, fontSize = 18.sp)
            }

            // 촬영 버튼
            Button(
                onClick = {
                    mediaActionSound.play(MediaActionSound.SHUTTER_CLICK) // 셔터음 재생
                    imageCapture?.let {
                        takePhoto(it, context, onImageCaptured, onError)
                    }
                },
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF)) // 파란색
            ) {
                Icon(
                    Icons.Default.Camera,
                    contentDescription = "Take Picture",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

// 사진을 찍고 파일로 저장하는 함수 (변경 없음)
private fun takePhoto(
    imageCapture: ImageCapture,
    context: Context,
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    val photoFile = File.createTempFile(
        "JPEG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())}",
        ".jpg",
        context.externalCacheDir
    )
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val savedUri = outputFileResults.savedUri ?: Uri.fromFile(photoFile)
                onImageCaptured(savedUri)
            }

            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        }
    )
}