package com.trever.android.ui.auction

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.trever.android.domain.model.AuctionCar
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.trever.android.R
import com.trever.android.ui.theme.Grey_100
import com.trever.android.ui.theme.Grey_200
import com.trever.android.ui.theme.Grey_400
import com.trever.android.ui.theme.Red_1
import com.trever.android.ui.theme.Red_2
import com.trever.android.ui.theme.backgroundColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuctionListScreen(
    items: List<AuctionCar> = sampleAuctions(),      // 서버 전 붙이는 더미
    onItemClick: (String) -> Unit = {},
    onToggleLike: (String) -> Unit = {}
) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.background(cs.backgroundColor)) {



        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .background(cs.backgroundColor),
            contentPadding = PaddingValues(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(items, key = { it.id }) { car ->
                AuctionItem(
                    car = car,
                    onClick = { onItemClick(car.id) }
                )
            }
        }
    }
}

@Composable
fun TagChip(text: String) {
    Text(
        text = text,
        color = Grey_400,
        style = MaterialTheme.typography.bodySmall.copy(
            fontWeight = FontWeight.Bold,  // 글씨 굵게
                    fontSize = 10.sp
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Grey_100)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    )
}

private fun formatKoreanWon(amount: Long): String {
    val 억 = amount / 100_000_000
    val 만 = (amount % 100_000_000) / 10_000

    return buildString {
        if (억 > 0) append("${억}억 ")
        if (만 > 0) append("${만}만원")
        if (억 == 0L && 만 == 0L) append("0원")
    }.trim()
}


@Composable
private fun TagsWithPrice(
    tags: List<String>,
    priceText: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 태그 3개까지
        tags.take(3).forEachIndexed { i, tag ->
            if (i > 0) Spacer(Modifier.width(8.dp))
            TagChip(tag)
        }

        Spacer(Modifier.weight(1f)) // 오른쪽으로 가격 밀기

        // "최고 입찰가 " + 금액 분리
        val label = "최고 입찰가"
        val amount = priceText.removePrefix(label)

        Text(
            buildAnnotatedString {
                withStyle(
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = Color.Black
                    ).toSpanStyle()
                ) {
                    append(label)
                }
                withStyle(
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00C364) // 초록색
                    ).toSpanStyle()
                ) {
                    append(amount)
                }
            }
        )
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FeatureChips(tags: List<String>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tags.forEach { tag ->
            TagChip(text = tag)
        }
    }
}

@Composable
private fun AuctionItem(
    car: AuctionCar,
    onClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val tags = remember(car.id) {
        when (car.id) {
            "1" -> listOf("무사고", "비흡연차", "1인소유")
            "2" -> listOf("정비이력 양호", "사고이력 無")
            else -> listOf("급매", "실주행", "단순교환")
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cs.backgroundColor)
    ) {
        Column(Modifier.padding(12.dp)) {
            // ---- 이미지 ----
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                if (car.imageUrl.isNullOrBlank()) {
                    Box(
                        Modifier
                            .matchParentSize()
                            .background(cs.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) { Text("이미지", color = cs.onSurfaceVariant) }
                } else {
                    AsyncImage(
                        model = car.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Text(
                    text = "경매",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                        .background(
                            color = Red_2, // 빨간색
                            shape = RoundedCornerShape(6.dp) // 둥근 모서리
                        )
                        .padding(horizontal = 14.dp, vertical = 0.dp) // 내부 패딩
                )
                IconButton(
                    onClick = {  },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(36.dp) // 터치 영역 확보
                ) {
                    if (car.liked) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "찜 해제",
                            tint = Red_1
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder, // 빈 하트
                            contentDescription = "찜하기",
                            tint = Color.Black                         // 🖤 검정색
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = car.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f), // 남은 공간 차지
                    fontSize = 18.sp
                )

                // 오른쪽 아이콘 + 남은시간
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.gavel_1),
                        contentDescription = "경매",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    CountdownText(car.endsAtMillis)
                }
            }

            Text(
                text = "${car.year}년 · ${formatKm(car.mileageKm)}km",
                style = MaterialTheme.typography.bodySmall,
                color = cs.onSurfaceVariant,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(4.dp))



            Spacer(Modifier.height(6.dp))

            // ---- 태그 + 현재가 ----
            TagsWithPrice(
                tags = listOf("비흡연자", "무사고", "정비완료"),
                priceText = "최고 입찰가 ${formatKoreanWon(car.currentPriceWon)}"
            )
        }
    }
}




@Composable
private fun CountdownText(endsAtMillis: Long) {
    var remain by remember(endsAtMillis) { mutableStateOf(endsAtMillis - System.currentTimeMillis()) }

    LaunchedEffect(endsAtMillis) {
        while (remain > 0) {
            kotlinx.coroutines.delay(1000)
            remain = endsAtMillis - System.currentTimeMillis()
        }
        remain = 0
    }

    val h = TimeUnit.MILLISECONDS.toHours(remain.coerceAtLeast(0))
    val m = TimeUnit.MILLISECONDS.toMinutes(remain.coerceAtLeast(0)) % 60

    val text = if (h > 0) {
        "${h}시간 ${m}분"
    } else {
        "${m}분"
    }

    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
        color = Red_1
    )
}
private val dec = DecimalFormat("#,###")
private fun formatWon(won: Long) = dec.format(won) + "원"
private fun formatKm(km: Int) = dec.format(km)

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