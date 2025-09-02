package com.example.saferep.ui.proto

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.saferep.model.PhotoSettingViewModel
import com.google.accompanist.pager.*

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PhotoPreviewScreen(
    navController: NavController,
    viewModel: PhotoSettingViewModel
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState()
    val images = viewModel.capturedImageUris.value

    var showConfirmDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("촬영 결과 미리보기", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                // 저장 버튼
                Button(
                    onClick = {
                        // TODO: 최종 저장 로직 구현
                        // 1. viewModel의 정보로 파일 경로 및 이름 생성
                        // 2. images 목록의 임시 파일들을 최종 경로로 복사/이동
                        // 3. (선택) 사진에 메타데이터 워터마크 추가
                        Toast.makeText(context, "저장 기능은 구현해야 합니다.", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Save")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "저장",
                        fontSize = 12.sp,
                        softWrap = false
                    )
                }
                // 처음으로 버튼
                Button(
                    onClick = {
                        showConfirmDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500)),
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.Home, contentDescription = "Home")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "처음으로",
                        fontSize = 12.sp,
                        softWrap = false
                    )
                }
                // 다시 촬영 버튼
                Button(
                    onClick = {
                        // ViewModel의 사진 목록 초기화 후 이전 화면(카메라)으로 이동
                        viewModel.clearPhotos()
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = "Retake")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "다시 촬영",
                        fontSize = 12.sp,
                        softWrap = false
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 이미지 페이저 (좌우 스크롤)
            Box(modifier = Modifier.weight(1f)) {
                HorizontalPager(
                    count = images.size,
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = images[page],
                            contentDescription = "Captured Photo ${page + 1}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                        // 정보 오버레이
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text("현장명: ${viewModel.siteName.value}", color = Color.White, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("위치: ${viewModel.location.value}", color = Color.White, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            val inspectionText = viewModel.inspectionPart.value + " > " + viewModel.inspectionDetail.value
                            Text("점검부위: $inspectionText", color = Color.White, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("촬영시각: ${viewModel.timestamp.value}", color = Color.White, fontSize = 14.sp)
                        }
                    }
                }
                // 페이지 카운터 (예: 3/8)
                Text(
                    text = "${pagerState.currentPage + 1} / ${images.size}",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    color = Color.White,
                    fontSize = 14.sp
                )
            }

            // 페이저 인디케이터 (점)
            HorizontalPagerIndicator(
                pagerState = pagerState,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 16.dp)
            )

            // 저장 위치 정보
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("저장 위치", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    val siteName = viewModel.siteName.value
                    val datePart = viewModel.timestamp.value.split(" ").firstOrNull() ?: ""
                    val location = viewModel.location.value
                    val inspectionPart = viewModel.inspectionPart.value
                    val inspectionDetail = viewModel.inspectionDetail.value

                    val basePath = if (location.isBlank()) {
                        "SAFE-REP/${siteName}/${datePart}/"
                    } else {
                        "SAFE-REP/${siteName}/${datePart}/${location}/${inspectionPart}/${inspectionDetail}/"
                    }

                    // 저장경로
                    viewModel.basePath.value = basePath

                    Text("Pictures/" + basePath, fontSize = 14.sp)
                }
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                // 모달 바깥쪽을 클릭하거나 뒤로가기 버튼을 눌렀을 때 모달을 닫음
                showConfirmDialog = false
            },
            title = {
                Text(text = "확인")
            },
            text = {
                Text(text = "사진 저장 후 홈 화면으로 이동합니다.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (!isSaving) { // 중복 실행 방지
                            isSaving = true // 저장 시작 상태로 변경
                            // ViewModel의 파일 저장 함수 호출
                            viewModel.saveCapturedImages(context) { success, message ->
                                // 저장 완료 후 UI 스레드에서 실행
                                isSaving = false // 저장 완료 상태로 변경
                                showConfirmDialog = false // 모달 닫기

                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()

                                if (success) {
                                    // 저장 성공 시 홈 화면으로 이동
                                    viewModel.clearPhotos() // 저장 후 임시 사진 목록 초기화 (선택 사항)
                                    navController.navigate("home") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                } else {
                                    // 저장 실패 시 추가적인 사용자 안내 또는 로직
                                }
                            }
                        }
                    },
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp)) // 저장 중 로딩 표시
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("저장 중...")
                    } else {
                        Text("확인")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false // 모달 닫기
                    }
                ) {
                    Text("취소")
                }
            }
        )
    }
}