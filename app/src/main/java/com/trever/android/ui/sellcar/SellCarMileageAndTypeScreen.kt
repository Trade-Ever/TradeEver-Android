package com.trever.android.ui.sellcar

import android.util.Log 
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel
import kotlinx.coroutines.launch

// import com.trever.android.ui.theme.YourAppTheme // 실제 테마로 교체 필요

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellCarMileageAndTypeScreen(
    sellCarViewModel: SellCarViewModel,
    onNavigateBack: () -> Unit,
    onNextClicked: () -> Unit
) {
    val uiState by sellCarViewModel.uiState.collectAsState()
    var mileage by remember { mutableStateOf(uiState.mileage) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val carTypes = listOf("대형", "준중형", "중형", "소형", "스포츠", "SUV", "승합차", "경차")

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("정보 입력") }, 
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로 가기"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
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

            Spacer(modifier = Modifier.height(32.dp))

            DisplayInfoField(label = "차량 모델을 입력해주세요", value = uiState.selectedModel.ifEmpty { "(모델 정보 없음)" })

            Spacer(modifier = Modifier.height(16.dp))

            DisplayInfoField(label = "연식을 입력해주세요", value = uiState.selectedYear.toString())

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "차종을 선택해주세요",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 차종 선택 (OutlinedTextField 대신 Surface 사용)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        Log.d("BottomSheetDebug", "Car type Surface clicked, showBottomSheet will be set to true")
                        showBottomSheet = true
                    },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (showBottomSheet) Color(0xFF6A11CB) else Color.LightGray),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp), 
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

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "주행거리를 입력해주세요 (단위: km)",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = mileage,
                onValueChange = { newValue ->
                    mileage = newValue.filter { it.isDigit() } 
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("예: 11,234") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6A11CB),
                    unfocusedBorderColor = Color.LightGray
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    sellCarViewModel.updateMileage(mileage)
                    onNextClicked()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A11CB))
            ) {
                Text("다음", fontSize = 18.sp, color = Color.White)
            }
        }

        if (showBottomSheet) {
            Log.d("BottomSheetDebug", "showBottomSheet is true, ModalBottomSheet should be displayed")
            ModalBottomSheet(
                onDismissRequest = { 
                    Log.d("BottomSheetDebug", "onDismissRequest called, showBottomSheet will be set to false")
                    showBottomSheet = false 
                },
                sheetState = sheetState,
                containerColor = Color.White,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .navigationBarsPadding(), 
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "차종을 선택해주세요",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    val rows = carTypes.chunked(4)
                    rows.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                        ) {
                            rowItems.forEach { type ->
                                Button(
                                    onClick = {
                                        Log.d("BottomSheetDebug", "Car type '$type' button clicked in BottomSheet")
                                        sellCarViewModel.updateSelectedCarType(type)
                                    },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (uiState.selectedCarType == type) Color(0xFF6A11CB) else Color.LightGray,
                                        contentColor = if (uiState.selectedCarType == type) Color.White else Color.Black
                                    )
                                ) {
                                    Text(type, fontSize = 12.sp)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                Log.d("BottomSheetDebug", "Cancel button clicked in BottomSheet")
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) showBottomSheet = false
                                }
                            },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF6A11CB))
                        ) {
                            Text("취소", color = Color(0xFF6A11CB))
                        }
                        Button(
                            onClick = {
                                Log.d("BottomSheetDebug", "Confirm button clicked in BottomSheet")
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) showBottomSheet = false
                                }
                            },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A11CB))
                        ) {
                            Text("확인")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun DisplayInfoField(label: String, value: String) {
    Text(
        text = label,
        fontSize = 14.sp,
        color = Color.Gray,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFF6A11CB)),
        color = Color.White
    ) {
        Text(
            text = value,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

@Preview(showBackground = true, device = "spec:shape=Normal,width=360,height=640,unit=dp,dpi=480")
@Composable
fun SellCarMileageAndTypeScreenPreview() {
    YourAppTheme { 
        val previewViewModel = SellCarViewModel()
        previewViewModel.updateCurrentStep(6)
        previewViewModel.updateSelectedModel("현대 아반떼 SN7")
        previewViewModel.updateSelectedYear(2023)
        previewViewModel.updateMileage("11234")

        SellCarMileageAndTypeScreen(
            sellCarViewModel = previewViewModel,
            onNavigateBack = {},
            onNextClicked = {}
        )
    }
}
