package com.trever.android.ui.sellcar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel
import com.trever.android.ui.theme.AppTheme
import com.trever.android.ui.theme.G_100
import com.trever.android.ui.theme.backgroundColor
import com.trever.android.ui.theme.textPrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectModelNameScreen(
    viewModel: SellCarViewModel,
    onSystemBack: () -> Unit,
    onModelNameSelected: () -> Unit 
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedCarName = uiState.selectedModel 
    val modelNameList = uiState.modelNameList 

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedCarName.isNotEmpty()) "$selectedCarName 상세 모델" else "상세 모델 선택",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.textPrimaryColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onSystemBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로 가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.backgroundColor)
            )
        },
        containerColor = MaterialTheme.colorScheme.backgroundColor
    ) { paddingValues ->
        if (uiState.isLoadingModelNames) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (modelNameList.isEmpty() && selectedCarName.isNotEmpty() && !uiState.isLoadingModelNames) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${selectedCarName}의 세부 모델 정보가 없습니다.",
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(modelNameList) { modelName ->
                    ModelNameRow(modelName = modelName) {
                        viewModel.updateSelectedModelName(modelName)
                        onModelNameSelected()
                    }
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.G_100,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ModelNameRow(modelName: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = modelName,
            fontSize = 16.sp,
            color = Color.Black
        )
    }
}

