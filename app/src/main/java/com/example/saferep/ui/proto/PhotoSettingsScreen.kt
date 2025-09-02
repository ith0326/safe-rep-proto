package com.example.saferep.ui.proto

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.saferep.model.PhotoSettingViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PhotoSettingsScreen(navController: NavController, siteName: String, viewModel: PhotoSettingViewModel) {
    var location by remember { mutableStateOf("") }
    var directInput by remember { mutableStateOf("") }
    var selectedChip by remember { mutableStateOf("상부") }
    var siteName by remember { mutableStateOf(siteName) }
    val scrollState = rememberScrollState()

    val niceBlue = Color(0xFF6495ED)

    val context = LocalContext.current

    // 현재 시간을 "yyyy-MM-dd HH:mm" 형식으로 포맷팅
    val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    var showDetailBottomSheet by remember { mutableStateOf(false) }
    var selectedDetail by remember { mutableStateOf("") }

    if (showDetailBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showDetailBottomSheet = false },
            sheetState = sheetState
        ) {
            DetailBottomSheetContent(
                mainCategory = selectedChip,
                onItemSelected = { detail ->
                    selectedDetail = detail // 선택된 항목 저장
                    showDetailBottomSheet = false // 바텀 시트 닫기
                },
                onCancel = { showDetailBottomSheet = false } // 취소 시 닫기
            )
        }
    }

    var isLocationFocused by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    var isDirectInputFocused by remember { mutableStateOf(false) }

    var showBottomSheet by remember { mutableStateOf(false) }
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

    LaunchedEffect(selectedChip) {
        if (selectedChip == "직접입력") {
            focusRequester.requestFocus()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("사진 촬영 설정", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { // 뒤로가기
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    viewModel.siteName.value = siteName
                    viewModel.location.value = location
                    viewModel.timestamp.value = currentTime

                    viewModel.inspectionPart.value = selectedChip
                    viewModel.inspectionDetail.value = selectedDetail

                    viewModel.clearPhotos()
                    navController.navigate("camera_screen")
                },
                enabled = selectedDetail != "",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500)) // 오렌지색
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Camera")
                Spacer(modifier = Modifier.width(8.dp))
                Text("카메라 열기", fontSize = 18.sp)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 현재 현장 섹션
            SectionCard(
                title = "현재 현장",
                siteName = siteName,
                onActionClick = { showBottomSheet = true }
            )
            Spacer(modifier = Modifier.height(24.dp))

            // 위치 섹션
            SectionTitle(text = "위치 (선택)")
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                placeholder = { Text(if (isLocationFocused) "입력중" else "직접 입력 (예: 중앙부, 단부, 교각#3)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        isLocationFocused = focusState.isFocused
                    },
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = niceBlue,
                    unfocusedBorderColor = Color.LightGray
                )
            )
            Spacer(modifier = Modifier.height(24.dp))

            // 점검부위 섹션
            SectionTitle(text = "점검부위 *")
            if (selectedChip == "직접입력" || selectedDetail.isBlank()) {
                val chipItems = listOf("상부", "하부", "받침", "케이블", "2차부재", "기타부재", "공중", "직접입력")
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    chipItems.forEach { item ->
                        Chip(text = item, isSelected = selectedChip == item, selectedColor = niceBlue) {
                            if (selectedChip == "직접입력" && item != "직접입력") {
                                selectedDetail = ""
                            }

                            selectedChip = item
                            if (item != "직접입력") {
                                directInput = ""
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = directInput,
                    onValueChange = {
                        directInput = it
                        selectedDetail = it
                    },
                    placeholder = { Text(if (isDirectInputFocused) "입력중" else "직접입력시 활성화") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            isDirectInputFocused = focusState.isFocused
                        },
                    enabled = selectedChip == "직접입력",
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = niceBlue,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (selectedChip.isEmpty()) {
                            Toast.makeText(context, "점검부위를 선택해주세요.", Toast.LENGTH_SHORT).show()
                        } else {
                            showDetailBottomSheet = true
                        }
                    },
                    enabled = selectedChip != "직접입력",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = niceBlue,
                        contentColor = Color.White
                    )
                ) {
                    Text("세부 부위 선택하기", fontSize = 18.sp)
                }
            } else {
                // 이 블록은 이제 '직접입력'이 아닌 다른 칩에서 세부 부위를 선택했을 때만 표시됩니다.
                OutlinedTextField(
                    value = selectedChip + ">" + selectedDetail,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    label = { Text("선택된 세부 부위") },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = Color.LightGray,
                        disabledTextColor = Color.Black,
                        disabledLabelColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        selectedDetail = ""
                        selectedChip = "상부"
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = niceBlue,
                        contentColor = Color.White
                    )
                ) {
                    Text("점검 부위 다시 선택", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 시각 섹션
            SectionTitle(text = "시각 *")
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = currentTime,
                    onValueChange = {},
                    enabled = false,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = niceBlue,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// 각 섹션 제목을 위한 Composable
@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}

// '현재 현장' 카드 UI를 위한 Composable
@Composable
fun SectionCard(title: String, siteName: String, onActionClick: () -> Unit) {
    val niceBlue = Color(0xFF6495ED)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val buttonContainerColor = if (isPressed) niceBlue else Color.White
    val buttonContentColor = if (isPressed) Color.White else niceBlue

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(title, color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(siteName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        Button(
            onClick = onActionClick,
            modifier = Modifier.height(56.dp),
            shape = RoundedCornerShape(8.dp),
            interactionSource = interactionSource,
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonContainerColor,
                contentColor = buttonContentColor
            ),
            border = BorderStroke(1.dp, Color.LightGray)
        ) {
            Text("변경")
        }
    }
}

// '점검부위' 칩 UI를 위한 Composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Chip(text: String, isSelected: Boolean, selectedColor: Color, onClick: () -> Unit) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(text) },
        shape = RoundedCornerShape(50),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = selectedColor,
            selectedLabelColor = Color.White
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = Color.LightGray,
            selectedBorderColor = Color.Transparent
        )
    )
}

