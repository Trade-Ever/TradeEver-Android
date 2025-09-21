package com.trever.android.ui.sellcar

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trever.android.ui.sellcar.util.NumberCommaTransformation
import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel
import com.trever.android.ui.theme.AppTheme
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellCarMileageAndTypeScreen(
    sellCarViewModel: SellCarViewModel,
    onSystemBack: () -> Unit,
    onStepBack: () -> Unit,
    onNextClicked: () -> Unit
) {
    val uiState by sellCarViewModel.uiState.collectAsState()
    var mileage by remember { mutableStateOf(uiState.mileage) }
    
    // 연식 초기값: ViewModel의 selectedYear가 초기값이 아니면 사용, 아니면 빈 문자열
    val initialYearString = if (uiState.selectedYear != Calendar.getInstance().get(Calendar.YEAR)) {
        uiState.selectedYear.toString()
    } else {
        "" // 사용자가 직접 입력해야 하는 경우 또는 선택 안한 경우
    }
    var yearInput by remember(uiState.selectedYear) { mutableStateOf(initialYearString) }
    
    var showBottomSheet by remember { mutableStateOf(false) }

    val purpleColor = Color(0xFF6A11CB)

    // 선택된 차량 정보 조합 (제조사 + 모델)
    val selectedCarModelDisplay = listOfNotNull(
        uiState.selectedManufacturer.takeIf { it.isNotBlank() },
        uiState.selectedModel.takeIf { it.isNotBlank() }
    ).joinToString(" ").ifEmpty { "(모델 정보 없음)" }

    val isCarModelInfoComplete = uiState.selectedManufacturer.isNotBlank() && uiState.selectedModel.isNotBlank()

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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CustomProgressBar(totalSteps = 7, currentStep = uiState.currentStep)
                Spacer(modifier = Modifier.height(32.dp))

                // 수정된 차량 모델 표시부
                DisplayInfoField(
                    label = "선택된 차량 모델",
                    value = selectedCarModelDisplay,
                    isComplete = isCarModelInfoComplete
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text("연식을 입력해주세요", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = yearInput, // ViewModel의 값으로 초기화된 로컬 상태 사용
                    onValueChange = {
                        if (it.length <= 4) yearInput = it.filter { c -> c.isDigit() }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("예: 2023") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = purpleColor,
                        unfocusedBorderColor = if (yearInput.isNotEmpty()) purpleColor else Color.LightGray,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White
                    )
                )

                AnimatedVisibility(
                    visible = yearInput.length == 4, // 로컬 입력 상태(yearInput) 기준
                    enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("차종을 선택해주세요", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable { showBottomSheet = true },
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, if (uiState.selectedCarType.isNotEmpty()) purpleColor else Color.LightGray)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = uiState.selectedCarType.ifEmpty { "차종 선택" },
                                    color = if (uiState.selectedCarType.isEmpty()) Color.Gray else Color.Black,
                                    fontSize = 16.sp
                                )
                                Icon(Icons.Filled.KeyboardArrowDown, "차종 선택", tint = Color.Gray)
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = uiState.selectedCarType.isNotEmpty(),
                    enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("주행거리를 입력해주세요 (단위: km)", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = mileage,
                            onValueChange = { mileage = it.filter { c -> c.isDigit() } },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("예: 11,234") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            visualTransformation = NumberCommaTransformation(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = purpleColor,
                                unfocusedBorderColor = if (mileage.isNotEmpty()) purpleColor else Color.LightGray,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                disabledContainerColor = Color.White
                            )
                        )
                    }
                }
            } // 스크롤 Column 끝

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
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    border = BorderStroke(1.dp, Color.LightGray)
                ) {
                    Text(text = "이전", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        // 입력된 연식(yearInput)으로 ViewModel 업데이트
                        sellCarViewModel.updateSelectedYear(yearInput.toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR))
                        sellCarViewModel.updateMileage(mileage)
                        onNextClicked()
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = purpleColor,
                        disabledContainerColor = Color.LightGray
                    ),
                    enabled = yearInput.length == 4 && uiState.selectedCarType.isNotBlank() && mileage.isNotBlank(),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Text("다음", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (showBottomSheet) {
            CarTypeBottomSheet(
                initialSelectedType = uiState.selectedCarType,
                onConfirm = {
                    sellCarViewModel.updateSelectedCarType(it)
                    showBottomSheet = false
                },
                onDismiss = { showBottomSheet = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarTypeBottomSheet(
    initialSelectedType: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val carTypes = listOf("대형", "준중형", "중형", "소형", "스포츠", "SUV", "승합차", "경차")
    var tempSelectedType by remember { mutableStateOf(initialSelectedType) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val purpleColor = Color(0xFF6A11CB)
    val selectedColor = Color(0xFF9F72FF)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("차종을 선택해주세요", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

            val rows = carTypes.chunked(4)
            rows.forEach { rowItems ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowItems.forEach { type ->
                        val isSelected = tempSelectedType == type
                        Button(
                            onClick = { tempSelectedType = type },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) selectedColor else Color.White,
                                contentColor = if (isSelected) Color.White else Color.Black
                            ),
                            border = if (!isSelected) BorderStroke(1.dp, Color.LightGray) else null
                        ) {
                            Text(type, fontSize = 14.sp)
                        }
                    }
                    if (rowItems.size < 4) {
                        for (i in 0 until (4 - rowItems.size)) {
                            Spacer(modifier = Modifier.weight(1f).height(48.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion { if (!sheetState.isVisible) onDismiss() }
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, purpleColor)
                ) {
                    Text("취소", color = purpleColor)
                }
                Button(
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) onConfirm(tempSelectedType)
                        }
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = purpleColor)
                ) {
                    Text("확인", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun DisplayInfoField(label: String, value: String, isComplete: Boolean) {
    Text(label, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(8.dp))
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        border = BorderStroke(1.dp, if (isComplete) Color(0xFF6A11CB) else Color.LightGray)
    ) {
        Text(
            text = value,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            fontSize = 16.sp,
            color = if (value == "(모델 정보 없음)" && !isComplete) Color.Gray else Color.Black // 모델 정보 없을 때 회색 처리 추가
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SellCarMileageAndTypeScreenPreview() {
    AppTheme {
        val previewViewModel = remember { SellCarViewModel() }
        previewViewModel.updateCurrentStep(3) // 이 화면은 3단계 또는 그 이후
        previewViewModel.updateSelectedManufacturer("현대")
        previewViewModel.updateSelectedModel("아반떼 SN7")
        previewViewModel.updateSelectedYear(2023)
        // previewViewModel.updateSelectedCarType("준중형")
        // previewViewModel.updateMileage("15000")

        SellCarMileageAndTypeScreen(
            sellCarViewModel = previewViewModel,
            onSystemBack = {},
            onStepBack = {},
            onNextClicked = {}
        )
    }
}
