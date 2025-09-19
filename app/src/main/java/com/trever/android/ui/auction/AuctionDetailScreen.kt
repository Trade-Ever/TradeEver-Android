package com.trever.android.ui.auction


import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.trever.android.ui.components.AppFilledButton
import com.trever.android.ui.components.AuctionBadge
import com.trever.android.ui.components.DetailContent
import com.trever.android.ui.theme.G_100
import com.trever.android.ui.theme.G_200
import com.trever.android.ui.theme.G_300
import com.trever.android.ui.theme.Green
import com.trever.android.ui.theme.Red_1
import com.trever.android.ui.theme.Grey_100
import com.trever.android.ui.theme.Grey_400
import com.trever.android.ui.theme.backgroundColor

// 배경 확장 컬러 사용 (이미 네가 정의한 확장)


@Composable
fun AuctionDetailScreen(
    carId: String,                            // ← 추가
    item: AuctionDetailUi = demoDetail(),
    onBack: () -> Unit = {},
    onLike: () -> Unit = {},
    onBid: () -> Unit = {},
    onShowBidHistory: (String) -> Unit = {}   // ← 추가
) {

    var showBidSheet by remember { mutableStateOf(false) }
    val blur by animateDpAsState(if (showBidSheet) 12.dp else 0.dp, label = "")

    Box(Modifier.blur(blur)) {
    DetailContent(
        item = item,
        onBack = onBack,
        badge = { AuctionBadge() },            // 경매 배지
        showBidSection = true,                 // 입찰내역 보여줌
        onMoreBids = { onShowBidHistory(carId) },
        bottomBar = {
            BottomActionBar(
                currentPrice = item.priceWonText,
                startPriceText = item.startPriceText,
                remainText = item.remainText,
                topBidderName = item.bids.firstOrNull()?.name,
                topBidderAvatarUrl = item.bids.firstOrNull()?.avatarUrl,
                onBid = { showBidSheet = true }
            )
        }

    )}
    if (showBidSheet) {
        PlaceBidSheet(
            currentTopPrice = item.priceWon,
            onConfirm = { newBid ->
                // 필요하면 외부로 이벤트 알림
                onBid()
                showBidSheet = false
            },
            onDismiss = { showBidSheet = false }
        )
    }
}

