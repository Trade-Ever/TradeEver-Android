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
import com.trever.android.domain.model.AuctionCar // RecentlyViewedCar 임포트 제거 가능
import com.trever.android.ui.components.ListingItem
import com.trever.android.ui.myPage.MyPageViewModel
import com.trever.android.ui.theme.AppTheme
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentlyViewedCarsScreen(
    navController: NavController,
    viewModel: MyPageViewModel = koinViewModel(),
    initialTabIndex: Int = 0
) {
    var selectedTabIndex by remember { mutableStateOf(initialTabIndex) }
    val tabs = listOf("최근", "찜")

    // viewModel.recentlyViewedCars는 이제 StateFlow<List<AuctionCar>>를 직접 제공
    val recentlyViewedCars by viewModel.recentlyViewedCars.collectAsState()
    val likedCars by viewModel.likedCars.collectAsState() // 이것도 List<AuctionCar>

    // 메시지 상태 관찰 (오류 메시지 등 표시용)
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            // 메시지를 보여준 후 ViewModel에서 메시지를 초기화하는 로직이 있다면 호출
            // viewModel.clearMessage() // 예시
        }
    }

    // 화면이 나타나거나 탭이 변경될 때 데이터를 로드
    LaunchedEffect(selectedTabIndex) {
        when (selectedTabIndex) {
            0 -> viewModel.loadRecentlyViewedCars()
            1 -> viewModel.loadLikedCars()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }, // 스낵바 추가
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
        containerColor = Color(0xFFF0F0F0) // 배경색 약간 어둡게 유지
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
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black
                            )
                        }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> { // 최근 본 차량
                    if (recentlyViewedCars.isEmpty()) {
                        EmptyState(message = "최근에 본 차량이 없습니다.")
                    } else {
                        // CarList의 파라미터 타입이 List<AuctionCar>로 변경됨
                        CarList(cars = recentlyViewedCars, navController = navController)
                    }
                }
                1 -> { // 찜한 차량
                    if (likedCars.isEmpty()) {
                        EmptyState(message = "찜한 내역이 없습니다.")
                    } else {
                        // LikedCarList는 이미 List<AuctionCar>를 사용하고 있었음
                        LikedCarList(cars = likedCars, navController = navController)
                    }
                }
            }
        }
    }
}

@Composable
private fun CarList(cars: List<AuctionCar>, navController: NavController) { // 파라미터 타입 변경
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(cars, key = { it.id }) { car -> // 이제 car는 AuctionCar 타입
            // AuctionCar 객체를 직접 사용하므로 별도의 변환 로직 불필요
            val isAuctionDisplay = car.auctionId != null && car.auctionId != 0L

            ListingItem(
                car = car, // AuctionCar 객체를 직접 전달
                onClick = {
                    if (isAuctionDisplay) {
                        navController.navigate("auction/detail/${car.id}/${car.auctionId}")
                    } else {
                        navController.navigate("buy/detail/${car.id}")
                    }
                },
                onToggleLike = { /* TODO: 찜하기 로직 (ViewModel과 연동 필요) */ },
                tags = car.mainOptions ?: emptyList(), // AuctionCar의 mainOptions 사용
                showBadge = isAuctionDisplay,
                showAuctionMeta = isAuctionDisplay, // AuctionCar의 경매 정보 사용
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
        items(cars, key = { it.id }) { car -> // car는 이미 AuctionCar 타입
            val isAuctionDisplay = car.auctionId != null && car.auctionId != 0L
            ListingItem(
                car = car,
                onClick = {
                    if (isAuctionDisplay) {
                        navController.navigate("auction/detail/${car.id}/${car.auctionId}")
                    } else {
                        navController.navigate("buy/detail/${car.id}")
                    }
                },
                onToggleLike = { /* TODO: 찜하기 로직 (ViewModel과 연동 필요) */ },
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
        // Preview에서는 ViewModel을 직접 생성하거나 Mock 데이터를 사용해야 합니다.
        // koinViewModel()은 실제 앱 실행 시에만 작동합니다.
        // val mockNavController = rememberNavController()
        // val mockViewModel = MyPageViewModel(...) // Mock Repository들 필요
        RecentlyViewedCarsScreen(navController = rememberNavController(), initialTabIndex = 0)
    }
}
