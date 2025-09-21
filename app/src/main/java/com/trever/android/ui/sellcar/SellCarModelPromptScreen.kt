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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel
import com.trever.android.ui.theme.AppTheme
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellCarModelPromptScreen(
    sellCarViewModel: SellCarViewModel,
    onSystemBack: () -> Unit,
    onStepBack: () -> Unit,
    onSelectModelPathClicked: () -> Unit, // 모델 선택 플로우 시작 (Surface 클릭)
    onConfirmAndProceedClicked: () -> Unit, // 선택 완료 후 다음 단계로 ("다음" 버튼)
//    onSkipAndProceedClicked: () -> Unit // 건너뛰고 다음 단계로 ("건너뛰고 직접 입력" 버튼)
) {
    val uiState by sellCarViewModel.uiState.collectAsState()
    val purpleColor = Color(0xFF6A11CB)

    val isModelSelected = uiState.selectedManufacturer.isNotBlank() && 
                          uiState.selectedModel.isNotBlank() && 
                          uiState.selectedYear != Calendar.getInstance().get(Calendar.YEAR) // 초기값이 아닌지 확인

    val displayText = if (isModelSelected) {
        "${uiState.selectedManufacturer} ${uiState.selectedModel} ${uiState.selectedYear}"
    } else {
        "모델을 선택해주세요"
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onSystemBack) {
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
            CustomProgressBar(totalSteps = 7, currentStep = uiState.currentStep)
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "차량 모델을 입력해주세요",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(24.dp))

            // 모델 선택 영역 (클릭 시 제조사 선택부터 시작)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSelectModelPathClicked() }, // 제조사 선택 플로우 시작
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color.LightGray),
                color = Color.White
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = displayText,
                        fontSize = 16.sp,
                        color = if (isModelSelected) Color.Black else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

//            // 건너뛰기 버튼
//            OutlinedButton(
//                onClick = onSkipAndProceedClicked,
//                modifier = Modifier.fillMaxWidth().height(56.dp),
//                shape = RoundedCornerShape(8.dp),
//                colors = ButtonDefaults.outlinedButtonColors(
//                    contentColor = purpleColor
//                ),
//                border = BorderStroke(1.dp, purpleColor)
//            ) {
//                Text("건너뛰고 직접 입력", fontSize = 16.sp)
//            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onStepBack,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    border = BorderStroke(1.dp, Color.LightGray)
                ) {
                    Text(text = "이전", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onConfirmAndProceedClicked, // 선택 완료 후 다음으로
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = purpleColor,
                        disabledContainerColor = Color.LightGray
                    ),
                    enabled = isModelSelected, // 모델 정보가 모두 선택되었을 때만 활성화
                ) {
                    Text("다음", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun SellCarModelPromptScreenPreview() {
//    AppTheme {
//        val previewViewModel = SellCarViewModel()
//        previewViewModel.updateCurrentStep(2)
//        // previewViewModel.updateSelectedManufacturer("현대")
//        // previewViewModel.updateSelectedModel("아반떼")
//        // previewViewModel.updateSelectedYear(2023)
//
//        SellCarModelPromptScreen(
//            sellCarViewModel = previewViewModel,
//            onSystemBack = {},
//            onStepBack = {},
//            onSelectModelPathClicked = {},
//            onConfirmAndProceedClicked = {},
//            onSkipAndProceedClicked = {}
//        )
//    }
//}
