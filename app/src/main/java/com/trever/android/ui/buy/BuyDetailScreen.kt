package com.trever.android.ui.buy

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.dp
import com.trever.android.ui.auction.AuctionDetailUi
import com.trever.android.ui.components.DetailContent
import com.trever.android.ui.components.SellingBadge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.trever.android.ui.auction.SellerUi
import com.trever.android.ui.components.AppFilledButton
import com.trever.android.ui.components.AppOutlinedButton
import com.trever.android.ui.theme.backgroundColor

@Composable
fun BuyDetailScreen(
    carId: String,
    item: AuctionDetailUi = demoBuyDetail(),
    onBack: () -> Unit = {},
    onLike: () -> Unit = {},
    onInquiry: () -> Unit = {},
    onBuy: () -> Unit = {}
) {
    var showBuySheet by remember { mutableStateOf(false) }
    val blur by animateDpAsState(if (showBuySheet) 12.dp else 0.dp, label = "")

    Box(Modifier.blur(blur)) {
        DetailContent(
            item = item,
            onBack = onBack,
            badge = { SellingBadge() },  // 구매 뱃지
            showBidSection = false,  // 입찰 섹션 표시 안 함
            onMoreBids = null,       // 입찰 내역 보기 기능 비활성화
            bottomBar = {
                BuyBottomActionBar(
                    price = item.priceWonText,
                    onBuy = { showBuySheet = true },
                    onInquiry = onInquiry
                )
            }
        )
    }

    if (showBuySheet) {
        BuyConfirmSheet(
            price = item.priceWon,
            onConfirm = {
                onBuy()
                showBuySheet = false
            },
            onDismiss = { showBuySheet = false }
        )
    }
}

@Composable
private fun BuyBottomActionBar(
    price: String,
    onBuy: () -> Unit,
    onInquiry: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = cs.backgroundColor,
        contentColor = cs.onSurface,
        tonalElevation = 2.dp,
        shadowElevation = 12.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppOutlinedButton(
                    text = "문의하기",
                    onClick = onInquiry,
                    modifier = Modifier.weight(1f),
                    height = 48.dp,

                )

                // 구매하기 버튼 - 채워진 스타일
                AppFilledButton(
                    text = "구매하기",
                    onClick = onBuy,
                    modifier = Modifier.weight(1f),
                    height = 48.dp,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BuyConfirmSheet(
    price: Long,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val cs = MaterialTheme.colorScheme

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = cs.backgroundColor,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "구매 확인",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = formatKoreanWon(price),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "위 금액으로 구매를 진행하시겠습니까?",
                style = MaterialTheme.typography.bodyMedium,
                color = cs.onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    color = cs.surfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            "취소",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                AppFilledButton(
                    text = "구매하기",
                    onClick = onConfirm,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

private fun demoBuyDetail() = AuctionDetailUi(
    images = listOf(
        "https://picsum.photos/id/1018/1600/900",
        "https://picsum.photos/id/1015/1600/900",
        "https://picsum.photos/id/1020/1600/900",
    ),
    liked = false,
    title = "현대 아반떼 CN7",
    subTitle = "2023년 · 1.5만km",
    priceWon = 22_500_000,
    priceWonText = "2,250만원",
    startPriceText = "",  // 경매가 아니므로 시작가 필요 없음
    likeCount = 18,
    remainText = "",      // 경매가 아니므로 남은 시간 필요 없음
    specs = listOf(
        "연료" to "가솔린",
        "변속기" to "자동",
        "배기량(cc)" to "1,598cc",
        "마력" to "123마력",
        "색상" to "아마존 그레이",
        "기타 정보" to listOf(
            "열선시트(앞좌석)", "스마트키", "내비게이션(정품)",
            "블루투스", "전동시트(운전석)", "통풍시트(앞좌석)"
        ).joinToString("\n"),
        "사고 이력" to "없음",
        "사고 설명" to ""
    ),
    notice = "신차 보증기간 이내의 차량입니다.\n" +
            "\n" +
            "풀옵션 차량으로 편의장비 완벽하게 갖춰진 차량입니다.",
    bids = emptyList(),  // 입찰 내역 없음
    seller = SellerUi(
        name = "이판매",
        id = "seller456",
        addr = "서울 강남구 역삼동",
        regDate = "2025.08.22",
        validDate = "2025.10.12",
        count = 57,
        response = 98,
        avatarUrl = null
    )
)

private fun formatKoreanWon(amount: Long): String {
    val 억 = amount / 100_000_000
    val 만 = (amount % 100_000_000) / 10_000

    return buildString {
        if (억 > 0) append("${억}억 ")
        if (만 > 0) append("${만}만원")
        if (억 == 0L && 만 == 0L) append("0원")
    }.trim()
}
