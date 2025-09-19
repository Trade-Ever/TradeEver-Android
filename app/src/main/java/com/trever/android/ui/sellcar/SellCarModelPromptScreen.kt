package com.trever.android.ui.sellcar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
// import androidx.compose.ui.text.style.TextAlign // 현재 사용되지 않음
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellCarModelPromptScreen(
    sellCarViewModel: SellCarViewModel,
    onSystemBack: () -> Unit,    // ArrowBack 아이콘용
    onStepBack: () -> Unit,      // 하단 "이전" 버튼용
    onNextClicked: () -> Unit    // "모델 선택" Surface 클릭 또는 하단 "다음" 버튼용
    // (기존 onPromptClicked를 onNextClicked로 변경 또는 통합)
) {
    val uiState by sellCarViewModel.uiState.collectAsState()
    val purpleColor = Color(0xFF6A11CB) // 다음 버튼 색상용

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onSystemBack) { // onSystemBack 사용
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로 가기"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CustomProgressBar(totalSteps = 7, currentStep = uiState.currentStep) // ViewModel의 현재 스텝 사용

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "차량 모델을 입력해주세요",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onNextClicked() }, // 클릭 시 다음 단계로 이동 (하단 버튼과 동일 액션)
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color.LightGray),
                color = Color.White
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    // 실제 선택된 모델명을 표시하도록 ViewModel의 상태를 사용
                    Text(
                        text = if (uiState.selectedModel.isNullOrBlank()) "모델을 선택해주세요" else uiState.selectedModel!!,
                        fontSize = 16.sp,
                        color = if (uiState.selectedModel.isNullOrBlank()) Color.Gray else Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f)) // 버튼들을 하단에 위치시키기 위한 Spacer

            // ▼▼▼ 이전/다음 버튼 추가 ▼▼▼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 이전 버튼
                OutlinedButton(
                    onClick = onStepBack, // 하단 "이전" 버튼 클릭 시 실행
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    border = BorderStroke(1.dp, Color.LightGray),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Text(text = "이전", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                // 다음 버튼
                Button(
                    onClick = onNextClicked, // "모델 선택" Surface 클릭과 동일한 액션
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = purpleColor,
                        disabledContainerColor = Color.LightGray
                    ),
                    // "다음" 버튼 활성화 조건: ViewModel에서 모델이 선택되었는지 확인
                    enabled = !uiState.selectedModel.isNullOrBlank(),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Text("다음", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            // ▲▲▲ 이전/다음 버튼 추가 ▲▲▲
        }
    }
}

// Preview 수정: 두 가지 콜백을 받도록
//@Preview(showBackground = true, device = "spec:shape=Normal,width=360,height=640,unit=dp,dpi=480")
//@Composable
//fun SellCarModelPromptScreenPreview() {
//    MaterialTheme { // 실제 앱 테마로 교체 권장
//        val previewViewModel = SellCarViewModel()
//        previewViewModel.updateCurrentStep(2) // 이 화면은 2단계
//        // previewViewModel.updateSelectedModel("현대 아반떼 (미리보기)") // 미리보기에 모델명 표시 예시
//
//        SellCarModelPromptScreen(
//            sellCarViewModel = previewViewModel,
//            onSystemBack = {},
//            onStepBack = {},
//            onNextClicked = {}
//        )
//    }
//}

