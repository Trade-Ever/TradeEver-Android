import com.trever.android.ui.search.RangeSelectBottomSheet


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trever.android.data.remote.toAuctionCarForDisplay
import com.trever.android.data.remote.toSearchCarItem
import com.trever.android.ui.components.ListingItem
import com.trever.android.domain.model.AuctionCar
import com.trever.android.domain.model.FirebaseAuction
import com.trever.android.domain.model.SearchCarItem
import com.trever.android.domain.model.toAuctionCar
import com.trever.android.domain.model.toAuctionCarForDisplay
import com.trever.android.ui.search.CarTypeSelectBottomSheet
import com.trever.android.ui.search.SearchViewModel
import com.trever.android.ui.theme.G_100
import com.trever.android.ui.theme.G_200
import com.trever.android.ui.theme.backgroundColor
import com.trever.android.ui.theme.textPrimaryColor

import kotlin.text.get
import kotlin.toString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultScreen(
    viewModel: SearchViewModel,
    cars: List<SearchCarItem>,
    onBack: () -> Unit,
    onCarClick: (SearchCarItem) -> Unit,
    onToggleLike: (AuctionCar) -> Unit,
    selectedPriceRange: String,
    selectedDistance: String,
    selectedSort: String,
    onSortClick: () -> Unit,
    yearRange: ClosedFloatingPointRange<Float>?,
    distanceRange: ClosedFloatingPointRange<Float>?,
    priceRange: ClosedFloatingPointRange<Float>?,
    selectedType: String?,
    onYearRangeClick: () -> Unit,
    onDistanceClick: () -> Unit,
    onPriceRangeClick: () -> Unit,
    onTypeClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    var showBottomSheet by remember { mutableStateOf<String?>(null) }
//    val cars = viewModel.searchResult.collectAsState().value?.vehicles?.map { it.toSearchCarItem() } ?: emptyList()
////    val cars = viewModel.searchCarItems.collectAsState().value
    val yearRange = viewModel.yearRange.collectAsState().value
    val distanceRange = viewModel.distanceRange.collectAsState().value
    val priceRange = viewModel.priceRange.collectAsState().value
    val selectedType = viewModel.selectedType.collectAsState().value

//    val cars = viewModel.searchCarItems.collectAsState().value

    fun formatDistance(range: ClosedFloatingPointRange<Float>?): String =
        if (range == null) "주행거리"
        else "${range.start.toInt()}km ~ ${range.endInclusive.toInt()}km"

    fun formatRange(range: ClosedFloatingPointRange<Float>?, unit: String): String {
        return if (range == null) unit else "${range.start.toInt()}$unit ~ ${range.endInclusive.toInt()}$unit"
    }
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.setupFirebaseListenerForSearchResults()
    }

    Scaffold(
        containerColor = cs.backgroundColor,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = cs.backgroundColor
                ),
                title = { Text("검색결과", color = MaterialTheme.colorScheme.textPrimaryColor) },
                actions = {
                    TextButton(onClick = onBack) {
                        Text("나가기", color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)
        ) {
            // 필터/정렬 영역
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),

            ) {


                OutlinedButton(
                    modifier = Modifier
                        .height(36.dp),
                    onClick = { showBottomSheet = "year" },
                    border = BorderStroke(1.dp, if (yearRange != null) cs.primary else MaterialTheme.colorScheme.G_100),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (yearRange != null) cs.primary else MaterialTheme.colorScheme.textPrimaryColor
                    )
                ) {
                    Text(
                        if (yearRange == null) "연식"
                        else "${yearRange.start.toInt()}년 ~ ${yearRange.endInclusive.toInt()}년",
                        color = if (yearRange != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.textPrimaryColor
                    )
                }
                OutlinedButton(
                    modifier = Modifier
                        .height(36.dp),
                    onClick = { showBottomSheet = "type" },
                    border = BorderStroke(1.dp, if (selectedType != null) cs.primary else MaterialTheme.colorScheme.G_100),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (selectedType != null) cs.primary else MaterialTheme.colorScheme.textPrimaryColor
                    )
                ) {
                    Text(
                        selectedType ?: "차종",
                        color = if (selectedType != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.textPrimaryColor
                    )
                }
                OutlinedButton(
                    modifier = Modifier
                        .height(36.dp),
                    onClick = { showBottomSheet = "distance" },
                    border = BorderStroke(1.dp, if (distanceRange != null) cs.primary else MaterialTheme.colorScheme.G_100),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (distanceRange != null) cs.primary else MaterialTheme.colorScheme.textPrimaryColor
                    )
                ) {
                    Text(
                        if (distanceRange == null) "주행거리"
                        else "${String.format("%,d", distanceRange.start.toInt())}km ~ ${String.format("%,d", distanceRange.endInclusive.toInt())}km",
                        color = if (distanceRange != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.textPrimaryColor
                    )
                }
                OutlinedButton(
                    modifier = Modifier
                        .height(36.dp),
                    onClick = { showBottomSheet = "price" },
                    border = BorderStroke(1.dp, if (priceRange != null) cs.primary else MaterialTheme.colorScheme.G_100),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (priceRange != null) cs.primary else MaterialTheme.colorScheme.textPrimaryColor
                    )
                ) {
                    Text(
                        if (priceRange == null) "가격"
                        else "${priceRange.start.toInt() * 100}만원 ~ ${priceRange.endInclusive.toInt() * 100}만원",
                        color = if (priceRange != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.textPrimaryColor
                    )
                }
            }

            if (isLoading && cars.isNotEmpty()) {   // ✅ 목록 있을 때만 로딩 오버레이
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {

            // 차량 리스트
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (cars.isEmpty()) {
                    item {
                        // 빈 화면: 아무것도 안 보이게 하려면 이 부분을 비워두세요
                        Spacer(modifier = Modifier.height(1.dp))
                        // 또는 아래처럼 메시지 추가 가능
                        // Box(
                        //     modifier = Modifier.fillMaxSize(),
                        //     contentAlignment = Alignment.Center
                        // ) {
                        //     Text("검색 결과가 없습니다.")
                        // }
                    }
                }
                 else {itemsIndexed(cars) { index, car ->

                    when (car) {
                        is SearchCarItem.Auction -> ListingItem(
                            car = car.toAuctionCar(),
                            onClick = { onCarClick(car) }, // SearchCarItem 그대로 넘김
                            onToggleLike = { onToggleLike(car.toAuctionCar()) },
                            tags = car.mainOptions,
                            priceLabel = "최고 입찰가",
                            showBadge = true,
                            showAuctionMeta = true
                        )
                        is SearchCarItem.General -> ListingItem(
                            car = car.toAuctionCarForDisplay(),
                            onClick = { onCarClick(car) }, // SearchCarItem 그대로 넘김
                            onToggleLike = { onToggleLike(car.toAuctionCarForDisplay()) },
                            tags = car.mainOptions,
                            priceLabel = "",
                            showBadge = false,
                            showAuctionMeta = false
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                }}

                if (viewModel.hasMorePages && cars.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                            LaunchedEffect(Unit) {
                                viewModel.loadNextPage()
                            }
                        }
                    }
                }
            }}
            when (showBottomSheet) {
                "price" -> RangeSelectBottomSheet(
                    title = "가격을 선택해 주세요",
                    unit = "만원",
                    valueRange = 0f..300f,
                    steps = 31,
                    initialRange = priceRange ?: (0f..300f),
                    onDismiss = { showBottomSheet = null },
                    onConfirm = {
                        viewModel.priceRange.value = it
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
                "type" -> CarTypeSelectBottomSheet(
                    selectedType = selectedType,
                    onDismiss = { showBottomSheet = null },
                    onConfirm = {
                        viewModel.selectedType.value = it
                        showBottomSheet = null
                    }
                )
            }
        }
    }
}
