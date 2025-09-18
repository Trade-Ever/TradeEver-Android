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
    onNavigateBack: () -> Unit,
    onNextClicked: () -> Unit
) {
    val uiState by sellCarViewModel.uiState.collectAsState()
    var accidentDetails by remember { mutableStateOf(uiState.accidentDetails) }

    // 사고 이력 '없음'을 선택하면, 상세 내용을 초기화
    LaunchedEffect(uiState.hasAccidentHistory) {
        if (uiState.hasAccidentHistory == false) {
            accidentDetails = ""
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("사고 이력 입력") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
            CustomProgressBar(totalSteps = 10, currentStep = uiState.currentStep) // 10단계로 가정

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // 사고이력 선택
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
                                containerColor = if (isSelected) Color(0xFF9F72FF) else Color.White,
                                contentColor = if (isSelected) Color.White else Color.Black
                            ),
                            border = if (!isSelected) BorderStroke(1.dp, Color.LightGray) else null
                        ) {
                            Text(text)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 사고 정보 입력 (애니메이션과 함께 표시/숨김)
                AnimatedVisibility(visible = uiState.hasAccidentHistory == true) {
                    Column {
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
                                focusedBorderColor = Color(0xFF6200EE),
                                unfocusedBorderColor = Color.LightGray
                            ),
                            enabled = uiState.hasAccidentHistory == true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (uiState.hasAccidentHistory == true) {
                        sellCarViewModel.updateAccidentDetails(accidentDetails)
                    } else {
                        sellCarViewModel.updateAccidentDetails("") // 사고 '없음'이면 상세내용 비움
                    }
                    onNextClicked()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)),
                // 사고이력 선택을 해야 다음 버튼 활성화
                enabled = uiState.hasAccidentHistory != null
            ) {
                Text("다음", fontSize = 18.sp, color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=360dp,height=800dp,dpi=480")
@Composable
fun SellCarAccidentHistoryScreenPreview() {
    MaterialTheme {
        val previewViewModel = SellCarViewModel()
        previewViewModel.updateCurrentStep(10)
        previewViewModel.updateHasAccidentHistory(true) // '있음' 선택된 상태 미리보기
        SellCarAccidentHistoryScreen(
            sellCarViewModel = previewViewModel,
            onNavigateBack = {},
            onNextClicked = {}
        )
    }
}

@Preview(showBackground = true, device = "spec:width=360dp,height=800dp,dpi=480")
@Composable
fun SellCarAccidentHistoryScreenNoAccidentPreview() {
    MaterialTheme {
        val previewViewModel = SellCarViewModel()
        previewViewModel.updateCurrentStep(10)
        previewViewModel.updateHasAccidentHistory(false) // '없음' 선택된 상태 미리보기
        SellCarAccidentHistoryScreen(
            sellCarViewModel = previewViewModel,
            onNavigateBack = {},
            onNextClicked = {}
        )
    }
}
