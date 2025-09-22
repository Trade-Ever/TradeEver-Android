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
import androidx.compose.runtime.remember // remember 추가 (Preview용)
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// NavController import 제거
import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel
import com.trever.android.ui.theme.AppTheme
import com.trever.android.ui.theme.Grey_100
import java.util.Calendar

// 더미 데이터 정의는 이전과 동일하게 유지
val currentYear = Calendar.getInstance().get(Calendar.YEAR)
val dummyYears = (currentYear downTo currentYear - 20).toList().map { it.toString() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectYearScreen(
    viewModel: SellCarViewModel,
    onSystemBack: () -> Unit, // 시스템 뒤로가기 콜백
    onYearSelected: () -> Unit, // 연식 선택 완료 콜백
    // onStepBack: (() -> Unit)? = null // 화면 내 이전 버튼용 (필요시 추가)
) {
    val uiState by viewModel.uiState.collectAsState()
    val titleText = if (uiState.selectedManufacturer.isNotBlank() && uiState.selectedModel.isNotBlank()) {
        "${uiState.selectedManufacturer} ${uiState.selectedModel} 연식 선택"
    } else {
        "연식 선택"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titleText, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onSystemBack) { // onSystemBack 콜백 사용
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로 가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            items(dummyYears) { year ->
                YearRow(year = year) {
                    viewModel.updateSelectedYear(year.toInt())
                    onYearSelected() // 다음 화면으로 전환 콜백 호출
                }
                HorizontalDivider(color = Grey_100)
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
//        val previewViewModel = remember { SellCarViewModel() }
//        previewViewModel.updateSelectedManufacturer("현대")
//        previewViewModel.updateSelectedModel("아반떼")
//        SelectYearScreen(
//            viewModel = previewViewModel,
//            onSystemBack = {},
//            onYearSelected = {}
//        )
//    }
//}
