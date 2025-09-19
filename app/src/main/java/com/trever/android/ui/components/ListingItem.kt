package com.trever.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults.Icon
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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.trever.android.R
import com.trever.android.domain.model.AuctionCar


import com.trever.android.ui.theme.Grey_100
import com.trever.android.ui.theme.Grey_400
import com.trever.android.ui.theme.Red_1
import com.trever.android.ui.theme.backgroundColor
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

@Composable
fun ListingItem(
    car: AuctionCar,
    onClick: () -> Unit,
    onToggleLike: (() -> Unit)? = null,
    tags: List<String> = emptyList(),
    priceLabel: String = "최고 입찰가",
    showBadge: Boolean = true,
    showAuctionMeta: Boolean = true,
) {
    val cs = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cs.backgroundColor),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Grey_100)
        )
    ) {
        Column(Modifier.fillMaxWidth()) {
            // 이미지 영역 - 패딩 없이 꽉 차게 설정
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
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

                if (showBadge) {
                    AuctionBadge(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 12.dp, top = 8.dp)
                    )
                }

                if (onToggleLike != null) {
                    IconButton(
                        onClick = onToggleLike,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(36.dp)
                    ) {
                        if (car.liked) {
                            Icon(Icons.Default.Favorite, contentDescription = "찜 해제", tint = Red_1)
                        } else {
                            Icon(Icons.Default.FavoriteBorder, contentDescription = "찜하기", tint = cs.onSurface)
                        }
                    }
                }
            }

            // 정보 영역 - 패딩 적용
            Column(Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = car.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                        fontSize = 18.sp
                    )

                    if (showAuctionMeta) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
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
                }

                Text(
                    text = "${car.year}년 · ${formatKm(car.mileageKm)}km",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )

                Spacer(Modifier.height(6.dp))

                TagsWithPrice(
                    tags = tags.take(3),
                    priceText = "$priceLabel ${formatKoreanWon(car.currentPriceWon)}"
                )
            }
        }
    }
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

private val dec = DecimalFormat("#,###")

private fun formatKm(km: Int) = dec.format(km)

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

        // 금액 표시 로직 수정
        if (priceText.startsWith("최고 입찰가")) {
            // 경매 화면: "최고 입찰가"와 실제 금액 분리
            val labelEndIndex = priceText.indexOf("최고 입찰가") + "최고 입찰가".length
            val label = priceText.substring(0, labelEndIndex)
            val amount = priceText.substring(labelEndIndex).trim()

            Text(
                buildAnnotatedString {
                    withStyle(
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = Color.Black
                        ).toSpanStyle()
                    ) {
                        append("$label ")
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
        } else {
            // 구매 화면: 금액 전체를 초록색으로 표시
            Text(
                text = priceText,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00C364) // 초록색
                )
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
