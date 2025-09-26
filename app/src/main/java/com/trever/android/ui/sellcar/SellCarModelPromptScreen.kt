package com.trever.android.ui.sellcar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel
import com.trever.android.ui.theme.AppTheme
import com.trever.android.ui.theme.backgroundColor
import com.trever.android.ui.theme.cardBackgroundColor
import com.trever.android.ui.theme.textPrimaryColor
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellCarModelPromptScreen(
    sellCarViewModel: SellCarViewModel,
    onSystemBack: () -> Unit,
    onStepBack: () -> Unit,
    onSelectModelPathClicked: () -> Unit, 
    onConfirmAndProceedClicked: () -> Unit, 
) {
    val uiState by sellCarViewModel.uiState.collectAsState()
    val purpleColor = Color(0xFF6A11CB)

    val isModelSelected = uiState.selectedManufacturer.isNotBlank() && 
                          uiState.selectedModel.isNotBlank() && 
                          uiState.selectedYear != Calendar.getInstance().get(Calendar.YEAR) 

    val displayText = if (isModelSelected) {
        "${uiState.selectedManufacturer} ${uiState.selectedModel} ${uiState.selectedYear}"
    } else {
        "모델을 선택해주세요"
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
                    titleContentColor = MaterialTheme.colorScheme.textPrimaryColor,
                    navigationIconContentColor = MaterialTheme.colorScheme.textPrimaryColor
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
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "차량 모델을 입력해주세요",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.textPrimaryColor
            )
            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSelectModelPathClicked() }, 
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color.LightGray),
                color = MaterialTheme.colorScheme.cardBackgroundColor
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = displayText,
                        fontSize = 16.sp,
                        color = if (isModelSelected) Color.Black else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onStepBack,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.cardBackgroundColor,
                        contentColor = Color.Black
                    ),
                    border = BorderStroke(1.dp, Color.LightGray)
                ) {
                    Text(text = "이전", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onConfirmAndProceedClicked, 
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = purpleColor,
                        disabledContainerColor = Color.LightGray
                    ),
                    enabled = isModelSelected, 
                ) {
                    Text("다음", fontSize = 18.sp, color = MaterialTheme.colorScheme.textPrimaryColor, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

