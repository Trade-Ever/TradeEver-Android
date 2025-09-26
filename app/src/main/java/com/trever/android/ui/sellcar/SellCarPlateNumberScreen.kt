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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel
import com.trever.android.ui.theme.backgroundColor
import com.trever.android.ui.theme.cardBackgroundColor
import com.trever.android.ui.theme.textPrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellCarPlateNumberScreen(
    sellCarViewModel: SellCarViewModel,
    onSystemBack: () -> Unit,
    onStepBack: () -> Unit,
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

    if (uiState.plateNumberExists == true) {
        Dialog(onDismissRequest = { sellCarViewModel.resetPlateNumberCheck() }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.cardBackgroundColor
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "차량 번호 중복",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.textPrimaryColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "이미 등록된 차량 번호입니다.다른 번호를 입력해주세요.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.textPrimaryColor,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { sellCarViewModel.resetPlateNumberCheck() },
                        modifier = Modifier,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 40.dp)
                    ) {
                        Text(
                            text = "확인",
                            color = MaterialTheme.colorScheme.textPrimaryColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
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
                border = BorderStroke(1.dp, cs.outline),
                color = cs.backgroundColor 
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .border(1.dp, cs.outlineVariant, CircleShape)
                    )

                    BasicTextField(
                        value = plateNumber,
                        onValueChange = { plateNumber = it },
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = cs.onSurface,
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
                                            color = cs.onSurface.copy(alpha = 0.6f),
                                            textAlign = TextAlign.Center
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .border(1.dp, cs.outlineVariant, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onStepBack,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.cardBackgroundColor,
                        contentColor = Color.Black
                    ),
                    border = BorderStroke(1.dp, Color.LightGray),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Text(text = "이전", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        sellCarViewModel.updatePlateNumber(plateNumber)
                        sellCarViewModel.checkPlateNumberDuplication { isDuplicate ->
                            if (!isDuplicate) {
                                onNextClicked()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A11CB)),
                    enabled = plateNumber.isNotBlank() && !uiState.isPlateNumberChecking,
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    if (uiState.isPlateNumberChecking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.textPrimaryColor,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(text = "다음", fontWeight = FontWeight.Bold)
                    }
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
