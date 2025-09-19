package com.trever.android.ui.sellcar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellCarAccidentHistoryScreen(
    sellCarViewModel: SellCarViewModel,
    onSystemBack: () -> Unit,    // ArrowBack 아이콘용
    onStepBack: () -> Unit,      // 하단 "이전" 버튼용
    onNextClicked: () -> Unit
) {
    val uiState by sellCarViewModel.uiState.collectAsState()
    var accidentDetails by remember { mutableStateOf(uiState.accidentDetails) }
    val purpleColor = Color(0xFF6A11CB)
    val lightPurpleColor = Color(0xFF9F72FF)

    LaunchedEffect(uiState.hasAccidentHistory) {
        if (uiState.hasAccidentHistory == false) {
            accidentDetails = ""
        }
    }

    // ViewModel의 accidentDetails가 변경되면 로컬 상태도 업데이트
    LaunchedEffect(uiState.accidentDetails) {
        if (accidentDetails != uiState.accidentDetails) {
            accidentDetails = uiState.accidentDetails
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onSystemBack) { // onSystemBack 사용
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로 가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
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
            // CustomProgressBar는 uiState.currentStep 또는 고정값 중 원래 의도대로 사용
            // 여기서는 원본처럼 6으로 두겠습니다.
            // ViewModel 연동 시에는 uiState.currentStep 사용 권장.
            CustomProgressBar(totalSteps = 7, currentStep = uiState.currentStep) // ViewModel 값 사용 권장

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Text("사고이력을 선택해주세요", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val accidentOptions = listOf("있음" to true, "없음" to false)
                    accidentOptions.forEach { (text, value) ->
                        val isSelected = uiState.hasAccidentHistory == value
                        Button(
                            onClick = { sellCarViewModel.updateHasAccidentHistory(value) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) lightPurpleColor else Color.White,
                                contentColor = if (isSelected) Color.White else Color.Black
                            ),
                            border = if (!isSelected) BorderStroke(1.dp, Color.LightGray) else null
                        ) {
                            Text(text)
                        }
                    }
                }

                AnimatedVisibility(visible = uiState.hasAccidentHistory == true) {
                    Column {
                        Spacer(modifier = Modifier.height(32.dp))
                        Text("사고 정보에 대해 입력해주세요", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = accidentDetails,
                            onValueChange = { accidentDetails = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            placeholder = { Text("사고 정보에 대해 입력해주세요") },
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = purpleColor,
                                unfocusedBorderColor = if(accidentDetails.isNotEmpty()) purpleColor else Color.LightGray,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            enabled = uiState.hasAccidentHistory == true
                        )
                    }
                }
            } // 스크롤 Column 끝

            Spacer(modifier = Modifier.height(16.dp))

            // ▼▼▼ 이전/다음 버튼으로 수정 ▼▼▼
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
                    onClick = {
                        if (uiState.hasAccidentHistory == true) {
                            sellCarViewModel.updateAccidentDetails(accidentDetails)
                        } else {
                            sellCarViewModel.updateAccidentDetails("")
                        }
                        onNextClicked()
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = purpleColor,
                        disabledContainerColor = Color.LightGray
                    ),
                    enabled = uiState.hasAccidentHistory == false || (uiState.hasAccidentHistory == true && accidentDetails.isNotBlank()),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Text("다음", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            // ▲▲▲ 이전/다음 버튼으로 수정 ▲▲▲
        }
    }
}

@Preview(showBackground = true, device = "spec:width=360dp,height=800dp,dpi=480")
@Composable
fun SellCarAccidentHistoryScreenPreview() {
    MaterialTheme {
        val previewViewModel = remember { SellCarViewModel() }
        // previewViewModel.updateCurrentStep(6) // Preview에서는 ViewModel 값에 따라 결정되도록 주석 처리하거나 실제 값으로 설정
        previewViewModel.updateHasAccidentHistory(true)
        // previewViewModel.updateAccidentDetails("사고 상세 내용 미리보기")

        SellCarAccidentHistoryScreen(
            sellCarViewModel = previewViewModel,
            onSystemBack = {},    // onNavigateBack 대신 onSystemBack
            onStepBack = {},      // onStepBack 추가
            onNextClicked = {}
        )
    }
}

@Preview(showBackground = true, device = "spec:width=360dp,height=800dp,dpi=480")
@Composable
fun SellCarAccidentHistoryScreenNoAccidentPreview() {
    MaterialTheme {
        val previewViewModel = remember { SellCarViewModel() }
        // previewViewModel.updateCurrentStep(6)
        previewViewModel.updateHasAccidentHistory(false)

        SellCarAccidentHistoryScreen(
            sellCarViewModel = previewViewModel,
            onSystemBack = {},
            onStepBack = {},
            onNextClicked = {}
        )
    }
}

