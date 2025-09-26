package com.trever.android.ui.sellcar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel
import com.trever.android.ui.theme.backgroundColor
import com.trever.android.ui.theme.cardBackgroundColor
import com.trever.android.ui.theme.textPrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellCarAccidentHistoryScreen(
    sellCarViewModel: SellCarViewModel,
    onSystemBack: () -> Unit,    
    onStepBack: () -> Unit,      
    onNextClicked: () -> Unit
) {
    val uiState by sellCarViewModel.uiState.collectAsState()
    var accidentDetails by remember { mutableStateOf(uiState.accidentDetails) }
    val purpleColor = Color(0xFF6A11CB)
    val lightPurpleColor = Color(0xFF9F72FF)

    LaunchedEffect(uiState.hasAccidentHistory) {
        if (uiState.hasAccidentHistory == false) {
            accidentDetails = ""
        }
    }

    LaunchedEffect(uiState.accidentDetails) {
        if (accidentDetails != uiState.accidentDetails) {
            accidentDetails = uiState.accidentDetails
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.backgroundColor,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onSystemBack) { 
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로 가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.backgroundColor)
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

                Text("사고이력을 선택해주세요", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val accidentOptions = listOf("있음" to true, "없음" to false)
                    accidentOptions.forEach { (text, value) ->
                        val isSelected = uiState.hasAccidentHistory == value
                        Button(
                            onClick = { sellCarViewModel.updateHasAccidentHistory(value) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) lightPurpleColor else MaterialTheme.colorScheme.cardBackgroundColor,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.textPrimaryColor else MaterialTheme.colorScheme.textPrimaryColor
                            ),
                            border = if (!isSelected) BorderStroke(1.dp, Color.LightGray) else null
                        ) {
                            Text(text)
                        }
                    }
                }

                AnimatedVisibility(visible = uiState.hasAccidentHistory == true) {
                    Column {
                        Spacer(modifier = Modifier.height(32.dp))
                        Text("사고 정보에 대해 입력해주세요", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = accidentDetails,
                            onValueChange = { accidentDetails = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            placeholder = { Text("사고 정보에 대해 입력해주세요") },
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = purpleColor,
                                unfocusedBorderColor = if(accidentDetails.isNotEmpty()) purpleColor else Color.LightGray,
                                focusedContainerColor = MaterialTheme.colorScheme.cardBackgroundColor,
                                unfocusedContainerColor = MaterialTheme.colorScheme.cardBackgroundColor
                            ),
                            enabled = uiState.hasAccidentHistory == true
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
                        if (uiState.hasAccidentHistory == true) {
                            sellCarViewModel.updateAccidentDetails(accidentDetails)
                        } else {
                            sellCarViewModel.updateAccidentDetails("")
                        }
                        onNextClicked()
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = purpleColor,
                        disabledContainerColor = Color.LightGray
                    ),
                    enabled = uiState.hasAccidentHistory == false || (uiState.hasAccidentHistory == true && accidentDetails.isNotBlank()),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Text("다음", fontSize = 18.sp, color = MaterialTheme.colorScheme.textPrimaryColor, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=360dp,height=800dp,dpi=480")
@Composable
fun SellCarAccidentHistoryScreenPreview() {
    MaterialTheme {
        val context = LocalContext.current
        val previewViewModel: SellCarViewModel = viewModel(
        )
        previewViewModel.updateHasAccidentHistory(true)

        SellCarAccidentHistoryScreen(
            sellCarViewModel = previewViewModel,
            onSystemBack = {},    
            onStepBack = {},      
            onNextClicked = {}
        )
    }
}

@Preview(showBackground = true, device = "spec:width=360dp,height=800dp,dpi=480")
@Composable
fun SellCarAccidentHistoryScreenNoAccidentPreview() {
    MaterialTheme {
        val context = LocalContext.current
        val previewViewModel: SellCarViewModel = viewModel(
        )
        previewViewModel.updateHasAccidentHistory(false)

        SellCarAccidentHistoryScreen(
            sellCarViewModel = previewViewModel,
            onSystemBack = {},
            onStepBack = {},
            onNextClicked = {}
        )
    }
}

