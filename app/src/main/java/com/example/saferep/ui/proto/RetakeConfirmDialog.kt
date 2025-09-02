package com.example.saferep.ui.proto

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.saferep.model.PhotoSettingViewModel

@Composable
fun RetakeConfirmDialog(
    viewModel: PhotoSettingViewModel,
    onDismissRequest: () -> Unit,
    onGoHome: () -> Unit,
    onSaveAndRetake: () -> Unit,
    onRetakeWithoutSaving: () -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface, // Material3 배경색
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 제목
                Text(
                    text = "다시 찍기",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                // 부제목
                Text(
                    text = "세션을 어떻게 처리할지 선택하세요.",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 버튼 그룹
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. 전체 저장 후 다시찍기 버튼
                    Button(
                        onClick = onSaveAndRetake,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        border = BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Text("전체 저장 후 다시찍기")
                    }

                    // 2. 저장하지 않고 다시찍기 버튼
                    Button(
                        onClick = onRetakeWithoutSaving,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFAE8E8), // 연한 붉은색
                            contentColor = Color(0xFFD32F2F)   // 붉은색 텍스트
                        )
                    ) {
                        Text("저장하지 않고 다시찍기")
                    }

                    // 3. 취소 버튼
                    Button(
                        onClick = onGoHome, // 취소는 onDismissRequest 호출
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF0F0F0), // 연한 회색
                            contentColor = Color.Black
                        )
                    ) {
                        Text("취소")
                    }
                }
            }
        }
    }
}