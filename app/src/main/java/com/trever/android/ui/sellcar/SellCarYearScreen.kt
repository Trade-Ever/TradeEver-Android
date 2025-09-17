package com.trever.android.ui.sellcar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel
import java.util.Calendar
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

// import com.trever.android.ui.theme.YourAppTheme // 실제 테마로 교체 필요

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellCarYearScreen(
    sellCarViewModel: SellCarViewModel,
    onNavigateBack: () -> Unit,
    onNextClicked: () -> Unit
) {
    val uiState by sellCarViewModel.uiState.collectAsState()
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    // 연식 범위: 현재 연도부터 30년 전까지 (예시)
    val yearRange = (currentYear downTo currentYear - 30).toList()

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("연식 입력") },
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
            CustomProgressBar(totalSteps = 6, currentStep = 2) // ViewModel의 currentStep 사용

            Spacer(modifier = Modifier.height(32.dp))

            // 선택된 차량 모델 표시부
            Text(
                text = "차량 모델을 입력해주세요", // 이미지상의 레이블
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFF6A11CB)), // 이미지 보라색 테두리
                color = Color.White
            ) {
                Text(
                    text = uiState.selectedModel.ifEmpty { "(모델 정보 없음)" }, // ViewModel의 모델 사용
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "연식을 입력해주세요",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 연식 선택기
            YearPicker( // 높이를 제한하여 일부 항목만 보이도록 함
                modifier = Modifier.height(180.dp), // 5개 항목 정도 보이도록 높이 조절
                years = yearRange,
                selectedYear = uiState.selectedYear,
                onYearSelected = { year ->
                    sellCarViewModel.updateSelectedYear(year)
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            // 다음 버튼
            Button(
                onClick = onNextClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A11CB))
            ) {
                Text("다음", fontSize = 18.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun YearPicker(
    modifier: Modifier = Modifier,
    years: List<Int>,
    selectedYear: Int,
    onYearSelected: (Int) -> Unit
) {
    val listState = rememberLazyListState()
    val centralItemOffset = years.indexOf(selectedYear) - 2 // 선택된 연도가 중앙에서 3번째 오도록 (0-indexed)

    // 초기 스크롤 위치 설정 (선택된 연도가 중앙에 오도록)
    LaunchedEffect(selectedYear, years) {
        val targetIndex = years.indexOf(selectedYear)
        if (targetIndex != -1) {
            // 중앙에 오도록 스크롤, 아이템이 최소 5개는 있어야 중앙 정렬 의미가 있음
            val scrollIndex = (targetIndex - 2).coerceAtLeast(0)
            listState.scrollToItem(scrollIndex)
        }
    }

    // 스크롤 중 중앙 아이템 감지 및 업데이트 (선택 사항 - 스크롤 멈췄을때만 업데이트로 변경 가능)
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .map { visibleItems ->
                if (visibleItems.isEmpty()) return@map selectedYear // 비어있으면 기존 값
                // 중앙에 가장 가까운 아이템 찾기 (개선된 로직 필요)
                // 여기서는 간단하게 첫번째 보이는 아이템 +2 (중앙으로 가정)
                val firstVisibleIndex = listState.firstVisibleItemIndex
                val centralVisibleIndex = firstVisibleIndex + (listState.layoutInfo.visibleItemsInfo.size / 2)
                years.getOrElse(centralVisibleIndex) { selectedYear }
            }
            .distinctUntilChanged()
            .collect { year ->
                 // 스크롤 중 실시간 업데이트보다는, 스크롤 멈췄을 때 업데이트가 사용자 경험에 더 좋을 수 있음
                 // onYearSelected(year) // 이 부분은 사용자가 스크롤을 멈췄을때 호출하는게 더 좋습니다.
            }
    }
    // 사용자가 스크롤을 멈췄을 때 중앙 연도 업데이트
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress && listState.layoutInfo.visibleItemsInfo.isNotEmpty()) {
            val visibleItems = listState.layoutInfo.visibleItemsInfo
            val viewportHeight = listState.layoutInfo.viewportSize.height
            val centerLine = viewportHeight / 2

            val centralItem = visibleItems.minByOrNull { kotlin.math.abs((it.offset + it.size / 2) - centerLine) }
            centralItem?.index?.let {
                onYearSelected(years[it])
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = 60.dp) // 위아래로 여백을 주어 중앙 아이템이 잘 보이도록 함
    ) {
        items(years.size) { index ->
            val year = years[index]
            val isSelected = (year == selectedYear)
            Text(
                text = year.toString(),
                fontSize = if (isSelected) 36.sp else 24.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.Black else Color.Gray,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .alpha(if (isSelected) 1f else 0.7f)
            )
        }
    }
}

@Preview(showBackground = true, device = "spec:shape=Normal,width=360,height=640,unit=dp,dpi=480")
@Composable
fun SellCarYearScreenPreview() {
    YourAppTheme { // 실제 테마로 교체 필요
        val previewViewModel = SellCarViewModel()
        previewViewModel.updateCurrentStep(5) // 이 화면은 5단계로 가정
        previewViewModel.updateSelectedModel("현대 아반떼 SN7") // 더미 모델 설정
        previewViewModel.updateSelectedYear(2023) // 초기 선택 연도 설정

        SellCarYearScreen(
            sellCarViewModel = previewViewModel,
            onNavigateBack = {},
            onNextClicked = {}
        )
    }
}
