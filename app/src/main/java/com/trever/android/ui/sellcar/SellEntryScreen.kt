package com.trever.android.ui.sellcar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // items 임포트 확인
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
// import androidx.compose.runtime.remember // 사용하지 않으면 제거 가능
// import androidx.compose.runtime.setValue // 사용하지 않으면 제거 가능
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
// import androidx.compose.ui.tooling.preview.Preview // 프리뷰 관련 코드가 없다면 제거 가능
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.trever.android.R
// import com.trever.android.data.remote.toAuctionCar // ViewModel에서 처리하므로 여기선 불필요
import com.trever.android.domain.model.AuctionCar // AuctionCar 직접 사용
import com.trever.android.ui.components.ListingItem
import com.trever.android.ui.navigation.ROUTE_SELL_FLOW
import com.trever.android.ui.sellcar.viewmodel.SellEntryViewModel
// import com.trever.android.ui.theme.AppTheme // 프리뷰 관련 코드가 없다면 제거 가능
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
    // 화면이 나타날 때마다 내가 등록한 차량 목록을 새로고침하는 LaunchedEffect는 ViewModel의 init 블록으로 이동 또는 유지 가능
    // ViewModel에서 init 시점에 fetch 및 리스너를 설정하므로 여기서는 필수는 아님
    // 다만, 화면이 다시 보여질 때마다 강제 새로고침을 원한다면 유지할 수 있음
//    LaunchedEffect(key1 = Unit) { // key1 = true에서 Unit으로 변경하여 화면 진입 시 마다 실행
//        sellEntryViewModel.fetchMyVehicles()
//    }

    val uiState by sellEntryViewModel.uiState.collectAsState()
    // uiState.myVehicles가 이미 List<AuctionCar>이므로 추가 매핑 필요 없음
    val registeredCars = uiState.myVehicles
    val isRefreshing = uiState.isLoading

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        // onRefresh 콜백에서 isRefresh = true 로 설정
        onRefresh = { sellEntryViewModel.fetchMyVehicles(isRefresh = true) }
    )

    Scaffold(
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
                .pullRefresh(pullRefreshState)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // 1. 상단 UI (변경 없음)
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.purple_car_80),
                            contentDescription = "차량 등록 배경",
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Fit
                        )
                        Button(
                            onClick = { parentNavController.navigate(ROUTE_SELL_FLOW) },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .offset(y = (-40).dp)
                                .border(
                                    width = 4.dp,
                                    color = Color.Gray,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black,
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Text(
                                text = "차량 등록하기",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                // 2. "내가 등록한 차량" 타이틀 또는 상태 메시지
                if (!isRefreshing || registeredCars.isNotEmpty()) { // 로딩 중이 아닐 때 또는 차가 있을 때
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
                            // 로딩이 끝났고, 차량이 없을 때 메시지 표시
                            registeredCars.isEmpty() && !isRefreshing -> {
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
                            registeredCars.isNotEmpty() -> {
                                Text(
                                    text = "내가 등록한 차량",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 16.dp)
                                )
                            }
                        }
                    }
                }


                // 3. 등록된 차량 목록 (registeredCars를 직접 사용)
                if (registeredCars.isNotEmpty()) {
                    items(registeredCars, key = { it.id }) { car -> // car는 이제 AuctionCar 타입
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            val isRealAuction = car.auctionId != null && car.auctionId != 0L
                            ListingItem(
                                car = car, // AuctionCar 타입이므로 그대로 전달
                                onClick = {
                                    if (isRealAuction) {
                                        parentNavController.navigate("auction/detail/${car.id}/${car.auctionId}")
                                    } else {
                                        parentNavController.navigate("buy/detail/${car.id}")
                                    }
                                },
                                onToggleLike = { /* SellEntryScreen에서는 찜하기 기능이 필요 없을 수 있음 */ },
                                tags = car.mainOptions ?: emptyList(),
                                showBadge = isRealAuction,
                                showAuctionMeta = isRealAuction,
                                priceLabel = if (isRealAuction) "최고 입찰가" else "판매 가격"
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }

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
