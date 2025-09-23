package com.trever.android.ui.search



import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import com.trever.android.R
import com.trever.android.data.remote.VehicleSearchRequest
import com.trever.android.ui.components.AppFilledButton
import com.trever.android.ui.components.AppOutlinedButton
import com.trever.android.ui.theme.G_200
import com.trever.android.ui.theme.backgroundColor
import kotlin.collections.get


import kotlin.compareTo

import kotlin.div
import kotlin.text.contains
import kotlin.text.get
import kotlin.text.toInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(

    viewModel: SearchViewModel, // 기본값 제거
    onSearch: (String) -> Unit = {},
    onClearRecent: (String) -> Unit = {},
    onFilterClick: (String) -> Unit = {},
    onReset: () -> Unit = {},
    onShowResults: () -> Unit = {},
            onBack: () -> Unit = {}
) {
    val cs = MaterialTheme.colorScheme
    val searchText by viewModel.searchText.collectAsState()




    // 바텀시트 상태 관리
    var showBottomSheet by remember { mutableStateOf<String?>(null) }
    val yearRange by viewModel.yearRange.collectAsState()
    val distanceRange by viewModel.distanceRange.collectAsState()
    val priceRange by viewModel.priceRange.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val selectedManufacturer by viewModel.selectedManufacturer.collectAsState()
    val selectedCarName by viewModel.selectedCarName.collectAsState()
    val selectedCarModel by viewModel.selectedCarModel.collectAsState()

    val recentSearches by viewModel.recentSearches.collectAsState()
    val carTypeMap = mapOf(
        "LARGE" to "대형",
        "MID_SIZE" to "중형",
        "SEMI_MID_SIZE" to "준중형",
        "SMALL" to "소형",
        "SPORTS" to "스포츠",
        "SUV" to "SUV",
        "VAN" to "승합차",
        "COMPACT" to "경차"
    )
    val carTypeMapReverse = carTypeMap.entries.associate { (k, v) -> v to k }


    BackHandler {
        // 필터값 초기화
        viewModel.yearRange.value = null
        viewModel.distanceRange.value = null
        viewModel.priceRange.value = null
        viewModel.selectedType.value = null
        viewModel.selectedManufacturer.value = null
        viewModel.selectedCarName.value = null
        viewModel.selectedCarModel.value = null
        onBack()
    }

    // 화면 진입 시 최근 검색어 불러오기
    LaunchedEffect(Unit) {
        viewModel.fetchRecentSearches()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = cs.backgroundColor
                ),
                title = {
                    TextField(
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.search),
                                contentDescription = null
                            )
                        },
                        value = searchText,
                        onValueChange = { viewModel.searchText.value = it },
                        placeholder = { Text("차량을 검색하세요") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(end = 20.dp) // 오른쪽 "취소" 공간 확보
                        .clip(RoundedCornerShape(22.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF3F8FF),
                            unfocusedContainerColor = Color(0xFFF3F8FF),
                            disabledContainerColor = Color(0xFFF3F8FF),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium,
                    )
                },
                actions = {
                    TextButton(onClick = {
                        // 필터값 초기화
                        viewModel.yearRange.value = null
                        viewModel.distanceRange.value = null
                        viewModel.priceRange.value = null
                        viewModel.selectedType.value = null
                        onBack()
                    }) {
                        Text("취소", color = Color(0xFF6C4DF4))
                    }
                },

            )
        },
        containerColor = Color.White,
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .navigationBarsPadding()
                    .imePadding(), // 키보드 대응
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppOutlinedButton(
                    text = "초기화",
                    onClick =  {
                        viewModel.yearRange.value = null
                        viewModel.distanceRange.value = null
                        viewModel.priceRange.value = null
                        viewModel.selectedType.value = null
                        viewModel.selectedManufacturer.value = null
                        viewModel.selectedCarName.value = null
                        viewModel.selectedCarModel.value = null
                    // 기타 상태도 필요시 null로
                },
                    modifier = Modifier.weight(1f)
                )
                AppFilledButton(
                    onClick = {
                        val request = VehicleSearchRequest(
                            keyword = searchText,
                            manufacturer = selectedManufacturer?.takeIf { it.isNotEmpty() },
                            carName = selectedCarName?.takeIf { it.isNotEmpty() },
                            carModel = selectedCarModel?.takeIf { it.isNotEmpty() },
                            yearStart = yearRange?.start?.toInt(),
                            yearEnd = yearRange?.endInclusive?.toInt(),
                            mileageStart = distanceRange?.start?.toInt(),
                            mileageEnd = distanceRange?.endInclusive?.toInt(),
                            priceStart = priceRange?.start?.toInt()?.times(100),
                            priceEnd = priceRange?.endInclusive?.toInt()?.times(100),
                            vehicleType = selectedType?.let { carTypeMapReverse[it] },
                            page = 0,
                            size = 10
                        )
                        Log.d("SearchRequest", request.toString())
                        viewModel.searchVehicles(request)
                        onShowResults()
                        // 검색 결과 화면으로 이동 (예: navController.navigate("search/results"))
                    },
                    modifier = Modifier.weight(1f),
                    text = "매불 보기"
                )
            }
        }
    ) { innerPadding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .background(Color.White)

    ) {

        // 검색바

        Spacer(Modifier.height(30.dp))
        // 최근 검색
        Text("최근 검색", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 16.dp))
        Spacer(Modifier.height(8.dp))
        recentSearches.forEach { keyword ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp,vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.search),
                    contentDescription = null,
                    tint = cs.G_200
                )
                Spacer(Modifier.width(8.dp))
                Text(keyword, color = cs.G_200, modifier = Modifier.weight(1f))
                Icon(
                    tint = cs.G_200,
                    painter = painterResource(id = R.drawable.close), // X 아이콘
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onClearRecent(keyword) }
                )
            }
            Divider(
                color = cs.G_200,
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth() // Divider에는 padding 없음
            )

        }
        Spacer(Modifier.height(70.dp))
        // 필터 목록
        FilterRow(
            "제조사 · 모델",
            listOfNotNull(selectedManufacturer, selectedCarModel)
                .filter { it.isNotEmpty() }
                .joinToString(" · "),
        ) { onFilterClick("model") }
        FilterRow("연식", yearRange?.let { "${it.start.toInt()}년 ~ ${it.endInclusive.toInt()}년" } ?: "")  {
            showBottomSheet = "year"
        }
        FilterRow("주행거리", distanceRange?.let { "${it.start.toInt()}km ~ ${it.endInclusive.toInt()}km" } ?: "") {
            showBottomSheet = "distance"
        }
        FilterRow(
            "가격",
            priceRange?.let {
                val start = it.start.toInt() * 100
                val end = it.endInclusive.toInt() * 100
                val startStr = if (start % 10000 == 0) "${start / 10_000}억"
                else if (start > 10_000) "${start / 10_000}억 ${start % 10_000}만원"
                else "${start}만원"
                val endStr = if (end % 10000 == 0) "${end / 10_000}억"
                else if (end > 10_000) "${end / 10_000}억 ${end % 10_000}만원"
                else "${end}만원"
                "$startStr ~ $endStr"
            } ?: ""
        ) {
            showBottomSheet = "price"
        }


        // FilterRow에서 선택된 차종 표시
        FilterRow(
            "차종",
            selectedType ?: ""
        ) { showBottomSheet = "type" }

