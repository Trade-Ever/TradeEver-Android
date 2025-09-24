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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectModelScreen(
    viewModel: SellCarViewModel,
    onSystemBack: () -> Unit,
    onModelSelected: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedManufacturer = uiState.selectedManufacturer
    val carNameList = uiState.carNameList // List<String>

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedManufacturer.isNotEmpty()) "$selectedManufacturer 차명 선택" else "차명 선택",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onSystemBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로 가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        if (uiState.isLoadingCarNames) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (carNameList.isEmpty() && selectedManufacturer.isNotEmpty() && !uiState.isLoadingCarNames) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${selectedManufacturer}의 차량 모델 정보가 없습니다.\n다른 제조사를 선택해보세요.",
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        } else if (selectedManufacturer.isEmpty()) {
             Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "먼저 제조사를 선택해주세요.",
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(carNameList) { carName ->
                    ModelRow(carName = carName) {
                        viewModel.updateSelectedModel(carName) // updateSelectedCarName -> updateSelectedModel
                        onModelSelected()
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
fun ModelRow(carName: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = carName,
            fontSize = 16.sp,
            color = Color.Black
        )
    }
}

//@Preview(showBackground = true)
//@Composable
//fun SelectModelScreenPreview() {
//    AppTheme {
//        val dummyManufacturer = "현대"
//        val dummyCarNames = listOf("쏘나타", "그랜저", "아반떼", "투싼")
//
//        Scaffold(
//            topBar = {
//                TopAppBar(
//                    title = { Text("$dummyManufacturer 모델 선택", fontWeight = FontWeight.Bold) },
//                    navigationIcon = {
//                        IconButton(onClick = {}) {
//                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로 가기")
//                        }
//                    },
//                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
//                )
//            }
//        ) { paddingValues ->
//            LazyColumn(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(paddingValues)
//            ) {
//                items(dummyCarNames) { carName ->
//                    ModelRow(carName = carName) {}
//                    HorizontalDivider(
//                        color = MaterialTheme.colorScheme.G_100,
//                        modifier = Modifier.padding(horizontal = 16.dp)
//                    )
//                }
//            }
//        }
//    }
//}