@Composable
private fun FloatingBackButton(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    Surface(
        onClick = onBack,
        shape = CircleShape,
        color = cs.surface.copy(alpha = 0.6f), // 반투명 배경으로 어떤 배경 위에서도 잘 보이게
        shadowElevation = 6.dp,
        tonalElevation = 0.dp,
        modifier = modifier
            .statusBarsPadding()
            .padding(12.dp)
            .size(36.dp)
            .zIndex(3f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "뒤로가기",
                tint = cs.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceBidSheet(
    currentTopPrice: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val green = Color(0xFF00C364)

    // ▼ 단일 소스 상태 (만원 단위)
    var addMan by remember { mutableStateOf(1) }
    val proposedBid = currentTopPrice + (addMan.coerceAtLeast(0)) * 10_000L
    val canBid = addMan >= 1

    // ▼ 표시/계산용 파생 값
    val safeAddMan = addMan.coerceAtLeast(1)
    val newBid = currentTopPrice + safeAddMan * 10_000L

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = cs.backgroundColor,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 현재가 / 계산된 입찰가
            Text(
                text = "시작가 ${formatKoreanWon(currentTopPrice)}",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = cs.G_200
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "현재가 ${formatKoreanWon(currentTopPrice)}",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = cs.G_300
            )
            Spacer(Modifier.height(2.dp))
            Text(
                formatKoreanWon(newBid),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = green
            )

            Spacer(Modifier.height(12.dp))

            // ====== 직접 입력 + 스테퍼 ======
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BidAmountRow(
                    valueMan = addMan.toString(),
                    onValueChange = { s ->
                        // 숫자만 허용 + 최대 6자리
                        addMan = s.filter(Char::isDigit).take(6).toIntOrNull() ?: 0
                    },
                    onMinus = { addMan = (addMan - 1).coerceAtLeast(0) },
                    onPlus  = { addMan = (addMan + 1).coerceAtMost(999_999) }
                )
            }
            // ==============================

            Spacer(Modifier.height(16.dp))

            AppFilledButton(
                text = "상위 입찰",
                onClick = { onConfirm(proposedBid) },
                enabled = canBid,                           // ★ 1만원 미만이면 false
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun BidAmountRow(
    valueMan: String,
    onValueChange: (String) -> Unit,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 입력 박스 (가운데 정렬)
        BasicTextField(
            value = valueMan,
            onValueChange = { s ->
                val t = s.filter { it.isDigit() }.take(6)
                onValueChange(t)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = MaterialTheme.typography.titleMedium.copy(
                color = cs.onSurface,
                textAlign = TextAlign.End
            ),
            modifier = Modifier
                .weight(1f)              // ★ 핵심: 남은 폭 전부 차지
                .height(44.dp),
            decorationBox = { inner ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(cs.backgroundColor)               // 배경
                        .border(1.dp, cs.outline.copy(0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    if (valueMan.isEmpty()) {
                        Text("0", color = cs.onSurfaceVariant) // 플레이스홀더
                    } else inner()
                }
            }
        )

        Spacer(Modifier.width(10.dp))
        Text("만원", color = cs.onSurface, style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.width(20.dp))

        IconStepButton(onClick = onMinus, iconRes = com.trever.android.R.drawable.remove) // ▸ 마이너스 아이콘
        Spacer(Modifier.width(8.dp))
        IconStepButton(onClick = onPlus,  iconRes = com.trever.android.R.drawable.add)
    }
}

@Composable
private fun IconStepButton(
    onClick: () -> Unit,
    iconRes: Int,


    ) {
    val cs = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = cs.G_100,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.size(36.dp)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,

                )
        }
    }
}


@Composable
private fun StepperField(
    value: Int,
    onValueChange: (Int) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val boxShape = RoundedCornerShape(12.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(boxShape)
            .border(1.dp, cs.outlineVariant, boxShape)

            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 중앙 숫자
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Text("만원", color = cs.onSurfaceVariant)

        Spacer(Modifier.weight(1f))

        // - 버튼
        SmallCircleButton(

            onClick = { onValueChange((value - 1).coerceAtLeast(1)) },
            icon = com.trever.android.R.drawable.remove
        )
        Spacer(Modifier.width(8.dp))
        // + 버튼
        SmallCircleButton(
            onClick = { onValueChange(value + 1) },
            icon = com.trever.android.R.drawable.add
        )
    }
}

@Composable
private fun SmallCircleButton(
    onClick: () -> Unit,
    icon: Int
) {
    val cs = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = cs.G_100,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier.size(36.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = cs.onSurfaceVariant
            )
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















@Composable
private fun BottomActionBar(
    currentPrice: String,          // "1억 2,500만원"
    startPriceText: String,        // "시작가 1억 500만원"
    remainText: String,            // "1시간 15분"
    topBidderName: String?,        // 상위 입찰자 이름
    topBidderAvatarUrl: String? = null,
    onBid: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val green = Color(0xFF00C364)


    Surface(
        modifier = modifier.fillMaxWidth(),

        color = cs.backgroundColor,
        contentColor = cs.onSurface,
        tonalElevation = 2.dp,
        shadowElevation = 12.dp
    ) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(cs.backgroundColor)
            .navigationBarsPadding()

            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

            // 상단 칩 (상위 입찰자 + 남은시간)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFFFEFEF))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 아바타
                if (!topBidderAvatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = topBidderAvatarUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(28.dp).clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier.size(28.dp).clip(CircleShape).background(Grey_100),
                        contentAlignment = Alignment.Center
                    ) { Text((topBidderName ?: "-").take(1)) }
                }

                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("상위 입찰자", style = MaterialTheme.typography.labelSmall, color = cs.onSurface.copy(0.6f))
                    Text(topBidderName ?: "-", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }

                Icon(
                    painter = painterResource(id = com.trever.android.R.drawable.gavel_1),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    remainText,
                    color = Red_1,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            }

            // 현재가/버튼 영역
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = currentPrice,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = cs.Green
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(startPriceText, color = Grey_400, style = MaterialTheme.typography.labelLarge)
                }

                AppFilledButton(
                    text = "상위 입찰",
                    onClick = onBid,
                    modifier = Modifier.height(50.dp).widthIn(min = 140.dp),
                       // <- onPrimary 권장
                )
            }
        }
    }}
}