@Composable
fun DetailBottomSheetContent(
    mainCategory: String, // "상부", "하부" 등 점검부위 값을 받음
    onItemSelected: (String) -> Unit,
    onCancel: () -> Unit
) {
    // 점검부위에 따라 다른 세부 부위 목록을 결정
    val detailItems = when (mainCategory) {
        "상부" -> listOf("바닥판", "거더", "직접입력")
        "하부" -> listOf("교대", "교각", "주탑", "기초", "직접입력")
        "받침" -> listOf("교량받침", "직접입력")
        "케이블" -> listOf("케이블", "정착구", "행어밴드", "새들", "직접입력")
        "2차부재" -> listOf("가로보", "세로보", "직접입력")
        "기타부재" -> listOf("신축이음", "배수시설", "난간", "연석", "교면포장", "직접입력")
        "공중" -> listOf("추락방지시설", "도로포장", "도로부 신축이음부", "환기구", "직접입력")
        else -> emptyList()
    }

    val displayItems = detailItems + "직접입력"

    val niceBlue = Color(0xFF6495ED)
    val lightGray = Color(0xFFF0F0F0)

    var currentSelection by remember { mutableStateOf(displayItems.firstOrNull() ?: "") }
    var directInputText by remember { mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }
    var isDirectInputFocused by remember { mutableStateOf(false) }

    LaunchedEffect(currentSelection) {
        if (currentSelection == "직접입력") {
            focusRequester.requestFocus()
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxHeight(0.80f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onCancel) { Text("취소") }
            Text("세부 부위 선택", fontWeight = FontWeight.Bold)
            TextButton(
                onClick = {
                    val result = if (currentSelection == "직접입력") {
                        directInputText // '직접입력' 선택 시 텍스트 필드 값을 결과로
                    } else {
                        currentSelection // 그 외에는 선택된 항목 이름을 결과로
                    }
                    onItemSelected(result)
                }
            ) { // '확인' 누르면 선택값 전달
                Text("확인")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // ✅ 3. UI를 Text 대신 Button 목록으로 변경
        LazyColumn {
            items(detailItems) { item ->
                val isSelected = item == currentSelection
                Button(
                    onClick = {
                        currentSelection = item
                        if (item != "직접입력") {
                            directInputText = ""
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) niceBlue else lightGray,
                        contentColor = if (isSelected) Color.White else Color.Black
                    )
                ) {
                    Text(item)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ 5. 직접 입력 텍스트 필드 추가
        OutlinedTextField(
            value = directInputText,
            onValueChange = { directInputText = it },modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged { isDirectInputFocused = it.isFocused },
            placeholder = { Text(if (isDirectInputFocused) "입력중" else "직접입력 선택시 활성화") },
            shape = RoundedCornerShape(8.dp),
            enabled = currentSelection == "직접입력", // '직접입력'이 선택됐을 때만 활성화
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = niceBlue,
                unfocusedBorderColor = Color.LightGray
            )
        )
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
}