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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// import com.google.common.math.LinearTransformation.horizontal // 사용 안함 제거
import com.trever.android.ui.sellcar.util.NumberCommaTransformation
import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel
import com.trever.android.ui.theme.AppTheme // Preview에서 사용될 수 있음
import com.trever.android.ui.theme.backgroundColor
import com.trever.android.ui.theme.cardBackgroundColor
// import com.trever.android.ui.theme.backgroundColor // MaterialTheme.colorScheme 사용
// import com.trever.android.ui.theme.cardBackgroundColor // MaterialTheme.colorScheme 사용
// import com.trever.android.ui.theme.textPrimaryColor // MaterialTheme.colorScheme 사용
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

    val initialYearString = if (uiState.selectedYear != Calendar.getInstance().get(Calendar.YEAR)) {
        uiState.selectedYear.toString()
    } else {
        ""
    }
    var yearInput by remember(uiState.selectedYear) { mutableStateOf(initialYearString) }
    
    var showBottomSheet by remember { mutableStateOf(false) }

    val purpleColor = Color(0xFF6A11CB) // TODO: 테마 색상으로 교체 고려
    val currentColorScheme = MaterialTheme.colorScheme

    val selectedCarModelDisplay = listOf(
        uiState.selectedManufacturer,
        uiState.selectedModelName
    ).filter { it.isNotBlank() }.joinToString(" ")
     .ifBlank { "(모델 정보 없음)" }

    val isCarModelInfoComplete = uiState.selectedManufacturer.isNotBlank() && uiState.selectedModel.isNotBlank()

    Scaffold(
        containerColor = currentColorScheme.backgroundColor,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onSystemBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로 가기"
                            // 기본 tint가 onSurface이므로 명시적 지정 불필요할 수 있음
                        )
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

                // "선택된 차량 모델" 텍스트 박스
                DisplayInfoField(
                    label = "선택된 차량 모델",
                    value = selectedCarModelDisplay,
                    isComplete = isCarModelInfoComplete,
                    activeBorderColor = purpleColor // 활성 시 테두리 색상 전달
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text("연식을 입력해주세요", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = yearInput,
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
                        unfocusedBorderColor = if (yearInput.isNotEmpty()) purpleColor else currentColorScheme.outline,
                        focusedContainerColor = currentColorScheme.cardBackgroundColor,
                        unfocusedContainerColor = currentColorScheme.cardBackgroundColor,
                        disabledContainerColor = currentColorScheme.cardBackgroundColor
                    )
                )

                AnimatedVisibility(
                    visible = yearInput.length == 4,
                    enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("차종을 선택해주세요", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        // "차종을 선택해주세요" 텍스트 박스
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable { showBottomSheet = true },
                            shape = RoundedCornerShape(8.dp),
                            color = currentColorScheme.cardBackgroundColor, // 배경색 변경
                            border = BorderStroke(1.dp, if (uiState.selectedCarType.isNotEmpty()) purpleColor else currentColorScheme.outline) // 테두리색 변경
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = uiState.selectedCarType.ifEmpty { "차종 선택" },
                                    color = if (uiState.selectedCarType.isEmpty()) currentColorScheme.onSurfaceVariant else currentColorScheme.onSurface, // 텍스트색 변경
                                    fontSize = 16.sp
                                )
                                Icon(Icons.Filled.KeyboardArrowDown, "차종 선택", tint = currentColorScheme.onSurfaceVariant) // 아이콘색 변경
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = uiState.selectedCarType.isNotEmpty(),
                    // ... (이하 동일)
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
                                unfocusedBorderColor = if (mileage.isNotEmpty()) purpleColor else currentColorScheme.outline,
                                focusedContainerColor = currentColorScheme.cardBackgroundColor,
                                unfocusedContainerColor = currentColorScheme.cardBackgroundColor,
                                disabledContainerColor = currentColorScheme.cardBackgroundColor
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
                        containerColor = currentColorScheme.surface, // 이전 backgroundColor에서 surface로 변경
                        contentColor = currentColorScheme.onSurface // 이전 Black에서 onSurface로 변경
                    ),
                    border = BorderStroke(1.dp, currentColorScheme.outline) // 이전 LightGray에서 outline으로 변경
                ) {
                    Text(text = "이전", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        sellCarViewModel.updateSelectedYear(yearInput.toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR))
                        sellCarViewModel.updateMileage(mileage)
                        onNextClicked()
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = purpleColor,
                        contentColor = Color.White, // textPrimaryColor에서 White로 변경 (purpleColor 대비)
                        disabledContainerColor = currentColorScheme.onSurface.copy(alpha = 0.12f), // LightGray에서 변경
                        disabledContentColor = currentColorScheme.onSurface.copy(alpha = 0.38f) // 추가
                    ),
                    enabled = yearInput.length == 4 && uiState.selectedCarType.isNotBlank() && mileage.isNotBlank(),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Text("다음", fontSize = 18.sp, fontWeight = FontWeight.Bold) // 색상은 ButtonDefaults에서 자동 처리
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
    val purpleColor = Color(0xFF6A11CB) // TODO: 테마 색상으로 교체 고려
    val selectedColor = Color(0xFF9F72FF) // TODO: 테마 색상으로 교체 고려
    val currentColorScheme = MaterialTheme.colorScheme

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = currentColorScheme.surface, // backgroundColor에서 surface로 변경
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("차종을 선택해주세요", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp), color = currentColorScheme.onSurface)

            val rows = carTypes.chunked(4)
            rows.forEach { rowItems ->
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { type ->
                        val isSelected = tempSelectedType == type
                        Button(
                            onClick = { tempSelectedType = type },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) selectedColor else currentColorScheme.surfaceVariant, // cardBackgroundColor에서 surfaceVariant로 변경
                                contentColor = if (isSelected) Color.White else currentColorScheme.onSurfaceVariant // textPrimaryColor에서 변경
                            ),
                            border = if (!isSelected) BorderStroke(1.dp, currentColorScheme.outline) else null, // LightGray에서 변경
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text(
                                text = type,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Visible
                            )
                        }
                    }
                    if (rowItems.size < 4) {
                        for (i in 0 until (4 - rowItems.size)) {
                            Spacer(modifier = Modifier.weight(1f))
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
                    border = BorderStroke(1.dp, purpleColor),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = purpleColor) // Text 색상 명시
                ) {
                    Text("취소")
                }
                Button(
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) onConfirm(tempSelectedType)
                        }
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = purpleColor,
                        contentColor = Color.White // textPrimaryColor에서 White로 변경
                        )
                ) {
                    Text("확인")
                }
            }
        }
    }
}

@Composable
fun DisplayInfoField(label: String, value: String, isComplete: Boolean, activeBorderColor: Color) {
    val currentColorScheme = MaterialTheme.colorScheme
    Text(label, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.fillMaxWidth(), color = currentColorScheme.onBackground)
    Spacer(modifier = Modifier.height(8.dp))
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = currentColorScheme.cardBackgroundColor, // textPrimaryColor에서 변경
        border = BorderStroke(1.dp, if (isComplete) activeBorderColor else currentColorScheme.outline) // LightGray에서 변경 및 activeBorderColor 사용
    ) {
        Text(
            text = value,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            fontSize = 16.sp,
            color = if (value == "(모델 정보 없음)" && !isComplete) currentColorScheme.onSurfaceVariant else currentColorScheme.onSurface // Gray, Black에서 변경
        )
    }
}
