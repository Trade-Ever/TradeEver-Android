package com.trever.android.ui.auction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.trever.android.domain.model.AuctionCar
import java.util.concurrent.TimeUnit
import com.trever.android.ui.components.ListingItem
import com.trever.android.ui.theme.backgroundColor


@Composable
fun AuctionListScreen(
    items: List<AuctionCar> = sampleAuctions(),
    onItemClick: (String) -> Unit = {},
    onToggleLike: (String) -> Unit = {}
) {
    val cs = MaterialTheme.colorScheme
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(cs.backgroundColor),
        contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 0.dp, bottom = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items, key = { it.id }) { car ->
            ListingItem(
                car = car,
                onClick = { onItemClick(car.id) },
                onToggleLike = { onToggleLike(car.id) },
                tags = listOf("비흡연자", "무사고", "정비완료"),
                priceLabel = "최고 입찰가",     // 기본값이라 생략 가능
                showBadge = true,
                showAuctionMeta = true
            )
        }
    }
}


@Composable
private fun sampleAuctions(): List<AuctionCar> {
    val now = System.currentTimeMillis()
    val demoImage = "https://www.genesis.com/content/dam/genesis-p2/kr/bto/jx/a/jx_uyh_a.png.thumb.1280.720.png"
    return listOf(
        AuctionCar("1", "Taycan GTS", 2024, 3850, demoImage, emptyList(), 141_900_000, now + TimeUnit.HOURS.toMillis(26),true),
        AuctionCar("2", "Taycan GTS", 2024, 3858, demoImage, emptyList(), 30_000_000, now + TimeUnit.HOURS.toMillis(5) + TimeUnit.MINUTES.toMillis(15)),
        AuctionCar("3", "Taycan GTS", 2024, 3858, demoImage, emptyList(), 141_900_000, now + TimeUnit.MINUTES.toMillis(45))
    )
}