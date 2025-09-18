package com.trever.android.ui.sellcar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.* 
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellCarDetailsScreen(
    sellCarViewModel: SellCarViewModel,
    onNavigateBack: () -> Unit,
    onNextClicked: () -> Unit
) {
    val uiState by sellCarViewModel.uiState.collectAsState()
    var displacement by remember { mutableStateOf(uiState.displacement) }
    var horsepower by remember { mutableStateOf(uiState.horsepower) }

    val purpleColor = Color(0xFF9F72FF)

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
            CustomProgressBar(totalSteps = 7, currentStep = 3)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // 연료 선택
                Text("연료를 선택해주세요", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                SelectableButtonGroup(
                    options = listOf("경유", "휘발유", "전기", "LPG"),
                    selectedOption = uiState.fuelType,
                    onOptionSelected = { sellCarViewModel.updateFuelType(it) }
                )

                // 변속기 선택 (애니메이션)
                AnimatedVisibility(
                    visible = uiState.fuelType.isNotEmpty(),
                    enter = slideInVertically { it / 2 } + fadeIn(),
                    exit = slideOutVertically { -it / 2 } + fadeOut()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("변속기를 선택해주세요", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        SelectableButtonGroup(
                            options = listOf("자동", "수동"),
                            selectedOption = uiState.transmissionType,
                            onOptionSelected = { sellCarViewModel.updateTransmissionType(it) }
                        )
                    }
                }

                // 배기량 입력 (애니메이션)
                AnimatedVisibility(
                    visible = uiState.transmissionType.isNotEmpty(),
                    enter = slideInVertically { it / 2 } + fadeIn(),
                    exit = slideOutVertically { -it / 2 } + fadeOut()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("배기량(CC)을 입력해주세요", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = displacement,
                            onValueChange = { displacement = it.filter { c -> c.isDigit() } },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("예: 1,600") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            visualTransformation = NumberCommaTransformation(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = purpleColor,
                                unfocusedBorderColor = if (displacement.isNotEmpty()) purpleColor else Color.LightGray,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                    }
                }

                // 마력 입력 (애니메이션)
                AnimatedVisibility(
                    visible = displacement.isNotEmpty(),
                    enter = slideInVertically { it / 2 } + fadeIn(),
                    exit = slideOutVertically { -it / 2 } + fadeOut()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("마력을 입력해주세요", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = horsepower,
                            onValueChange = { horsepower = it.filter { c -> c.isDigit() } },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("예: 123") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            visualTransformation = NumberCommaTransformation(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = purpleColor,
                                unfocusedBorderColor = if (horsepower.isNotEmpty()) purpleColor else Color.LightGray,
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
                    sellCarViewModel.updateDisplacement(displacement)
                    sellCarViewModel.updateHorsepower(horsepower)
                    onNextClicked()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = purpleColor,
                    disabledContainerColor = Color.LightGray
                ),
                enabled = uiState.fuelType.isNotEmpty() &&
                        uiState.transmissionType.isNotEmpty() &&
                        displacement.isNotEmpty() &&
                        horsepower.isNotEmpty()
            ) {
                Text("다음", fontSize = 18.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun SelectableButtonGroup(options: List<String>, selectedOption: String, onOptionSelected: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = selectedOption == option
            Button(
                onClick = { onOptionSelected(option) },
                modifier = Modifier.weight(1f), // 항상 weight(1f)를 적용하여 모든 버튼이 균등한 너비를 갖도록 함
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) Color(0xFF6A11CB) else Color.White,
                    contentColor = if (isSelected) Color.White else Color.Black
                ),
                border = if (!isSelected) BorderStroke(1.dp, Color.LightGray) else null,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(option)
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=360dp,height=800dp,dpi=480")
@Composable
fun SellCarDetailsScreenPreview() {
    MaterialTheme {
        val previewViewModel = SellCarViewModel()
        previewViewModel.updateCurrentStep(3)
        previewViewModel.updateFuelType("휘발유") // 깨짐 테스트를 위해 휘발유 선택
        previewViewModel.updateTransmissionType("자동")
        SellCarDetailsScreen(
            sellCarViewModel = previewViewModel,
            onNavigateBack = {},
            onNextClicked = {}
        )
    }
}