/* ------------------------------------ */
/* ------------- 데모 모델 ------------- */
/* ------------------------------------ */

data class AuctionDetailUi(
    val images: List<String>,
    val liked: Boolean,
    val title: String,
    val subTitle: String,          // 예: 2024년 · 3.5만km
    val priceWon: Long,
    val priceWonText: String,
    val startPriceText: String,    // 예: 시작가 1억 5,000만원
    val likeCount: Int,
    val remainText: String,        // 예: 1시간 15분
    val specs: List<Pair<String,String>>,
    val notice: String,
    val bids: List<BidUi>,
    val seller: SellerUi
)

data class BidUi(
    val name: String,
    val amountText: String,
    val timeText: String,
    val avatarUrl: String? = null,   // ⬅️ 추가
)
data class SellerUi(val name: String, val id: String, val addr: String, val regDate: String, val validDate: String, val count: Int, val response: Int,val avatarUrl: String? = null)

/* 샘플 */
private fun demoDetail() = AuctionDetailUi(
    images = listOf(
        "https://picsum.photos/id/1018/1600/900",
        "https://picsum.photos/id/1015/1600/900",
        "https://picsum.photos/id/1020/1600/900",
        "https://picsum.photos/id/1024/1600/900",
        "https://picsum.photos/id/1033/1600/900",
    ),
    liked = false,
    title = "테슬라 Model X AWD",
    subTitle = "2024년 · 3.5만km",
    priceWon = 125_000_000,
    priceWonText = "1억 2,500만원",
    startPriceText = "시작가 1억 5,000만원",
    likeCount = 32,
    remainText = "1시간 15분",
    specs = listOf(
        "연료" to "전기",
        "변속기" to "자동",
        "배기량(cc)" to "엔진없음",
        "마력" to "252마력",
        "색상" to "미드나잇 실버",
        "기타 정보" to listOf(
            "열선시트(앞좌석)", "글라스 루프", "내비게이션(정품)",
            "열선핸들", "전동시트(앞좌석)", "메모리시트(운전석)",
            "전동식 트렁크", "통풍시트(앞좌석)", "전동시트(뒷좌석)", "열선시트(뒷좌석)", "FSD"
        ).joinToString("\n"),
        "사고 이력" to "있음",
        "사고 설명" to "내차 피해 (1건)"
    ),
    notice = "2열 캡틴 시트가 장착된 6인승 차량입니다.\n" +
            "\n" +
            "FSD(Full Self Driving) 옵션이 탑재된 차량입니다.\n" +
            "\n" +
            "열선 핸들, 열선 시트(1열 및 2열), 통풍 시트(1열)가 탑재되어 쾌적한 주행이 가능한 차량입니다 .",
    bids = listOf(
        BidUi("홍길동", "1억 2,500만원", "2025-09-15 18:15"),
        BidUi("오광운", "1억 2,000만원", "2025-09-15 18:15"),
        BidUi("최상근", "1억 1,000만원", "2025-09-15 18:15"),
    ),
    seller = SellerUi(
        name = "김판매",
        id = "seller123",
        addr = "경기 수원시 영통구",
        regDate = "2025.09.12",
        validDate = "2025.09.16",
        count = 39,
        response = 96
    )
)






