package com.example.saferep.model

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

// 촬영 프로세스 전반의 데이터를 관리하는 ViewModel
class PhotoSettingViewModel : ViewModel() {
    // PhotoSettingsScreen에서 설정하는 값들
    val siteName = mutableStateOf("")
    val location = mutableStateOf("")
    val inspectionPart = mutableStateOf("") // 점검부위 (상위+세부)
    val inspectionDetail = mutableStateOf("") // 점검부위 (직접입력)
    val timestamp = mutableStateOf("")

    // CameraScreen에서 촬영된 사진들의 URI 목록
    val capturedImageUris = mutableStateOf<List<Uri>>(emptyList())

    // 사진 URI를 목록에 추가하는 함수
    fun addPhoto(uri: Uri) {
        capturedImageUris.value = capturedImageUris.value + uri
    }

    // 사진 목록을 비우는 함수 ('다시 촬영' 시 사용)
    fun clearPhotos() {
        capturedImageUris.value = emptyList()
    }
}