package com.trever.android.ui.sellcar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trever.android.ui.auction.AppFilledButton
import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel // ViewModel import
import com.trever.android.ui.theme.backgroundColor

// !! 중요 !!
// 실제 앱의 테마를 import 해야 합니다. 아래는 예시이며, 실제 경로로 수정해주세요.
// import com.trever.android.ui.theme.YourAppTheme // 또는 TreverAppTheme 등

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellCarPlateNumberScreen(
    sellCarViewModel: SellCarViewModel, // ViewModel 주입
    onNavigateBack: () -> Unit,
    onNextClicked: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val uiState by sellCarViewModel.uiState.collectAsState()
    var carNumber by remember { mutableStateOf(uiState.carNumber) } // 초기값은 ViewModel에서 가져옴

    // carNumber 상태가 ViewModel의 uiState 변경에 따라 업데이트 되도록 observe
    LaunchedEffect(uiState.carNumber) {
        if (carNumber != uiState.carNumber) {
            carNumber = uiState.carNumber
        }
    }

    Scaffold(
        containerColor = cs.backgroundColor, // Scaffold 전체 배경을 흰색으로 설정
        topBar = {
            TopAppBar(
                title = { }, // 제목 추가
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로 가기"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = cs.backgroundColor, // TopAppBar 배경도 흰색으로 설정
                    titleContentColor = cs.onSurface, // 제목 색상
                    navigationIconContentColor = Color.Black // 아이콘 색상
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 30.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CustomProgressBar(totalSteps = 7, currentStep = 1) // 전체 단계를 5로 수정

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "등록할 차량 번호를 입력해주세요",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 차량 번호판 스타일 입력 필드
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.Black),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(modifier = Modifier.size(10.dp).border(1.dp, Color.LightGray, CircleShape))

                    BasicTextField(
                        value = carNumber,
                        onValueChange = { carNumber = it },
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (carNumber.isEmpty()) {
                                    Text(
                                        text = "23가 4821",
                                        style = TextStyle(
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = Color.LightGray,
                                            textAlign = TextAlign.Center
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    Box(modifier = Modifier.size(10.dp).border(1.dp, Color.LightGray, CircleShape))
                }
            }


            Spacer(modifier = Modifier.weight(1f))

            AppFilledButton(
                text = "다음",
                onClick = {
                    sellCarViewModel.updateCarNumber(carNumber)
                    onNextClicked()
                },
                modifier = Modifier
                    .fillMaxWidth(),
                height = 56.dp,
                enabled = carNumber.isNotBlank()
            )
        }
    }
}

@Composable
fun CustomProgressBar(
    modifier: Modifier = Modifier,
    totalSteps: Int,
    currentStep: Int,
    activeColor: Color = Color(0xFF6A11CB),
    inactiveColor: Color = Color(0xFFD0D0D0),
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..totalSteps) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .background(
                        color = if (i == currentStep) activeColor else inactiveColor,
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

// 임시 YourAppTheme 정의 (실제 테마로 교체 필요)
@Composable
fun YourAppTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme(),
        content = content
    )
}

@Preview(showBackground = true, device = "spec:shape=Normal,width=360,height=640,unit=dp,dpi=480")
@Composable
fun SellCarPlateNumberScreenPreview() {
    YourAppTheme {
        val sellCarViewModel = remember { SellCarViewModel() }
        SellCarPlateNumberScreen(
            sellCarViewModel = sellCarViewModel,
            onNavigateBack = {},
            onNextClicked = {}
        )
    }
}
