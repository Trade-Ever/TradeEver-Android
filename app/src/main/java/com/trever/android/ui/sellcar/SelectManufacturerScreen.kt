package com.trever.android.ui.sellcar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import com.trever.android.ui.theme.Grey_100

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SelectManufacturerScreen(
    viewModel: SellCarViewModel,
    onSystemBack: () -> Unit,
    onManufacturerSelected: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val manufacturerData = uiState.manufacturerDataMap

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("제조사 선택", fontWeight = FontWeight.Bold) },
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
        if (uiState.isLoadingManufacturers) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (manufacturerData.isEmpty() && !uiState.isLoadingManufacturers) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "제조사 정보를 불러올 수 없습니다.\n네트워크 연결을 확인 후 다시 시도해주세요.",
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                manufacturerData.keys.sorted().forEach { category ->
                    stickyHeader {
                        Text(
                            text = category,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                        )
                    }
                    val manufacturers = manufacturerData[category] ?: emptyList()
                    items(manufacturers) { manufacturerName ->
                        ManufacturerRow(manufacturerName = manufacturerName) {
                            viewModel.updateSelectedManufacturer(category, manufacturerName)
                            onManufacturerSelected()
                        }
                        HorizontalDivider(
                            color = Grey_100,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ManufacturerRow(manufacturerName: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = manufacturerName,
            fontSize = 16.sp,
            color = Color.Black
        )
    }
}

//@Preview(showBackground = true)
//@Composable
//fun SelectManufacturerScreenPreview() {
//    AppTheme {
//        val dummyData = mapOf(
//            "국산" to listOf("현대", "기아", "제네시스"),
//            "수입" to listOf("BMW", "벤츠", "아우디")
//        )
//
//        Scaffold(
//            topBar = {
//                TopAppBar(
//                    title = { Text("제조사 선택", fontWeight = FontWeight.Bold) },
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
//                dummyData.keys.sorted().forEach { category ->
//                    item {
//                        Text(
//                            text = category,
//                            fontSize = 16.sp,
//                            fontWeight = FontWeight.SemiBold,
//                            modifier = Modifier.padding(16.dp)
//                        )
//                    }
//                    items(dummyData[category] ?: emptyList()) { manufacturerName ->
//                        ManufacturerRow(manufacturerName = manufacturerName) {}
//                        HorizontalDivider(
//                            color = Grey_100,
//                            modifier = Modifier.padding(horizontal = 16.dp)
//                        )
//                    }
//                }
//            }
//        }
//    }
//}