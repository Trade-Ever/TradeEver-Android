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
import com.trever.android.ui.theme.Grey_100

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectYearScreen(
    viewModel: SellCarViewModel,
    onSystemBack: () -> Unit,
    onYearSelected: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val yearList = uiState.yearList

    val titleText = if (uiState.selectedModelName.isNotBlank()) {
        "${uiState.selectedModelName}"
    } else if (uiState.selectedModel.isNotBlank()) {
        "${uiState.selectedModel}"
    } else {
        "연식 선택"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$titleText 연식 선택", fontWeight = FontWeight.Bold) },
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
        if (uiState.isLoadingYears) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (yearList.isEmpty() && !uiState.isLoadingYears) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "선택하신 모델의 연식 정보가 없습니다.",
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                items(yearList) { year ->
                    YearRow(year = year.toString()) {
                        viewModel.updateSelectedYear(year)
                        onYearSelected()
                    }
                    HorizontalDivider(color = Grey_100)
                }
            }
        }
    }
}

@Composable
fun YearRow(year: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "$year 년", fontSize = 16.sp, color = Color.Black)
    }
}

//@Preview(showBackground = true)
//@Composable
//fun SelectYearScreenPreview() {
//    AppTheme {
//        val dummyYears = (2024 downTo 2010).toList()
//
//        Scaffold(
//            topBar = {
//                TopAppBar(
//                    title = { Text("현대 쏘나타 연식 선택", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
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
//                    .padding(horizontal = 16.dp)
//            ) {
//                items(dummyYears) { year ->
//                    YearRow(year = year.toString()) {}
//                    HorizontalDivider(color = Grey_100)
//                }
//            }
//        }
//    }
//}