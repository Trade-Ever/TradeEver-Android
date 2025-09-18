package com.trever.android.ui.sellcar

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
import com.trever.android.ui.sellcar.util.NumberCommaTransformation
import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellCarPriceScreen(
    sellCarViewModel: SellCarViewModel,
    onNavigateBack: () -> Unit,
    onRegisterClicked: () -> Unit
) {
    val uiState by sellCarViewModel.uiState.collectAsState()
    var price by remember { mutableStateOf(uiState.price) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

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

    val purpleColor = Color(0xFF6A11CB)
    val lightPurpleColor = Color(0xFF9F72FF)

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("입력 - 제시 가격 및 거래 방식 입력") },
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
            CustomProgressBar(totalSteps = 7, currentStep = 7)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // 거래방식 선택
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
                                containerColor = if (isSelected) lightPurpleColor else Color.White,
                                contentColor = if (isSelected) Color.White else Color.Black
                            ),
                            border = BorderStroke(1.dp, if(isSelected) lightPurpleColor else Color.LightGray)
                        ) {
                            Text(option)
                        }
                    }
                }

                // 거래 날짜 선택 (애니메이션)
                AnimatedVisibility(
                    visible = uiState.transactionType.isNotEmpty(),
                    enter = slideInVertically { it / 2 } + fadeIn(),
                    exit = slideOutVertically { -it / 2 } + fadeOut()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(32.dp))
                        Text("거래 날짜를 선택해주세요", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            DateBox(
                                dateMillis = startDatePickerState.selectedDateMillis, 
                                onClick = { showStartDatePicker = true },
                                modifier = Modifier.weight(1f)
                            )
                            Text("  ~  ", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            DateBox(
                                dateMillis = endDatePickerState.selectedDateMillis, 
                                onClick = { showEndDatePicker = true },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 가격 입력 (애니메이션)
                AnimatedVisibility(
                    visible = endDatePickerState.selectedDateMillis != null,
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
                                unfocusedBorderColor = if(price.isNotEmpty()) purpleColor else Color.LightGray,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    sellCarViewModel.updatePrice(price)
                    sellCarViewModel.updateTransactionDateRange(
                        startDatePickerState.selectedDateMillis,
                        endDatePickerState.selectedDateMillis
                    )
                    onRegisterClicked()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = purpleColor, disabledContainerColor = Color.LightGray),
                enabled = uiState.transactionType.isNotEmpty() &&
                        startDatePickerState.selectedDateMillis != null &&
                        endDatePickerState.selectedDateMillis != null &&
                        price.isNotBlank()
            ) {
                Text("등록하기", fontSize = 18.sp, color = Color.White)
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
private fun DateBox(dateMillis: Long?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val purpleColor = Color(0xFF6A11CB)
    val isComplete = dateMillis != null
    val (dateText, textColor) = if (isComplete) {
        formatDate(dateMillis!!, "yyyy/MM/dd") to Color.Black
    } else {
        "YYYY/MM/DD" to Color.Gray
    }

    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (isComplete) purpleColor else Color.LightGray),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(dateText, color = textColor, fontSize = 16.sp)
            Icon(Icons.Default.DateRange, contentDescription = "Select Date", tint = Color.Gray)
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
        val previewViewModel = SellCarViewModel()
        previewViewModel.updateTransactionType("경매")
        SellCarPriceScreen(
            sellCarViewModel = previewViewModel,
            onNavigateBack = {},
            onRegisterClicked = {}
        )
    }
}