// 바텀시트에서 선택 완료 시 상태 갱신
        if (showBottomSheet == "type") {
            CarTypeSelectBottomSheet(
                selectedType = selectedType,
                onDismiss = { showBottomSheet = null },
                onConfirm = {
                    viewModel.selectedType.value = it
                    showBottomSheet = null
                }
            )
        }
        Spacer(Modifier.weight(1f))

        // 바텀시트 표시
        when (showBottomSheet) {
            "year" -> RangeSelectBottomSheet(
                title = "연식을 선택해 주세요",
                unit = "년",
                valueRange = 1998f..2025f,
                steps = 28,
                initialRange = yearRange ?: (1998f..2025f),
                onDismiss = { showBottomSheet = null },
                onConfirm = {
                    viewModel.yearRange.value = it
                    showBottomSheet = null
                }
            )
            "distance" -> RangeSelectBottomSheet(
                title = "주행 거리를 선택해 주세요",
                unit = "km",
                valueRange = 0f..300_000f,
                steps = 31,
                initialRange = distanceRange ?: (0f..300_000f),
                onDismiss = { showBottomSheet = null },
                onConfirm = {
                    viewModel.distanceRange.value = it
                    showBottomSheet = null
                }
            )
            "price" -> RangeSelectBottomSheet(
                title = "가격을 선택해 주세요",
                unit = "만원",
                valueRange = 0f..300f, // 0~3억, 1000만원 단위
                steps = 31,
                initialRange = priceRange ?: (0f..300f),
                onDismiss = { showBottomSheet = null },
                onConfirm = {
                    viewModel.priceRange.value = it
                    showBottomSheet = null
                }
            )}

    }}
}

