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
                title = { Text("나의 활동", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로 가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF0F0F0)
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
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
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black
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
        items(cars, key = { it.id }) { car ->
            val auctionCar = AuctionCar(
                id = car.id,
                title = car.title,
                year = car.year,
                mileageKm = car.mileageKm,
                imageUrl = car.imageUrl,
                currentPriceWon = car.priceWon,
                manufacturer = car.manufacturer,
                model = car.model,
                tags = emptyList(),
                mainOptions = emptyList(),
                startAtMillis = 0L, // 임시값 추가
                endsAtMillis = 0L,
                liked = false,
                auctionId = if(car.isAuction) car.id.toLongOrNull() else null,
                transactionType = if(car.isAuction) "경매" else "일반"
            )

            ListingItem(
                car = auctionCar,
                onClick = { /* TODO: 상세 화면 이동 */ },
                onToggleLike = { /* TODO: 찜하기 로직 */ },
                showBadge = car.isAuction,
                showAuctionMeta = car.isAuction,
                priceLabel = if (car.isAuction) "최고 입찰가" else "판매 가격"
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
        items(cars, key = { it.id }) { car ->
            ListingItem(
                car = car,
                onClick = { /* TODO: 상세 화면 이동 */ },
                onToggleLike = { /* TODO: 찜하기 로직 */ },
                showBadge = car.transactionType == "경매",
                showAuctionMeta = car.transactionType == "경매",
                priceLabel = if (car.transactionType == "경매") "최고 입찰가" else "판매 가격"
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
            color = Color.Gray,
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
