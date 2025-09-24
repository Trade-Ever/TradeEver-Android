package com.trever.android.ui.buy

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsProperties.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.trever.android.domain.model.AuctionCar
import com.trever.android.ui.components.ListingItem
import com.trever.android.ui.theme.backgroundColor
import java.util.concurrent.TimeUnit
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trever.android.R
import com.trever.android.data.remote.toAuctionCarForDisplay

import com.trever.android.ui.theme.G_200
import com.trever.android.ui.theme.G_300


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BuyListScreen(
    viewModel: BuyListViewModel = viewModel(),
    onItemClick: (String) -> Unit = {},
    onToggleLike: (String) -> Unit = {},
    onSearchClick: () -> Unit = {}     // ← 검색 화면으로 이동 콜백
) {
    val cs = MaterialTheme.colorScheme
    val uiState by viewModel.uiState.collectAsState()

    // 당겨서 새로고침 상태
    var refreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = {
            refreshing = true
            viewModel.loadVehicles()
        }
    )

    // 새로고침 완료 감지
    LaunchedEffect(uiState) {
        if (refreshing && uiState !is BuyListUiState.Loading) {
            refreshing = false
        }
    }

    // 검색 버튼 실제 높이를 리스트 패딩에 반영
    var searchBarH by remember { mutableStateOf(0) }
    val searchBarHdp = with(LocalDensity.current) { searchBarH.toDp() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.backgroundColor)
    ) {
        // 목록 부분
        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = uiState) {
                is BuyListUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is BuyListUiState.Success -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pullRefresh(pullRefreshState)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 10.dp,
                                end = 10.dp,
                                top = searchBarHdp + 30.dp,
                                bottom = 10.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            items(state.vehicles, key = { it.id }) { vehicle ->
                                ListingItem(
                                    car = vehicle.toAuctionCarForDisplay(),
                                    onClick = { onItemClick(vehicle.id.toString()) },
                                    onToggleLike = { onToggleLike(vehicle.id.toString()) },
                                    tags = vehicle.mainOptions ?: emptyList(),
                                    showBadge = false,
                                    showAuctionMeta = false,
                                    priceLabel = ""
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

                is BuyListUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(state.message)
                    }
                }
            }
        }

        // 떠있는 "검색으로 이동" 버튼 (가짜 검색바)
        FloatingSearchButton(
            text = "원하는 차량을 검색해보세요",
            onClick = onSearchClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .zIndex(1f)
                .onSizeChanged { searchBarH = it.height }
                .statusBarsPadding()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FloatingSearchButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val pillBg = if (isSystemInDarkTheme()) Color(0x33FFFFFF) else Color(0xFFF3F8FF)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(999.dp))
            .clickable(onClick = onClick),    // ← 클릭 시 검색 화면으로 이동
        color = pillBg,
        tonalElevation = 0.dp,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.search),
                contentDescription = null,
                tint = cs.onSurfaceVariant
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = text,
                color = cs.G_300,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
