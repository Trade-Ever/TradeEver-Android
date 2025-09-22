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
    viewModel: MyPageViewModel = koinViewModel()
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("최근", "찜")

    val recentlyViewedCars by viewModel.recentlyViewedCars.collectAsState()

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
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(text = title) }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> {
                    if (recentlyViewedCars.isEmpty()) {
                        EmptyState(message = "최근에 본 차량이 없습니다.")
                    } else {
                        CarList(cars = recentlyViewedCars, navController = navController)
                    }
                }
                1 -> {
                    // 찜한 차량은 아직 AuctionCar를 사용하므로, 추후 RecentlyViewedCar와 유사한 모델로 교체 필요
                    EmptyState(message = "찜한 내역이 없습니다.")
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
            // RecentlyViewedCar를 ListingItem이 요구하는 AuctionCar로 변환
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
                endsAtMillis = 0L,
                liked = false,
                auctionId = if(car.isAuction) car.id.toLongOrNull() else null, // 임시 처리
                transactionType = if(car.isAuction) "경매" else "일반"
            )

            ListingItem(
                car = auctionCar,
                onClick = {
                    // TODO: 상세 화면으로 이동하는 네비게이션 로직 구현
                    // navController.navigate("vehicleDetail/${car.id}")
                },
                onToggleLike = { /* TODO: 찜하기 로직 */ },
                showBadge = car.isAuction,
                showAuctionMeta = car.isAuction,
                priceLabel = if (car.isAuction) "최고 입찰가" else "판매 가격"
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

//@Preview(showBackground = true)
//@Composable
//fun RecentlyViewedCarsScreenPreview() {
//    AppTheme {
//        // Preview에서는 ViewModel을 직접 주입할 수 없으므로, 로직이 없는 상태로 렌더링됩니다.
//        RecentlyViewedCarsScreen(navController = rememberNavController())
//    }
//}
