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
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellCarMileageAndTypeScreen(
    sellCarViewModel: SellCarViewModel,
    onNavigateBack: () -> Unit,
    onNextClicked: () -> Unit
) {
    val uiState by sellCarViewModel.uiState.collectAsState()
    var mileage by remember { mutableStateOf(uiState.mileage) }
    val initialYear = if (uiState.selectedYear != Calendar.getInstance().get(Calendar.YEAR)) uiState.selectedYear.toString() else ""
    var year by remember { mutableStateOf(initialYear) }
    var showBottomSheet by remember { mutableStateOf(false) }

    val purpleColor = Color(0xFF6A11CB)

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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

                CustomProgressBar(totalSteps = 7, currentStep = 2)

                Spacer(modifier = Modifier.height(32.dp))

                DisplayInfoField(
                    label = "차량 모델을 입력해주세요",
                    value = uiState.selectedModel.ifEmpty { "(모델 정보 없음)" },
                    isComplete = uiState.selectedModel.isNotEmpty()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 연식 입력 필드
                Text("연식을 입력해주세요", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = year,
                    onValueChange = {
                        if (it.length <= 4) year = it.filter { c -> c.isDigit() }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("예: 2023") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = purpleColor,
                        unfocusedBorderColor = if (year.isNotEmpty()) purpleColor else Color.LightGray,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White
                    )
                )

                // 차종 선택 (애니메이션 적용)
                AnimatedVisibility(
                    visible = year.length == 4,
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

                // 주행거리 입력 (애니메이션 적용)
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
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 다음 버튼 (활성화/비활성화 상태만 관리)
            Button(
                onClick = {
                    sellCarViewModel.updateSelectedYear(year.toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR))
                    sellCarViewModel.updateMileage(mileage)
                    onNextClicked()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = purpleColor),
                enabled = year.length == 4 && uiState.selectedCarType.isNotBlank() && mileage.isNotBlank()
            ) {
                Text("다음", fontSize = 18.sp, color = Color.White)
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
                    border = BorderStroke(1.dp, Color(0xFF6A11CB))
                ) {
                    Text("취소", color = Color(0xFF6A11CB))
                }
                Button(
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) onConfirm(tempSelectedType)
                        }
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A11CB))
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
            color = Color.Black
        )
    }
}

@Preview(showBackground = true, device = "spec:shape=Normal,width=360,height=800,unit=dp,dpi=480")
@Composable
fun SellCarMileageAndTypeScreenPreview() {
    MaterialTheme {
        val previewViewModel = remember { SellCarViewModel() }
        previewViewModel.updateCurrentStep(2)
        previewViewModel.updateSelectedModel("현대 아반떼 SN7")
        SellCarMileageAndTypeScreen(
            sellCarViewModel = previewViewModel,
            onNavigateBack = {},
            onNextClicked = {}
        )
    }
}
