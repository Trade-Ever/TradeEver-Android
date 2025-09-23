package com.trever.android.ui.sellcar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.trever.android.R
import com.trever.android.data.remote.toAuctionCar
import com.trever.android.domain.model.AuctionCar
import com.trever.android.ui.components.ListingItem
import com.trever.android.ui.navigation.ROUTE_SELL_FLOW
import com.trever.android.ui.sellcar.viewmodel.SellEntryViewModel
import com.trever.android.ui.theme.AppTheme
import com.trever.android.ui.theme.Red_1
import org.koin.androidx.compose.koinViewModel

@Composable
fun AuctionBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Red_1.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text("경매", color = Red_1, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun SellEntryScreen(
    parentNavController: NavHostController,
    sellEntryViewModel: SellEntryViewModel = koinViewModel()
) {
    // 화면이 나타날 때마다 내가 등록한 차량 목록을 새로고침합니다.
    LaunchedEffect(key1 = true) {
        sellEntryViewModel.fetchMyVehicles()
    }

    val uiState by sellEntryViewModel.uiState.collectAsState()
    val registeredCars = uiState.myVehicles.map { it.toAuctionCar() }
    val isRefreshing = uiState.isLoading

    // "당겨서 새로고침" 상태와 동작을 정의합니다.
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { sellEntryViewModel.fetchMyVehicles() }
    )

    Scaffold(
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(pullRefreshState) // Box에 pullRefresh Modifier 적용
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // 1. 상단 UI: 차량 이미지 및 등록 버튼
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp), // 이미지 컨테이너 높이
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.purple_car_44),
                            contentDescription = "차량 등록 배경",
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Fit
                        )
                        Button(
                            onClick = { parentNavController.navigate(ROUTE_SELL_FLOW) },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .offset(y = (-50).dp),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Text(
                                text = "차량 등록하기",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // 2. "내가 등록한 차량" 타이틀 또는 상태 메시지 (로딩 중일 때는 표시하지 않음)
                if (!isRefreshing || registeredCars.isNotEmpty()) {
                    item {
                        when {
                            uiState.error != null -> {
                                Text(
                                    text = uiState.error ?: "알 수 없는 오류가 발생했습니다.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    color = Color.Red,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 48.dp)
                                )
                            }
                            registeredCars.isEmpty() -> {
                                Text(
                                    text = "아직 등록된 차량이 없어요. 지금 바로 내 차 정보를 등록해보세요!",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    color = Color.Gray,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 48.dp)
                                )
                            }
                            else -> {
                                Text(
                                    text = "내가 등록한 차량",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 16.dp)
                                )
                            }
                        }
                    }
                }


                // 3. 등록된 차량 목록
                if (registeredCars.isNotEmpty()) {
                    items(registeredCars, key = { it.id }) { car ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            val isAuction = car.transactionType == "경매"
                            ListingItem(
                                car = car,
                                onClick = { /* TODO: 등록된 차량 상세 화면으로 이동 */ },
                                onToggleLike = { /* TODO: 찜하기 로직 */ },
                                showBadge = isAuction,
                                showAuctionMeta = isAuction,
                                priceLabel = if (isAuction) "최고 입찰가" else "판매 가격"
                            )
                        }
                    }
                }

                // 바텀 네비게이션과 겹치지 않도록 충분한 하단 공간 확보
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }

            // 새로고침 인디케이터 (화면 상단 중앙에 표시)
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = Color.White,
                contentColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}