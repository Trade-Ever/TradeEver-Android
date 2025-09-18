package com.trever.android.ui.sellcar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellCarOptionsScreen(
    sellCarViewModel: SellCarViewModel,
    onNavigateBack: () -> Unit,
    onNextClicked: () -> Unit
) {
    val uiState by sellCarViewModel.uiState.collectAsState()
    var description by remember { mutableStateOf(uiState.description) }
    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("옵션 및 상세설명") },
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
            CustomProgressBar(totalSteps = 9, currentStep = uiState.currentStep) // 9단계로 가정

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // 차량 옵션 선택
                Text("차량 옵션을 선택해주세요", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp) // 최소 높이 지정
                        .clickable { showBottomSheet = true },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color.LightGray),
                    color = Color.White
                ) {
                    Text(
                        text = if (uiState.selectedOptions.isEmpty()) "옵션을 선택해주세요." else uiState.selectedOptions.joinToString(),
                        modifier = Modifier.padding(16.dp),
                        color = if (uiState.selectedOptions.isEmpty()) Color.Gray else Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 상세 설명 입력
                Text("상세 설명을 입력해주세요", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    placeholder = { Text("차량에 대해 상세하게 설명해주세요.") },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6200EE),
                        unfocusedBorderColor = Color.LightGray
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    sellCarViewModel.updateDescription(description)
                    onNextClicked()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
            ) {
                Text("다음", fontSize = 18.sp, color = Color.White)
            }
        }

        // 옵션 선택 바텀 시트
        if (showBottomSheet) {
            OptionsBottomSheet(
                allOptions = listOf("열선시트", "통풍시트", "썬루프", "열선핸들", "내비게이션", "전동시트", "어라운드뷰", "전동트렁크", "스마트키", "블랙박스"),
                selectedOptions = uiState.selectedOptions,
                onDismiss = { showBottomSheet = false },
                onConfirm = { selected ->
                    sellCarViewModel.updateSelectedOptions(selected)
                    showBottomSheet = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsBottomSheet(
    allOptions: List<String>,
    selectedOptions: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    val tempSelectedOptions = remember { mutableStateListOf<String>().also { it.addAll(selectedOptions) } }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .navigationBarsPadding()
        ) {
            Text("옵션을 선택해주세요", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 16.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(allOptions.chunked(2)) { rowItems ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowItems.forEach { option ->
                            FilterChip(
                                modifier = Modifier.weight(1f),
                                selected = tempSelectedOptions.contains(option),
                                onClick = {
                                    if (tempSelectedOptions.contains(option)) {
                                        tempSelectedOptions.remove(option)
                                    } else {
                                        tempSelectedOptions.add(option)
                                    }
                                },
                                label = { Text(option) },
                                leadingIcon = if (tempSelectedOptions.contains(option)) {
                                    { Icon(imageVector = Icons.Default.Check, contentDescription = "Selected") }
                                } else {
                                    null
                                }
                            )
                        }
                        if (rowItems.size < 2) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                onDismiss()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF6200EE))
                ) {
                    Text("취소", color = Color(0xFF6200EE))
                }
                Button(
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                onConfirm(tempSelectedOptions.toList())
                            }
                        }
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
                ) {
                    Text("확인")
                }
            }
        }
    }
}

// Preview는 생략합니다.
