package com.trever.android.ui.sellcar

import androidx.compose.animation.*
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trever.android.ui.sellcar.util.NumberCommaTransformation
import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel
import com.trever.android.ui.theme.backgroundColor
import com.trever.android.ui.theme.cardBackgroundColor
import com.trever.android.ui.theme.textPrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellCarDetailsScreen(
    sellCarViewModel: SellCarViewModel,
    onSystemBack: () -> Unit,    
    onStepBack: () -> Unit,      
    onNextClicked: () -> Unit
) {
    val uiState by sellCarViewModel.uiState.collectAsState()
    var displacement by remember { mutableStateOf(uiState.displacement) }
    var horsepower by remember { mutableStateOf(uiState.horsepower) }

    val purpleColor = Color(0xFF6A11CB)

    LaunchedEffect(uiState.displacement) {
        if (displacement != uiState.displacement) {
            displacement = uiState.displacement
        }
    }
    LaunchedEffect(uiState.horsepower) {
        if (horsepower != uiState.horsepower) {
            horsepower = uiState.horsepower
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.backgroundColor,
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
                    containerColor = MaterialTheme.colorScheme.backgroundColor,
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
            CustomProgressBar(totalSteps = 7, currentStep = uiState.currentStep) 

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Text("연료를 선택해주세요", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                SelectableButtonGroup(
                    options = listOf("경유", "휘발유", "전기", "LPG"),
                    selectedOption = uiState.fuelType,
                    onOptionSelected = { sellCarViewModel.updateFuelType(it) }
                )

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
                                focusedContainerColor = MaterialTheme.colorScheme.cardBackgroundColor,
                                unfocusedContainerColor = MaterialTheme.colorScheme.cardBackgroundColor
                            )
                        )
                    }
                }

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
                                focusedContainerColor = MaterialTheme.colorScheme.cardBackgroundColor,
                                unfocusedContainerColor = MaterialTheme.colorScheme.cardBackgroundColor
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
                        containerColor = MaterialTheme.colorScheme.backgroundColor,
                        contentColor = Color.Black
                    ),
                    border = BorderStroke(1.dp, Color.LightGray),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Text(text = "이전", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        sellCarViewModel.updateDisplacement(displacement)
                        sellCarViewModel.updateHorsepower(horsepower)
                        onNextClicked()
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = purpleColor,
                        disabledContainerColor = Color.LightGray
                    ),
                    enabled = uiState.fuelType.isNotEmpty() &&
                            uiState.transmissionType.isNotEmpty() &&
                            displacement.isNotEmpty() &&
                            horsepower.isNotEmpty(),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Text("다음", fontSize = 18.sp, color = MaterialTheme.colorScheme.textPrimaryColor, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SelectableButtonGroup(options: List<String>, selectedOption: String, onOptionSelected: (String) -> Unit) {
    val lightPurpleColor = Color(0xFF9F72FF)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = selectedOption == option
            Button(
                onClick = { onOptionSelected(option) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) lightPurpleColor else MaterialTheme.colorScheme.cardBackgroundColor,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.textPrimaryColor else MaterialTheme.colorScheme.textPrimaryColor
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
        val context = LocalContext.current
        val previewViewModel: SellCarViewModel = viewModel(
        )
        previewViewModel.updateFuelType("휘발유")
        previewViewModel.updateTransmissionType("자동")

        SellCarDetailsScreen(
            sellCarViewModel = previewViewModel,
            onSystemBack = {},    
            onStepBack = {},      
            onNextClicked = {}
        )
    }
}
