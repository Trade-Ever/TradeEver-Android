package com.trever.android.ui.sellcar

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel
import kotlinx.coroutines.flow.first
import java.util.Calendar

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
    val yearRange = (currentYear + 2 downTo currentYear - 30).toList()

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
            YearPicker(
                years = yearRange,
                initialYear = uiState.selectedYear,
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
    initialYear: Int,
    onYearSelected: (Int) -> Unit
) {
    var selectedYear by remember { mutableStateOf(initialYear) }
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = years.indexOf(initialYear)
    )
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // LazyColumn
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp),
        state = listState,
        flingBehavior = flingBehavior,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(years) { year ->
            val isSelected = year == selectedYear
            Text(
                text = "$year",
                fontSize = if (isSelected) 36.sp else 24.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.Black else Color.Gray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .alpha(if (isSelected) 1f else 0.7f)
                    .clickable {
                        selectedYear = year
                        onYearSelected(year)
                    },
                textAlign = TextAlign.Center
            )
        }
    }

    // 중앙 하이라이트 로직
    val density = LocalDensity.current
    var firstLaunch by remember { mutableStateOf(true) }

    LaunchedEffect(listState) {
        // 1️⃣ 첫 레이아웃 완료 대기
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.isNotEmpty() }
            .first { it }

        // 2️⃣ 초기 선택값
        selectedYear = initialYear
        onYearSelected(initialYear)

        // 3️⃣ 중앙 정렬 계산
        val index = years.indexOf(initialYear).coerceIn(0, years.lastIndex)
        val layoutInfo = listState.layoutInfo
        val visibleItems = layoutInfo.visibleItemsInfo
        if (visibleItems.isNotEmpty()) {
            val itemHeight = visibleItems.first().size
            val viewportHeight = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
            val paddingTopPx = with(density) { 60.dp.roundToPx() } // contentPadding.vertical / 2
            val spacingPx = with(density) { 8.dp.roundToPx() }      // verticalArrangement.spacedBy

            val offset =
                index * (itemHeight + spacingPx) - viewportHeight / 2 + itemHeight / 2 + paddingTopPx
            listState.scrollToItem(0, offset.coerceAtLeast(0))
        }

        // 4️⃣ 스크롤 시 중앙 아이템 업데이트
        snapshotFlow { listState.layoutInfo }
            .collect { layout ->
                if (firstLaunch) {
                    firstLaunch = false
                    return@collect
                }

                val viewportCenter = (layout.viewportStartOffset + layout.viewportEndOffset) / 2
                val centerItem = layout.visibleItemsInfo.minByOrNull { item ->
                    kotlin.math.abs(item.offset + item.size / 2 - viewportCenter)
                }
                centerItem?.let { info ->
                    val year = years.getOrNull(info.index)
                    if (year != null && year != selectedYear) {
                        selectedYear = year
                        onYearSelected(year)
                    }
                }
            }
    }
}

//@Preview(showBackground = true, device = "spec:shape=Normal,width=360,height=640,unit=dp,dpi=480")
//@Composable
//fun SellCarYearScreenPreview() {
//    YourAppTheme { // 실제 테마로 교체 필요
//        val previewViewModel = SellCarViewModel()
//        previewViewModel.updateCurrentStep(5) // 이 화면은 5단계로 가정
//        previewViewModel.updateSelectedModel("현대 아반떼 SN7") // 더미 모델 설정
//        previewViewModel.updateSelectedYear(2023) // 초기 선택 연도 설정
//
//        SellCarYearScreen(
//            sellCarViewModel = previewViewModel,
//            onNavigateBack = {},
//            onNextClicked = {}
//        )
//    }
//}
