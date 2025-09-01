package com.example.saferep.ui.proto

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    // 화면의 상태를 기억하는 변수 (현장 이름)
    var siteName by remember { mutableStateOf("") }
    var isTextFieldFocused by remember { mutableStateOf(false) }

    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val lightGray = Color(0xFFE0E0E0)
    val darkGray = Color(0xFF616161)
    val lightBlue = Color(0xFF6495ED)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val buttonContainerColor = if (isPressed) lightBlue else Color.White
    val buttonContentColor = if (isPressed) Color.White else lightBlue

    val settingsButtonInteractionSource = remember { MutableInteractionSource() }
    val isSettingsButtonPressed by settingsButtonInteractionSource.collectIsPressedAsState()
    val settingsButtonContainerColor = if (isSettingsButtonPressed) lightBlue else Color.White
    val settingsButtonContentColor = if (isSettingsButtonPressed) Color.White else lightBlue

    val context = LocalContext.current

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false }, // 배경 클릭 시 닫기
            sheetState = sheetState
        ) {
            // ModalBottomSheet 안에 들어갈 내용 (새로운 화면)
            BottomSheetContent(
                onConfirm = { selectedSite ->
                    // 확인 버튼을 누르면 선택된 현장 이름을 TextField에 반영
                    siteName = selectedSite
                    showBottomSheet = false
                },
                onCancel = { showBottomSheet = false } // 취소 버튼을 누르면 닫기
            )
        }
    }

    // 전체 화면 레이아웃
    val value = Column(
        modifier = Modifier
            .fillMaxSize() // 화면 전체 채우기
            .padding(horizontal = 24.dp, vertical = 16.dp), // 좌우, 상하 여백ㅁㄴ
        horizontalAlignment = Alignment.CenterHorizontally // 자식 요소들 가로 중앙 정렬
    ) {

        // 1. 상단 타이틀
        Text(
            text = "SAFE- REP",
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(vertical = 48.dp)
        )

        // 2. 현장 설정 섹션
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start // 이 섹션 내에서는 왼쪽 정렬
        ) {
            Text(
                text = "현장 설정",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // 2-1. 현장 이름 입력 필드와 버튼
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = siteName,
                    onValueChange = { siteName = it },
                    placeholder = {
                        Text(
                            text = if (isTextFieldFocused) "입력중" else "현장 이름 입력",
                            color = darkGray
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { focusState ->
                            isTextFieldFocused = focusState.isFocused
                        },
                    shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = lightBlue,
                        unfocusedBorderColor = lightGray
                    )
                )
                Button(
                    onClick = { showBottomSheet = true },
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                    interactionSource = interactionSource,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonContainerColor,
                        contentColor = buttonContentColor
                    ),
                    border = BorderStroke(1.dp, lightGray)
                ) {
                    Text("불러오기")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2-2. 사진 촬영 시작 버튼
            if (siteName.isNotBlank()) {
                Button(
                    onClick = { navController.navigate("photo_settings/$siteName") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = lightBlue, // 배경색: niceBlue
                        contentColor = Color.White    // 내용색: 흰색
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "카메라 아이콘",
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "사진 촬영 시작", fontSize = 18.sp)
                    }
                }
            } else {
                // ✅ siteName이 비어있는 경우 (비활성 상태)
                OutlinedButton(
                    onClick = {
                        Toast.makeText(context, "현장 이름은 필수값입니다.", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, lightGray),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = darkGray)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "카메라 아이콘",
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "사진 촬영 시작", fontSize = 18.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 3. 설정 버튼
        Button(
            onClick = { /* '설정' 버튼 클릭 시 동작 */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(8.dp),
            interactionSource = settingsButtonInteractionSource,
            colors = ButtonDefaults.buttonColors(
                containerColor = settingsButtonContainerColor,
                contentColor = settingsButtonContentColor
            ),
            border = BorderStroke(1.dp, lightGray)
        ) {
            Text("설정", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        // 빈 공간을 채워 하단 텍스트를 맨 아래로 밀어냄
        Spacer(modifier = Modifier.weight(1f))

        // 4. 하단 버전 및 회사 정보
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "v1.0.0",
                color = darkGray,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "INNOSAFE",
                color = darkGray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Android Studio에서 미리보기 위한 코드
@Preview(showBackground = true)
@Composable
fun HomePreview() {
    val navController = rememberNavController()
    HomeScreen(navController = navController)
}

@Composable
fun BottomSheetContent(onConfirm: (String) -> Unit, onCancel: () -> Unit) {
    val sites = listOf("장흥교", "장형교", "전조1교") // 현장 목록
    var selectedSite by remember { mutableStateOf(sites[0]) }

    val niceBlue = Color(0xFF1E88E5)
    val lightGray = Color(0xFFF0F0F0)

    Column(
        modifier = Modifier
            .fillMaxHeight(0.80f)
            .padding(16.dp)
    ) {
        // 상단 메뉴 (취소, 현장 정보, 확인)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onCancel) {
                Text("취소")
            }
            Text("현장 정보", fontWeight = FontWeight.Bold)
            TextButton(onClick = { onConfirm(selectedSite) }) {
                Text("확인")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 현장 목록 버튼
        sites.forEach { site ->
            val isSelected = site == selectedSite
            Button(
                onClick = { selectedSite = site },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) niceBlue else lightGray,
                    contentColor = if (isSelected) Color.White else Color.Black
                )
            ) {
                Text(site)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}