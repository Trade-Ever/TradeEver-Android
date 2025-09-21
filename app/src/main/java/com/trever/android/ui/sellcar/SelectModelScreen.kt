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

// 더미 데이터 정의는 이전과 동일하게 유지
data class ModelItem(val name: String)
val dummyModelsByManufacturer = mapOf(
    "현대" to listOf(ModelItem("그랜저"), ModelItem("아반떼"), ModelItem("쏘나타"), ModelItem("싼타페"), ModelItem("스타렉스"), ModelItem("i10"), ModelItem("i30"), ModelItem("i40"), ModelItem("ST1"), ModelItem("갤로퍼")),
    "제네시스" to listOf(ModelItem("G80"), ModelItem("GV70"), ModelItem("G90")),
    "기아" to listOf(ModelItem("K5"), ModelItem("쏘렌토"), ModelItem("카니발"))
)
val popularModelsByManufacturer = mapOf(
    "현대" to listOf(ModelItem("그랜저"), ModelItem("아반떼"), ModelItem("쏘나타"), ModelItem("싼타페"), ModelItem("스타렉스")),
    "제네시스" to listOf(ModelItem("G80"), ModelItem("GV70")),
    "기아" to listOf(ModelItem("K5"), ModelItem("쏘렌토"))
)
val alphabeticalModelsByManufacturer = mapOf(
    "현대" to listOf(ModelItem("갤로퍼"), ModelItem("그랜저"), ModelItem("i10"), ModelItem("i30"), ModelItem("i40"), ModelItem("ST1"), ModelItem("쏘나타"), ModelItem("싼타페"), ModelItem("스타렉스"), ModelItem("아반떼")),
    "제네시스" to listOf(ModelItem("G80"), ModelItem("G90"), ModelItem("GV70")),
    "기아" to listOf(ModelItem("K5"), ModelItem("카니발"), ModelItem("쏘렌토"))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectModelScreen(
    viewModel: SellCarViewModel,
    onSystemBack: () -> Unit, // 시스템 뒤로가기 콜백
    onModelSelected: () -> Unit, // 모델 선택 완료 콜백
    // onStepBack: (() -> Unit)? = null // 화면 내 이전 버튼용 (필요시 추가)
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedManufacturer = uiState.selectedManufacturer

    val popularModels = popularModelsByManufacturer[selectedManufacturer] ?: emptyList()
    val alphabeticalModels = alphabeticalModelsByManufacturer[selectedManufacturer] ?: emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("모델 선택", fontWeight = FontWeight.Bold) },
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
            item {
                Text(
                    text = "인기모델",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 4.dp)
                )
            }
            items(popularModels) { model ->
                ModelRow(model = model) {
                    viewModel.updateSelectedModel(model.name)
                    onModelSelected() // 다음 화면으로 전환 콜백 호출
                }
                HorizontalDivider(color = Grey_100)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "이름순",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 4.dp)
                )
            }
            items(alphabeticalModels) { model ->
                ModelRow(model = model) {
                    viewModel.updateSelectedModel(model.name)
                    onModelSelected() // 다음 화면으로 전환 콜백 호출
                }
                HorizontalDivider(color = Grey_100)
            }
        }
    }
}

@Composable
fun ModelRow(model: ModelItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = model.name, fontSize = 16.sp, color = Color.Black)
    }
}

@Preview(showBackground = true)
@Composable
fun SelectModelScreenPreview() {
    AppTheme {
        val previewViewModel = remember { SellCarViewModel() }
        previewViewModel.updateSelectedManufacturer("현대")
        SelectModelScreen(
            viewModel = previewViewModel,
            onSystemBack = {},
            onModelSelected = {}
        )
    }
}
