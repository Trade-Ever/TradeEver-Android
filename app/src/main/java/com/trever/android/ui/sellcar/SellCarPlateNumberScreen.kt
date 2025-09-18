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
import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel
import com.trever.android.ui.theme.backgroundColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellCarPlateNumberScreen(
    sellCarViewModel: SellCarViewModel,
    onNavigateBack: () -> Unit,
    onNextClicked: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val uiState by sellCarViewModel.uiState.collectAsState()
    var plateNumber by remember { mutableStateOf(uiState.plateNumber) } // uiState.plateNumber로 수정

    LaunchedEffect(uiState.plateNumber) {
        if (plateNumber != uiState.plateNumber) {
            plateNumber = uiState.plateNumber
        }
    }

    Scaffold(
        containerColor = cs.backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("차량 번호 입력") }, // 제목 추가
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로 가기"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = cs.backgroundColor,
                    titleContentColor = cs.onSurface,
                    navigationIconContentColor = Color.Black
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
            CustomProgressBar(totalSteps = 7, currentStep = 1)

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "등록할 차량 번호를 입력해주세요",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(24.dp))

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
                        value = plateNumber, // plateNumber로 수정
                        onValueChange = { plateNumber = it }, // plateNumber로 수정
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
                                if (plateNumber.isEmpty()) { // plateNumber로 수정
                                    Text(
                                        text = "12가 3456", // Placeholder 수정
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
                    sellCarViewModel.updatePlateNumber(plateNumber) // updatePlateNumber로 수정
                    onNextClicked()
                },
                modifier = Modifier.fillMaxWidth(),
                height = 56.dp,
                enabled = plateNumber.isNotBlank() // plateNumber로 수정
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

@Preview(showBackground = true, device = "spec:shape=Normal,width=360,height=640,unit=dp,dpi=480")
@Composable
fun SellCarPlateNumberScreenPreview() {
    MaterialTheme {
        val sellCarViewModel = remember { SellCarViewModel() }
        SellCarPlateNumberScreen(
            sellCarViewModel = sellCarViewModel,
            onNavigateBack = {},
            onNextClicked = {}
        )
    }
}