@Composable
private fun FilterRow(title: String, value: String, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, modifier = Modifier.weight(1f))
        Text(value, color = Color(0xFF6C4DF4))
        Spacer(Modifier.width(8.dp))
        Icon(
            modifier = Modifier.size(18.dp),
            painter = painterResource(id = R.drawable.arrow_right_1),
            contentDescription = null,

        )

    }
        Divider(color = MaterialTheme.colorScheme.G_200, thickness = 1.dp, modifier = Modifier.fillMaxWidth())}
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun RangeSelectBottomSheet(
    title: String,
    unit: String,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    initialRange: ClosedFloatingPointRange<Float>,
    onDismiss: () -> Unit,
    onConfirm: (ClosedFloatingPointRange<Float>) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    var range by remember { mutableStateOf(initialRange) }
    val stepSize = when (unit) {
        "km" -> 10_000f
        "만원" -> 10f
        else -> 1f
    }
    val displayValue: (Float) -> String = when (unit) {
        "년" -> { v -> "${v.toInt()}년" }
        "km" -> { v -> "${(v / 1_0000).toInt()}만km" }
        "만원" -> { v ->
            val price = v.toInt() * 1000 // 1000만원 단위
            if (price >= 100_000) {
                val 억 = price / 100_000
                val 만 = (price % 100_000) / 10000
                if (만 == 0) "${억}억"
                else "${억}억 ${만}000만원"
            } else if(price == 0) {
                "0만원"
            }
            else {
                "${(price / 10000)}000만원"
            }
        }
        else -> { v -> v.toInt().toString() + unit }
    }
    ModalBottomSheet(
        containerColor = cs.backgroundColor,
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(24.dp))
            RangeSlider(
                value = range,
                onValueChange = {
                    val snappedStart = ((it.start - valueRange.start) / stepSize).toInt() * stepSize + valueRange.start
                    val snappedEnd = ((it.endInclusive - valueRange.start) / stepSize).toInt() * stepSize + valueRange.start
                    range = snappedStart..snappedEnd
                },
                valueRange = valueRange,
                steps = steps,
                colors = SliderDefaults.colors(
                    activeTrackColor = Color(0xFF6C4DF4),
                    inactiveTrackColor = Color(0xFFD3D3D3),
                    thumbColor = Color.White,
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent
                ),
                modifier = Modifier.height(48.dp), // thumb 크기 조정
                startThumb = {
                    Box(
                        Modifier
                            .size(28.dp)
                            .shadow(4.dp, CircleShape)
                            .background(Color.White, CircleShape)
                    )
                },
                endThumb = {
                    Box(
                        Modifier
                            .size(28.dp)
                            .shadow(4.dp, CircleShape)
                            .background(Color.White, CircleShape)
                    )
                }

            )
            Spacer(Modifier.height(8.dp))
            Text(
                "${displayValue(range.start)} ~ ${displayValue(range.endInclusive)}",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(32.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppOutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    text = "취소"
                )
                AppFilledButton(
                    onClick = { onConfirm(range) },
                    modifier = Modifier.weight(1f),
                    text = "확인"
                )
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CarTypeSelectBottomSheet(
    carTypes: List<String> = listOf("준중형", "대형", "스포츠", "소형", "SUV", "승합차", "경차", "중형"),
    enabledTypes: Set<String> = setOf("준중형", "대형", "스포츠", "소형", "SUV", "승합차", "경차", "중형"),
    selectedType: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (String?) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    var selected by remember { mutableStateOf(selectedType) }

    ModalBottomSheet(
        containerColor = Color.White,
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("차종을 선택해주세요", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(32.dp))
            for (row in 0..1) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    carTypes.drop(row * 4).take(4).forEach { type ->
                        val enabled = enabledTypes.contains(type)
                        val isSelected = selected == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(
                                    when {
                                        !enabled -> Color(0xFFF5F5F5)
                                        isSelected -> Color(0xFF8C6CFF)
                                        else -> Color.White
                                    }
                                )
                                .border(
                                    width = 1.dp,
                                    color = when {
                                        !enabled -> Color(0xFFE0E0E0)
                                        isSelected -> Color(0xFF8C6CFF)
                                        else -> Color(0xFFE0E0E0)
                                    },
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .clickable(enabled = enabled) {
                                    selected = if (isSelected) null else type
                                }
                                .height(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                type,
                                color = when {
                                    !enabled -> Color(0xFFCCCCCC)
                                    isSelected -> Color.White
                                    else -> cs.G_200
                                },
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            Spacer(Modifier.height(20.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                AppOutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    text = "취소"
                )
                AppFilledButton(
                    onClick = { onConfirm(selected) },
                    modifier = Modifier.weight(1f),
                    text = "확인"
                )
            }
        }
    }
}