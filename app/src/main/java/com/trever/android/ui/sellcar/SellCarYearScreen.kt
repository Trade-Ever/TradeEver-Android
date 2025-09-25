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
import com.trever.android.ui.theme.backgroundColor
import com.trever.android.ui.theme.textPrimaryColor
import kotlinx.coroutines.flow.first
import java.util.Calendar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellCarYearScreen(
    sellCarViewModel: SellCarViewModel,
    onNavigateBack: () -> Unit,
    onNextClicked: () -> Unit
) {
    val uiState by sellCarViewModel.uiState.collectAsState()
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val yearRange = (currentYear + 2 downTo currentYear - 30).toList()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.backgroundColor,
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
                    containerColor = MaterialTheme.colorScheme.backgroundColor,
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
            CustomProgressBar(totalSteps = 6, currentStep = 2) 

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "차량 모델을 입력해주세요", 
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFF6A11CB)), 
                color = MaterialTheme.colorScheme.textPrimaryColor
            ) {
                Text(
                    text = uiState.selectedModel.ifEmpty { "(모델 정보 없음)" }, 
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

            YearPicker(
                years = yearRange,
                initialYear = uiState.selectedYear,
                onYearSelected = { year ->
                    sellCarViewModel.updateSelectedYear(year)
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onNextClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A11CB))
            ) {
                Text("다음", fontSize = 18.sp, color = MaterialTheme.colorScheme.textPrimaryColor)
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

    val density = LocalDensity.current
    var firstLaunch by remember { mutableStateOf(true) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.isNotEmpty() }
            .first { it }

        selectedYear = initialYear
        onYearSelected(initialYear)

        val index = years.indexOf(initialYear).coerceIn(0, years.lastIndex)
        val layoutInfo = listState.layoutInfo
        val visibleItems = layoutInfo.visibleItemsInfo
        if (visibleItems.isNotEmpty()) {
            val itemHeight = visibleItems.first().size
            val viewportHeight = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
            val paddingTopPx = with(density) { 60.dp.roundToPx() } 
            val spacingPx = with(density) { 8.dp.roundToPx() }      

            val offset =
                index * (itemHeight + spacingPx) - viewportHeight / 2 + itemHeight / 2 + paddingTopPx
            listState.scrollToItem(0, offset.coerceAtLeast(0))
        }

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

