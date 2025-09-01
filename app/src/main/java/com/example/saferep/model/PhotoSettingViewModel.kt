package com.example.saferep.model

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.activity.result.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.intl.Locale
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.type.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import kotlin.io.path.copyTo
import kotlin.io.path.exists
import kotlin.io.path.setAttribute
import kotlin.text.format

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
    val basePath = mutableStateOf("")

    // 사진 URI를 목록에 추가하는 함수
    fun addPhoto(uri: Uri) {
        capturedImageUris.value = capturedImageUris.value + uri
    }

    // 사진 목록을 비우는 함수 ('다시 촬영' 시 사용)
    fun clearPhotos() {
        capturedImageUris.value = emptyList()
    }

    fun saveCapturedImages(context: Context, onResult: (Boolean, String) -> Unit) {
        if (capturedImageUris.value.isEmpty()) {
            onResult(false, "저장할 이미지가 없습니다.")
            return
        }

        viewModelScope.launch {
            try {
                val successfulSaves = mutableListOf<String>()
                val failedSaves = mutableListOf<String>()

                val baseDirName = "SAFE-REP"

                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val appBaseDir = File(picturesDir, baseDirName)
                val finalSaveDir = File(appBaseDir, basePath.value)

                if (!finalSaveDir.exists()) {
                    finalSaveDir.mkdirs() // 필요한 모든 상위 디렉토리 생성
                }

                capturedImageUris.value.forEachIndexed { index, uri ->
                    val timeStampForFile = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(
                        java.util.Date()
                    ) // SimpleDateFormat 패턴 수정 (기존 파일명 생성 로직과 일치하도록)
                    val actualLocation = location.value // 지역 변수로 저장하여 중복 호출 방지
                    val actualInspectionPart = inspectionPart.value
                    val actualInspectionDetail = inspectionDetail.value

                    // 파일명 생성 로직을 ViewModel의 다른 부분과 일치시킴
                    val fileName = if (actualLocation.isBlank()) {
                        "${actualInspectionPart}_${actualInspectionDetail}_${timeStampForFile}_${index + 1}.jpg"
                    } else {
                        "${actualLocation}_${actualInspectionPart}_${actualInspectionDetail}_${timeStampForFile}_${index + 1}.jpg"
                    }
                    val outputFile = File(finalSaveDir, fileName)

                    var fileSavedSuccessfully = false
                    withContext(Dispatchers.IO) {
                        try {
                            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                                FileOutputStream(outputFile).use { outputStream ->
                                    inputStream.copyTo(outputStream)
                                    fileSavedSuccessfully = true // 파일 복사 성공
                                }
                            } ?: throw IOException("Failed to open input stream for $uri")

                            // --- 메타데이터 추가 시작 ---
                            if (fileSavedSuccessfully) {
                                try {
                                    val exif = ExifInterface(outputFile.absolutePath)

                                    // 표준 EXIF 태그 또는 사용자 정의 태그 사용 가능
                                    exif.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, "현장: ${siteName.value}, 위치: $actualLocation, 점검부위: $actualInspectionPart, 세부: $actualInspectionDetail")
                                    exif.setAttribute(ExifInterface.TAG_USER_COMMENT, "촬영 앱: SAFE-REP, 현장명: ${siteName.value}, 점검상세: $actualInspectionPart - $actualInspectionDetail")
                                    exif.setAttribute(ExifInterface.TAG_MAKE, "SAFE-REP App") // 제조사 정보 (앱 이름 등)
                                    exif.setAttribute(ExifInterface.TAG_MODEL, "Field Inspection Photo") // 모델 정보

                                    // 촬영 시각 (timestamp는 "yyyy-MM-dd HH:mm:ss" 형식이라고 가정)
                                    // EXIF는 "YYYY:MM:DD HH:MM:SS" 형식을 주로 사용
                                    val exifDateTime = timestamp.value.replaceFirst("-", ":").replaceFirst("-", ":")
                                    exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, exifDateTime) // 원본 촬영 시각
                                    exif.setAttribute(ExifInterface.TAG_DATETIME_DIGITIZED, exifDateTime) // 디지털화 시각
                                    exif.setAttribute(ExifInterface.TAG_DATETIME, SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault()).format(
                                        Date()
                                    )) // 파일 변경 시각 (현재 시각)

                                    // GPS 정보 (별도 수집 필요)
                                    // exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, "위도값")
                                    // exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "N 또는 S")
                                    // exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, "경도값")
                                    // exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "E 또는 W")

                                    exif.saveAttributes() // 변경된 메타데이터 저장
                                    successfulSaves.add(outputFile.name)
                                } catch (exifError: Exception) {
                                    // 메타데이터 저장 실패 시 로깅. 파일 자체는 저장되었을 수 있음.
                                    e.printStackTrace()
                                    // 메타데이터 실패를 어떻게 처리할지 결정 (예: 성공 목록에는 추가하되, 경고 메시지 포함)
                                    // 여기서는 메타데이터 실패도 실패로 간주하지 않고 파일 저장 성공으로 처리 (요구사항에 따라 변경)
                                    // 만약 메타데이터 저장이 필수라면 failedSaves에 추가하고 success 로직에서 제외해야 함.
                                    // 여기서는 일단 파일 복사가 성공했으면 successfulSaves에 추가된 상태이므로, 추가 작업 없이 로깅만 합니다.
                                    println("메타데이터 저장 실패 for ${outputFile.name}: ${exifError.message}")
                                    // 메타데이터가 중요하여 실패 시 전체 실패로 간주한다면 아래 주석 해제
                                    // throw IOException("메타데이터 저장 실패: ${exifError.message}", exifError)
                                }
                            }
                            // --- 메타데이터 추가 끝 ---

                        } catch (e: Exception) {
                            fileSavedSuccessfully = false
                            failedSaves.add("Image ${index + 1} (${fileName}): ${e.message}")
                            e.printStackTrace()
                        }
                    }
                }

                if (failedSaves.isEmpty() && successfulSaves.isNotEmpty()) {
                    onResult(true, "${successfulSaves.size}개의 모든 이미지가 저장되었습니다.")
                } else if (successfulSaves.isNotEmpty()) {
                    onResult(false, "${successfulSaves.size}개 저장 성공 (일부 메타데이터 오류 가능성 있음), ${failedSaves.size}개 저장 실패.\n실패: ${failedSaves.joinToString()}")
                } else {
                    onResult(false, "이미지 저장에 실패했습니다.\n${failedSaves.joinToString()}")
                }

            } catch (e: Exception) {
                onResult(false, "이미지 저장 중 오류 발생: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}