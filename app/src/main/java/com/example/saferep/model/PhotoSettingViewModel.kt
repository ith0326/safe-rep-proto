package com.example.saferep.model

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import android.provider.MediaStore
import android.util.Base64
import androidx.compose.runtime.mutableStateOf
import org.json.JSONObject


// 촬영 프로세스 전반의 데이터를 관리하는 ViewModel
class PhotoSettingViewModel : ViewModel() {
    // 기존 변수들은 그대로 유지
    val siteName = mutableStateOf("")
    val location = mutableStateOf("")
    val inspectionPart = mutableStateOf("")
    val inspectionDetail = mutableStateOf("")
    val timestamp = mutableStateOf("")
    val capturedImageUris = mutableStateOf<List<Uri>>(emptyList())
    val basePath = mutableStateOf("")

    fun addPhoto(uri: Uri) {
        capturedImageUris.value = capturedImageUris.value + uri
    }

    fun clearPhotos() {
        capturedImageUris.value = emptyList()
    }

    fun saveCapturedImages(context: Context, onResult: (Boolean, String) -> Unit) {
        if (capturedImageUris.value.isEmpty()) {
            onResult(false, "저장할 이미지가 없습니다.")
            return
        }

        viewModelScope.launch {
            val totalImages = capturedImageUris.value.size
            var successCount = 0

            // 백그라운드에서 각 이미지를 순차적으로 저장
            capturedImageUris.value.forEachIndexed {  index, uri ->
                if (saveSingleImage(context, uri, index)) {
                    successCount++
                }
            }

            // 모든 작업 완료 후 결과를 UI 스레드에서 전달
            withContext(Dispatchers.Main) {
                if (successCount == totalImages) {
                    onResult(true, "$totalImages 개의 모든 이미지가 저장되었습니다.")
                } else {
                    onResult(false, "총 $totalImages 개 중 $successCount 개만 저장되었습니다.")
                }
            }
        }
    }

    // --- 이미지 한 장을 저장하고 메타데이터를 쓰는 내부 함수 ---
    private suspend fun saveSingleImage(context: Context, sourceUri: Uri, index: Int): Boolean = withContext(Dispatchers.IO) {
        // 파일명 생성 로직은 동일하게 유지
        val timeStampForFile = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(java.util.Date())
        val fileName = if (location.value.isBlank()) {
            "${inspectionPart.value}_${inspectionDetail.value}_${timeStampForFile}_${index + 1}.jpg"
        } else {
            "${location.value}_${inspectionPart.value}_${inspectionDetail.value}_${timeStampForFile}_${index + 1}.jpg"
        }

        // 1. MediaStore를 사용해 파일 정보 설정
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Pictures/SAFE-REP/하위폴더 경로 지정
                val relativePath = File(Environment.DIRECTORY_PICTURES, "${basePath.value}").path
                put(MediaStore.Images.Media.RELATIVE_PATH, relativePath)
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val newImageUri = context.contentResolver.insert(collection, values) ?: return@withContext false

        try {
            // 2. 원본 이미지(sourceUri)를 새로 생성된 위치(newImageUri)로 복사
            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                context.contentResolver.openOutputStream(newImageUri)?.use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: throw IOException("입력 스트림 열기 실패: $sourceUri")

            // 3. 메타데이터(EXIF) 저장
            context.contentResolver.openFileDescriptor(newImageUri, "rw", null)?.use { pfd ->
                val exif = ExifInterface(pfd.fileDescriptor)

                val jsonData = JSONObject().apply {
                    put("현장명", siteName.value)
                    put("구분", inspectionPart.value)
                    put("세부점검부위", inspectionDetail.value)
                    put("촬영시각", timestamp.value)
                }

                val jsonString = jsonData.toString()
                val base64String = Base64.encodeToString(jsonString.toByteArray(), Base64.DEFAULT)

                exif.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, base64String)
                exif.saveAttributes()
            }

            // 4. IS_PENDING 플래그 해제
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(newImageUri, values, null, null)
            }

            return@withContext true // 모든 작업 성공
        } catch (e: Exception) {
            e.printStackTrace()
            // 실패 시 생성된 미디어 항목 삭제
            context.contentResolver.delete(newImageUri, null, null)
            return@withContext false // 작업 실패
        }
    }
}