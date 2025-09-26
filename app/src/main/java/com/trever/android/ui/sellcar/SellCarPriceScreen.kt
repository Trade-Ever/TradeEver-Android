package com.trever.android.ui.sellcar

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trever.android.ui.sellcar.util.NumberCommaTransformation
import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel
import com.trever.android.ui.theme.backgroundColor
import com.trever.android.ui.theme.cardBackgroundColor
import com.trever.android.ui.theme.textPrimaryColor
// import com.trever.android.ui.theme.backgroundColor // MaterialTheme.colorScheme 사용
// import com.trever.android.ui.theme.cardBackgroundColor // MaterialTheme.colorScheme 사용
// import com.trever.android.ui.theme.textPrimaryColor // MaterialTheme.colorScheme 사용
//import com.trever.android.ui.sellcar.viewmodel.SellCarViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.div
import kotlin.unaryMinus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellCarPriceScreen(
    sellCarViewModel: SellCarViewModel,
    onSystemBack: () -> Unit,    // ArrowBack 아이콘용
    onStepBack: () -> Unit,      // 하단 "이전" 버튼용
    onRegisterClicked: () -> Unit
) {
    val uiState by sellCarViewModel.uiState.collectAsState()
    var price by remember { mutableStateOf(uiState.price) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    val currentColorScheme = MaterialTheme.colorScheme

    val todayMillis = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val startDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = uiState.transactionStartDateMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= todayMillis
            }
        }
    )

    val endDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = uiState.transactionEndDateMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val startDate = startDatePickerState.selectedDateMillis
                return if (startDate != null) {
                    utcTimeMillis >= startDate
                } else {
                    utcTimeMillis >= todayMillis
                }
            }
        }
    )

    val purpleColor = Color(0xFF6A11CB) // TODO: 테마 색상으로 교체 고려
    val lightPurpleColor = Color(0xFF9F72FF) // TODO: 테마 색상으로 교체 고려
    val isLoading by sellCarViewModel.isLoading.collectAsState()

    LaunchedEffect(uiState.price) {
        if (price != uiState.price) {
            price = uiState.price
        }
    }

    Scaffold(
        containerColor = currentColorScheme.backgroundColor,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onSystemBack) { 
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로 가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = currentColorScheme.backgroundColor)
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

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Text("거래방식을 선택해주세요", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val transactionOptions = listOf("경매", "일반거래")
                    transactionOptions.forEach { option ->
                        val isSelected = uiState.transactionType == option
                        Button(
                            onClick = { sellCarViewModel.updateTransactionType(option) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) lightPurpleColor else currentColorScheme.cardBackgroundColor,
                                contentColor = if (isSelected) Color.White else currentColorScheme.onSurface // textPrimaryColor에서 변경
                            ),
                            border = BorderStroke(1.dp, if(isSelected) lightPurpleColor else Color.LightGray) // 요청 범위 밖
                        ) {
                            Text(option)
                        }
                    }
                }

                AnimatedVisibility(
                    visible = uiState.transactionType == "경매", 
                    enter = slideInVertically { it / 2 } + fadeIn(),
                    exit = slideOutVertically { -it / 2 } + fadeOut()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(32.dp))
                        Text("거래 시작-종료 날짜를 선택해주세요", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            DateBox(
                                dateMillis = startDatePickerState.selectedDateMillis,
                                onClick = { showStartDatePicker = true },
                                modifier = Modifier.weight(1f),
                                activeBorderColor = purpleColor // DateBox에 purpleColor 전달
                            )
                            Text("  ~  ", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            DateBox(
                                dateMillis = endDatePickerState.selectedDateMillis,
                                onClick = { showEndDatePicker = true },
                                modifier = Modifier.weight(1f),
                                activeBorderColor = purpleColor // DateBox에 purpleColor 전달
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = (uiState.transactionType == "경매" && endDatePickerState.selectedDateMillis != null) ||
                            uiState.transactionType == "일반거래",
                    enter = slideInVertically { it / 2 } + fadeIn(),
                    exit = slideOutVertically { -it / 2 } + fadeOut()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(32.dp))
                        Text("제시할 가격을 입력해주세요 (만원)", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it.filter { c -> c.isDigit() } },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Start),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            visualTransformation = NumberCommaTransformation(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = purpleColor,
                                unfocusedBorderColor = if(price.isNotEmpty()) purpleColor else Color.LightGray, // 요청 범위 밖
                                focusedContainerColor = currentColorScheme.cardBackgroundColor,
                                unfocusedContainerColor = currentColorScheme.cardBackgroundColor
                            )
                        )
                    }
                }
            } 

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onStepBack, 
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = currentColorScheme.backgroundColor,
                        contentColor = Color.Black // 요청 범위 밖
                    ),
                    border = BorderStroke(1.dp, Color.LightGray), // 요청 범위 밖
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Text(text = "이전", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        sellCarViewModel.updatePrice(price)
                        sellCarViewModel.updateTransactionDateRange(
                            startDatePickerState.selectedDateMillis,
                            endDatePickerState.selectedDateMillis
                        )
                        sellCarViewModel.registerCar(
                            onSuccess = { onRegisterClicked() },
                            onError = { /* errorMessage -> */ }
                        )
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = purpleColor,
                        disabledContainerColor = Color.LightGray // 요청 범위 밖
                    ),
                    enabled = when(uiState.transactionType) {
                        "경매" -> startDatePickerState.selectedDateMillis != null &&
                                endDatePickerState.selectedDateMillis != null &&
                                price.isNotBlank() && !isLoading
                        "일반거래" -> price.isNotBlank() && !isLoading
                        else -> false
                    },
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Text("등록하기", fontSize = 18.sp, color = MaterialTheme.colorScheme.textPrimaryColor, fontWeight = FontWeight.Bold) // 요청 범위 밖
                }
            }
        }

        if (showStartDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showStartDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        showStartDatePicker = false
                        sellCarViewModel.updateTransactionDateRange(startDatePickerState.selectedDateMillis, uiState.transactionEndDateMillis)
                    }) { Text("확인") }
                },
                dismissButton = {
                    TextButton(onClick = { showStartDatePicker = false }) { Text("취소") }
                }
            ) {
                DatePicker(state = startDatePickerState)
            }
        }

        if (showEndDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showEndDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        showEndDatePicker = false
                        sellCarViewModel.updateTransactionDateRange(uiState.transactionStartDateMillis, endDatePickerState.selectedDateMillis)
                    }) { Text("확인") }
                },
                dismissButton = {
                    TextButton(onClick = { showEndDatePicker = false }) { Text("취소") }
                }
            ) {
                DatePicker(state = endDatePickerState)
            }
        }
    }

}

