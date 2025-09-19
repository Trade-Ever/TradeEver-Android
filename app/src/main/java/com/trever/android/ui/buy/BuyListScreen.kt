package com.trever.android.ui.buy

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.trever.android.R
import com.trever.android.ui.theme.G_200
import com.trever.android.ui.theme.G_300


@Composable
fun BuyListScreen(
    items: List<AuctionCar> = sampleAuctions(),
    onItemClick: (String) -> Unit = {},
    onToggleLike: (String) -> Unit = {},
    onSearchClick: () -> Unit = {}     // ← 검색 화면으로 이동 콜백
) {
    val cs = MaterialTheme.colorScheme

    // 검색 버튼 실제 높이를 리스트 패딩에 반영
    var searchBarH by remember { mutableStateOf(0) }
    val searchBarHdp = with(LocalDensity.current) { searchBarH.toDp() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.backgroundColor)
    ) {
        // 목록
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 10.dp,
                end = 10.dp,
                top = searchBarHdp + 30.dp,  // 떠있는 검색 버튼 공간만큼 띄우기
                bottom = 10.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {

            items(items, key = { it.id }) { car ->
                ListingItem(
                    car = car,
                    onClick = { onItemClick(car.id) },
                    onToggleLike = { onToggleLike(car.id) },
                    tags = listOf("비흡연자", "무사고", "정비완료"),
                    showBadge = false,
                    showAuctionMeta = false,
                    priceLabel = ""  // 레이블 없이 가격만 표시
                )
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
@Composable
private fun sampleAuctions(): List<AuctionCar> {
    val now = System.currentTimeMillis()
    val demoImage = "https://dimg.donga.com/wps/EVLOUNGE/IMAGE/2024/02/06/123417778.1.jpg"
    return listOf(
        AuctionCar("1", "Taycan GTS", 2024, 3850, demoImage, emptyList(),listOf("내비게이션", "어라운드뷰"),  141_900_000, now + TimeUnit.HOURS.toMillis(26),true),
        AuctionCar("2", "Taycan GTS", 2024, 3858, demoImage, emptyList(),listOf("내비게이션", "어라운드뷰"),  30_000_000, now + TimeUnit.HOURS.toMillis(5) + TimeUnit.MINUTES.toMillis(15)),
        AuctionCar("3", "Taycan GTS", 2024, 3858, demoImage, emptyList(),listOf("내비게이션", "어라운드뷰"),  141_900_000, now + TimeUnit.MINUTES.toMillis(45))
    )
}