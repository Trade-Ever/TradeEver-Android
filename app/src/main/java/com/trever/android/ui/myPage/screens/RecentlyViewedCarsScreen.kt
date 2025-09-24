package com.trever.android.ui.myPage.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.trever.android.domain.model.AuctionCar
import com.trever.android.domain.model.RecentlyViewedCar
import com.trever.android.ui.components.ListingItem
import com.trever.android.ui.myPage.MyPageViewModel
import com.trever.android.ui.theme.AppTheme
import com.trever.android.ui.theme.backgroundColor
import com.trever.android.ui.theme.cardBackgroundColor
import com.trever.android.ui.theme.textPrimaryColor
import com.trever.android.ui.theme.textSecondaryColor
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentlyViewedCarsScreen(
    navController: NavController,
    viewModel: MyPageViewModel = koinViewModel(),
    initialTabIndex: Int = 0 // <-- 탭 인덱스를 외부에서 받을 수 있도록 파라미터 추가
) {
    // remember 상태를 initialTabIndex로 초기화
    var selectedTabIndex by remember { mutableStateOf(initialTabIndex) }
    val tabs = listOf("최근", "찜")

    val recentlyViewedCars by viewModel.recentlyViewedCars.collectAsState()
    val likedCars by viewModel.likedCars.collectAsState()

    // 화면이 나타나거나 탭이 변경될 때 데이터를 로드
    LaunchedEffect(selectedTabIndex) {
        when (selectedTabIndex) {
            0 -> viewModel.loadRecentlyViewedCars()
            1 -> viewModel.loadLikedCars()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("나의 활동", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.
                textPrimaryColor) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로 가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.backgroundColor)
            )
        },
        containerColor = MaterialTheme.colorScheme.backgroundColor
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.backgroundColor,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTabIndex == index
                    Tab(
                        selected = isSelected,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                // isSelected 값에 따라 텍스트 색상을 동적으로 변경
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.textPrimaryColor
                            )
                        }
                    )
                }
            }

            // 선택된 탭에 따라 다른 컨텐츠 표시
            when (selectedTabIndex) {
                0 -> {
                    if (recentlyViewedCars.isEmpty()) {
                        EmptyState(message = "최근에 본 차량이 없습니다.")
                    } else {
                        CarList(cars = recentlyViewedCars, navController = navController)
                    }
                }
                1 -> {
                    if (likedCars.isEmpty()) {
                        EmptyState(message = "찜한 내역이 없습니다.")
                    } else {
                        LikedCarList(cars = likedCars, navController = navController)
                    }
                }
            }
        }
    }
}

@Composable
private fun CarList(cars: List<RecentlyViewedCar>, navController: NavController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(cars, key = { it.id }) { car -> // car is RecentlyViewedCar
            val effectiveTitle = if (!car.manufacturer.isNullOrBlank() && !car.model.isNullOrBlank()) {
                "${car.manufacturer} ${car.model}"
            } else {
                car.title // car.title is from DTO's carName
            }

            val auctionCar = AuctionCar(
                id = car.id,
                title = effectiveTitle,
                year = car.year,
                mileageKm = car.mileageKm,
                imageUrl = car.imageUrl,
                currentPriceWon = car.priceWon,
                manufacturer = car.manufacturer,
                model = car.model,
                tags = emptyList(), // This is List<Tag> (enum). Pass emptyList() as we don't have this info from RecentlyViewedCar.
                mainOptions = car.mainOptions ?: emptyList(), // This is List<String> and is correctly used by ListingItem.
                startAtMillis = 0L, // Placeholder, as RecentlyViewedCar doesn't have auction times
                endsAtMillis = 0L,  // Placeholder
                liked = car.isFavorite ?: false, // Uses isFavorite from RecentlyViewedCar
                auctionId = if(car.isAuction) car.id.toLongOrNull() else null,
                transactionType = if(car.isAuction) "경매" else "일반"
            )
            
            val isAuctionDisplay = auctionCar.auctionId != null

            ListingItem(
                car = auctionCar,
                onClick = { // auctionCar를 사용하여 네비게이션
                    if (auctionCar.auctionId != null) {
                        // 경매 매물일 경우: auction/detail/{carId}/{auctionId}로 이동
                        navController.navigate("auction/detail/${auctionCar.id}/${auctionCar.auctionId}")
                    } else {
                        // 일반 매물일 경우: buy/detail/{carId}로 이동
                        navController.navigate("buy/detail/${auctionCar.id}")
                    }
                },
                onToggleLike = { /* TODO: 찜하기 로직 */ },
                tags = auctionCar.mainOptions ?: emptyList(),
                showBadge = isAuctionDisplay,
                showAuctionMeta = isAuctionDisplay,
                priceLabel = if (isAuctionDisplay) "최고 입찰가" else "판매 가격"
            )
        }
    }
}

@Composable
private fun LikedCarList(cars: List<AuctionCar>, navController: NavController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(cars, key = { it.id }) { car -> // car is AuctionCar
            val isAuctionDisplay = car.auctionId != null
            ListingItem(
                car = car,
                onClick = {
                    if (car.auctionId != null) {
                        // 경매 매물일 경우: auction/detail/{carId}/{auctionId}로 이동
                        navController.navigate("auction/detail/${car.id}/${car.auctionId}")
                    } else {
                        // 일반 매물일 경우: buy/detail/{carId}로 이동
                        navController.navigate("buy/detail/${car.id}")
                    }
                },
                onToggleLike = { /* TODO: 찜하기 로직 */ },
                tags = car.mainOptions ?: emptyList(),
                showBadge = isAuctionDisplay,
                showAuctionMeta = isAuctionDisplay,
                priceLabel = if (isAuctionDisplay) "최고 입찰가" else "판매 가격"
            )
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.textSecondaryColor,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RecentlyViewedCarsScreenPreview() {
    AppTheme {
        RecentlyViewedCarsScreen(navController = rememberNavController(), initialTabIndex = 1)
    }
}
