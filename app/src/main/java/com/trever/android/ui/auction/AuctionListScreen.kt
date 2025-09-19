package com.trever.android.ui.auction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trever.android.domain.model.AuctionCar
import java.util.concurrent.TimeUnit
import com.trever.android.ui.components.ListingItem
import com.trever.android.ui.theme.backgroundColor


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

@Composable
fun AuctionListScreen(
    viewModel: AuctionListViewModel = viewModel(),
    onItemClick: (String) -> Unit = {},
    onToggleLike: (String) -> Unit = {}
) {
    val cs = MaterialTheme.colorScheme
    val uiState by viewModel.uiState.collectAsState()

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
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.auctions, key = { it.id }) { car ->
                        ListingItem(
                            car = car,
                            onClick = { onItemClick(car.id) },
                            onToggleLike = { onToggleLike(car.id) },
                            tags = car.mainOptions,
                            priceLabel = "최고 입찰가",
                            showBadge = true,
                            showAuctionMeta = true
                        )
                    }
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

