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
//import com.trever.android.ui.auction.AppFilledButton
import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel
//import com.trever.android.ui.components.AppFilledButton
//
//import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel // ViewModel import
//import com.trever.android.ui.sellcar.viewmodel.SellCarUiState // UiState import
import com.trever.android.ui.theme.backgroundColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellCarPlateNumberScreen(
    sellCarViewModel: SellCarViewModel,
    onSystemBack: () -> Unit, // 시스템 뒤로가기 (ArrowBack 아이콘용)
    onStepBack: () -> Unit,   // 단계별 이전 (하단 "이전" 버튼용)
    onNextClicked: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val uiState by sellCarViewModel.uiState.collectAsState()
    var plateNumber by remember { mutableStateOf(uiState.plateNumber) }

    LaunchedEffect(uiState.plateNumber) {
        if (plateNumber != uiState.plateNumber) {
            plateNumber = uiState.plateNumber
        }
    }

    Scaffold(
        containerColor = cs.backgroundColor,
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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(modifier = Modifier.size(10.dp).border(1.dp, Color.LightGray, CircleShape))

                    BasicTextField(
                        value = plateNumber,
                        onValueChange = { plateNumber = it },
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
                                if (plateNumber.isEmpty()) {
                                    Text(
                                        text = "12가 3456",
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 이전 버튼
                OutlinedButton(
                    onClick = onStepBack,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    border = BorderStroke(1.dp, Color.LightGray),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Text(text = "이전", fontWeight = FontWeight.Bold)
                }

                // 다음 버튼
                Button(
                    onClick = {
                        sellCarViewModel.updatePlateNumber(plateNumber)
                        onNextClicked()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A11CB)),
                    enabled = plateNumber.isNotBlank(),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Text(text = "다음", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CustomProgressBar(
    modifier: Modifier = Modifier,
    totalSteps: Int,
    currentStep: Int,
    activeColor: Color = Color(0xFF6A11CB), // 활성화된 스텝의 색상
    inactiveColor: Color = Color(0xFFD0D0D0), // 비활성화된 스텝의 색상
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
                    .weight(1f) // 각 박스가 동일한 너비를 가지도록 함
                    .height(8.dp)
                    .background(
                        color = if (i == currentStep) activeColor else inactiveColor,
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}