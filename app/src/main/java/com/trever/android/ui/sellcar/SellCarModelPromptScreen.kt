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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel
// import com.trever.android.ui.theme.YourAppTheme // 실제 테마로 교체 필요

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellCarModelPromptScreen(
    sellCarViewModel: SellCarViewModel,
    onNavigateBack: () -> Unit,
    onPromptClicked: () -> Unit // "모델을 선택해주세요" 클릭 시 동작
) {
    val uiState by sellCarViewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { }, // 화면 제목
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
            // 진행 상태 표시기 (ViewModel의 currentStep 사용, 이 화면은 2단계여야 함)
            CustomProgressBar(totalSteps = 7, currentStep = 2)

            Spacer(modifier = Modifier.height(48.dp))

            // 안내 텍스트
            Text(
                text = "차량 모델을 입력해주세요",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(24.dp))

            // "모델을 선택해주세요" 클릭 가능한 영역 (버튼처럼 동작)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onPromptClicked() }, // 클릭 시 다음 단계로 이동
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color.LightGray),
                color = Color.White // 배경색
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 16.dp), // 내부 패딩
                    contentAlignment = Alignment.CenterStart // 텍스트를 왼쪽에 정렬
                ) {
                    Text(
                        text = "모델을 선택해주세요",
                        fontSize = 16.sp, // 플레이스홀더 텍스트 크기
                        color = Color.Gray // 플레이스홀더 텍스트 색상
                    )
                }
            }

            // 키보드 영역은 실제 구현에서는 시스템 키보드가 올라오므로 별도 UI 구성 불필요
            // Spacer(modifier = Modifier.weight(1f)) // 하단 버튼이 있다면 사용
        }
    }
}

//@Preview(showBackground = true, device = "spec:shape=Normal,width=360,height=640,unit=dp,dpi=480")
//@Composable
//fun SellCarModelPromptScreenPreview() {
//    // YourAppTheme를 실제 앱의 테마로 교체하세요.
//    // 예: com.trever.android.ui.theme.TreverAppTheme { ... }
//    YourAppTheme { // 임시 YourAppTheme 사용 (실제 테마로 교체 필요)
//        val previewViewModel = SellCarViewModel()
//        previewViewModel.updateCurrentStep(2) // 이 화면은 2단계
//
//        SellCarModelPromptScreen(
//            sellCarViewModel = previewViewModel,
//            onNavigateBack = {},
//            onPromptClicked = {}
//        )
//    }
//}
