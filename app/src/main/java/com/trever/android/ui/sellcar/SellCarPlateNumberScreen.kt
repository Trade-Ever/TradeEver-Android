package com.trever.android.ui.sellcar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.trever.android.ui.sellcar.viewmodel.SellCarUiState // UiState import
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
                title = { Text("입력 - 차량 번호") }, // 제목 추가
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
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CustomProgressBar(totalSteps = 6, currentStep = uiState.currentStep)

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "등록할 차량 번호를 입력해주세요",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = carNumber,
                onValueChange = {
                    carNumber = it
                    // 입력 변경 시 즉시 ViewModel 업데이트 혹은 다음 버튼에서 한번에 업데이트 선택 가능
                    // sellCarViewModel.updateCarNumber(it)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "23가 4821",
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                textStyle = TextStyle(
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6A11CB), // 보라색 계열
                    unfocusedBorderColor = Color.LightGray,
                )
            )

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
                enabled = carNumber.isNotBlank()   // 입력 검증 쓰면 좋음(선택)
            )

//            Button(
//                onClick = {
//                    sellCarViewModel.updateCarNumber(carNumber) // 다음 버튼 클릭 시 최종값 ViewModel에 저장
//                    onNextClicked()
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(56.dp),
//                shape = RoundedCornerShape(8.dp),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(0xFF6A11CB) // 보라색 계열
//                )
//            ) {
//                Text("다음", fontSize = 18.sp, color = Color.White)
//            }
        }
    }
}

@Composable
fun CustomProgressBar(
    modifier: Modifier = Modifier,
    totalSteps: Int,
    currentStep: Int,
    activeColor: Color = Color(0xFF6A11CB), // 보라색 계열
    inactiveColor: Color = Color(0xFFD0D0D0), // 회색 계열
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
                        color = if (i == currentStep) activeColor else inactiveColor, // 수정된 부분
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

// 임시 YourAppTheme 정의 (실제 테마로 교체 필요)
// 만약 ui/theme/Theme.kt 와 같은 파일에 실제 테마가 있다면 이 부분을 삭제하고 해당 테마를 import 하세요.
@Composable
fun YourAppTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme(),
        typography = Typography(),
        shapes = Shapes(),
        content = content
    )
}

@Preview(showBackground = true, device = "spec:shape=Normal,width=360,height=640,unit=dp,dpi=480")
@Composable
fun SellCarPlateNumberScreenPreview() {
    // !! 중요 !!
    // 아래 YourAppTheme를 실제 앱의 테마 Composable로 교체하세요.
    // (예: com.trever.android.ui.theme.TreverAppTheme 또는 프로젝트에 정의된 다른 테마)
    // 해당 테마 파일에서 Composable 함수를 import하여 사용해야 합니다.
    YourAppTheme {
        val sellCarViewModel = remember { SellCarViewModel() }
        // Preview에서 특정 단계를 보려면 ViewModel의 currentStep을 직접 설정
        // 예: sellCarViewModel.updateCurrentStep(1)
        SellCarPlateNumberScreen(
            sellCarViewModel = sellCarViewModel,
            onNavigateBack = {},
            onNextClicked = {}
        )
    }
}
