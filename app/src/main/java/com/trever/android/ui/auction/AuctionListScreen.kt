package com.trever.android.ui.auction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trever.android.domain.model.AuctionCar
import java.util.concurrent.TimeUnit
import com.trever.android.ui.components.ListingItem
import com.trever.android.ui.theme.backgroundColor
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


//@Composable
//fun AuctionListScreen(
//    items: List<AuctionCar> = sampleAuctions(),
//    onItemClick: (String) -> Unit = {},
//    onToggleLike: (String) -> Unit = {}
//) {
//    val cs = MaterialTheme.colorScheme
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .background(cs.backgroundColor)
//    ) {
//        Text(
//            text = "경매",
//            style = MaterialTheme.typography.headlineMedium,
//            color = cs.onBackground,
//            fontSize = 20.sp,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(start = 16.dp, top = 10.dp, bottom = 4.dp)
//        )
//
//        LazyColumn(
//            modifier = Modifier.fillMaxWidth(),
//            contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
//            verticalArrangement = Arrangement.spacedBy(8.dp),
//        ) {
//            items(items, key = { it.id }) { car ->
//                ListingItem(
//                    car = car,
//                    onClick = { onItemClick(car.id) },
//                    onToggleLike = { onToggleLike(car.id) },
//                    tags = listOf("비흡연자", "무사고", "정비완료"),
//                    priceLabel = "최고 입찰가",
//                    showBadge = true,
//                    showAuctionMeta = true
//                )
//            }
//        }
//    }
//}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AuctionListScreen(
    viewModel: AuctionListViewModel = viewModel(),
    onItemClick: (String, String) -> Unit = { _, _ -> },
    onToggleLike: (String) -> Unit = {}
) {
    val cs = MaterialTheme.colorScheme
    val uiState by viewModel.uiState.collectAsState()



    // 당겨서 새로고침 상태
    var refreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = {
            refreshing = true
            // 일반 loadAuctions 대신 Firebase 데이터를 포함하여 로드
            viewModel.loadAuctionsWithFirebaseData()
        }
    )



    // 새로고침 완료 감지
    LaunchedEffect(uiState) {
        if (refreshing && uiState !is AuctionListUiState.Loading) {
            refreshing = false
        }
    }

    // Firebase 리스너 설정
    LaunchedEffect(key1 = Unit) {
        viewModel.setupFirebaseListener()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(cs.backgroundColor)
    ) {
        Text(
            text = "경매",
            style = MaterialTheme.typography.headlineMedium,
            color = cs.onBackground,
            fontSize = 20.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 10.dp, bottom = 4.dp)
        )

        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = uiState) {
                is AuctionListUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is AuctionListUiState.Success -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pullRefresh(pullRefreshState)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(state.auctions, key = { it.id }) { car ->
                                ListingItem(
                                    car = car,
                                    onClick = { onItemClick(car.id, car.auctionId.toString()) },
                                    onToggleLike = { viewModel.toggleLike(car.id) },
                                    tags = car.mainOptions,
                                    priceLabel = "최고 입찰가",
                                    showBadge = true,
                                    showAuctionMeta = true
                                )
                            }

                            if (state.hasMorePages) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(30.dp))

                                        LaunchedEffect(key1 = true) {
                                            viewModel.loadNextPage()
                                        }
                                    }
                                }
                            }
                        }

                        // 당겨서 새로고침 인디케이터
                        PullRefreshIndicator(
                            refreshing = refreshing,
                            state = pullRefreshState,
                            modifier = Modifier.align(Alignment.TopCenter),
                            backgroundColor = cs.surfaceVariant.copy(alpha = 0.9f),
                            contentColor = cs.primary
                        )
                    }
                }
                is AuctionListUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(state.message)
                    }
                }
            }
        }
    }
}