@Composable
private fun DateBox(dateMillis: Long?, onClick: () -> Unit, modifier: Modifier = Modifier, activeBorderColor: Color) {
    val currentColorScheme = MaterialTheme.colorScheme
    val isComplete = dateMillis != null
    val (dateText, textColor) = if (isComplete) {
        formatDate(dateMillis!!, "yyyy/MM/dd") to currentColorScheme.onSurface // 텍스트 색상 변경
    } else {
        "YYYY/MM/DD" to currentColorScheme.onSurfaceVariant // 텍스트 색상 변경 (플레이스홀더)
    }

    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (isComplete) activeBorderColor else currentColorScheme.outline), // 테두리 색상 변경
        color = currentColorScheme.cardBackgroundColor // 배경색 변경
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween 
        ) {
            Text(dateText, color = textColor, fontSize = 16.sp)
            Icon(Icons.Default.DateRange, contentDescription = "Select Date", tint = currentColorScheme.onSurfaceVariant) // 아이콘 색상 변경
        }
    }
}

private fun formatDate(timestamp: Long, format: String): String {
    val sdf = SimpleDateFormat(format, Locale.getDefault())
    return sdf.format(Date(timestamp))
}


@Preview(showBackground = true, device = "spec:width=360dp,height=800dp,dpi=480")
@Composable
fun SellCarPriceScreenPreview() {
    MaterialTheme {
        val context = LocalContext.current
        val previewViewModel: SellCarViewModel = viewModel(
//            factory = SellCarViewModelFactory(context)
        )
        previewViewModel.updateTransactionType("경매")

        SellCarPriceScreen(
            sellCarViewModel = previewViewModel,
            onSystemBack = {},
            onStepBack = {},
            onRegisterClicked = {}
        )
    }
}

