package com.trever.android.ui.auction


import android.R.attr.translationY
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.trever.android.ui.components.AuctionBadge
import com.trever.android.ui.theme.Red_1
import com.trever.android.ui.theme.Red_2
import com.trever.android.ui.theme.Grey_100
import com.trever.android.ui.theme.Grey_400
import com.trever.android.ui.theme.backgroundColor
import kotlinx.coroutines.launch

// 배경 확장 컬러 사용 (이미 네가 정의한 확장)


@Composable
fun AuctionDetailScreen(
    item: AuctionDetailUi = demoDetail(),
    onBack: () -> Unit = {},
    onLike: () -> Unit = {},
    onBid: () -> Unit = {}
) {
    val cs = MaterialTheme.colorScheme
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    // 화면 전체
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.backgroundColor)
    ) {
        // 스크롤 영역
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = 96.dp + navBottom   // ✅ 바텀바 높이 + 시스템 하단 인셋
            ),

            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // 1) 이미지 영역 (간단 슬라이드 도트 포함)
            item {
                ImageHeader(
                    images = item.images,
                    onBack = onBack
                )
            }

            // 2) 타이틀 + 가격 + 좋아요 카운트 + 남은시간 배지
            item {
                TitleSection(
                    title = item.title,
                    subTitle = item.subTitle,
                    priceWon = item.priceWon,
                    likeCount = item.likeCount,

                )
            }


            item {
                SpecColumnOrdered(
                    specs = item.specs,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // 4) 설명/주의 배지 박스
            if (item.notice.isNotBlank()) {
                item {
                    NoticeCard(text = item.notice)
                }
            }

            // 5) 입찰 내역
            if (item.bids.isNotEmpty()) {
                item {
                    SectionHeader(title = "입찰 내역", actionText = "더보기", onAction = { /* TODO */ })
                }
                items(item.bids) { bid ->
                    BidRow(bid)
                }
            }

            // 6) 판매자 정보
            item {
                SectionHeader(title = "판매자 정보")
            }
            item {
                SellerCard(seller = item.seller)
            }
        }

        // 하단 고정 바
        BottomActionBar(
            currentPrice = item.priceWonText,
            startPriceText = item.startPriceText,
            remainText = item.remainText,
            onBid = onBid,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/* ------------------------------------ */
/* ---------- 구성 요소들 ------------- */
/* ------------------------------------ */

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ImageHeader(
    images: List<String>,
    onBack: () -> Unit
) {
    var showViewer by remember { mutableStateOf(false) }
    var viewerIndex by remember { mutableStateOf(0) }

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { images.size.coerceAtLeast(1) })
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp + topInset)
            .background(Color.Black)
    ) {
        HorizontalPager(state = pagerState) { page ->
            AsyncImage(
                model = images.getOrNull(page),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        viewerIndex = page
                        showViewer = true
                    }
            )
        }

        // 뒤로가기
        Box(
            modifier = Modifier
                .padding(12.dp)
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0x66000000))
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "back", tint = Color.White)
        }

        PagerDotsIndicator(
            total = images.size,
            current = pagerState.currentPage,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )
    }

    if (showViewer) {
        FullScreenImageViewer(
            images = images,
            initialPage = viewerIndex,
            onClose = { showViewer = false }
        )
    }
}
@Composable
private fun TitleSection(
    title: String,
    subTitle: String,
    priceWon: Long,
    likeCount: Int,
) {
    Column(Modifier.padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.weight(1f)
            )
            Text(text = "$likeCount", color = Color(0xFF9198A1))
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = Color(0xFF9198A1))
        }
        Spacer(Modifier.height(2.dp))
        Text(text = subTitle, color = Color(0xFF9198A1))

        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatKoreanWon(priceWon),
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color(0xFF00C364),
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(Modifier.width(8.dp))
            // 진행중 배지
            AuctionBadge()
        }

        Spacer(Modifier.height(8.dp))

    }
}




@Composable
private fun NoticeCard(text: String) {

    val cs = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF7F3FF))
            .border(1.dp, Color(0xFFE7DFFF), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(text = text)
    }
}

@Composable
private fun SectionHeader(title: String, actionText: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
        if (actionText != null && onAction != null) {
            Text(
                actionText,
                color = Color(0xFF818B99),
                modifier = Modifier.clickable { onAction() }
            )
        }
    }
}

@Composable
private fun BidRow(bid: BidUi) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 프로필 원형
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Grey_100),
            contentAlignment = Alignment.Center
        ) {
            Text(bid.name.take(1))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(bid.name, fontWeight = FontWeight.Medium)
            Text(bid.timeText, color = Grey_400, style = MaterialTheme.typography.labelSmall)
        }
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFEFFBF4))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(bid.amountText, color = Color(0xFF00A85A), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SellerCard(seller: SellerUi) {
    Column(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFF1F3F5), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Grey_100),
                contentAlignment = Alignment.Center
            ) {
                Text(seller.name.take(1))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(seller.name, fontWeight = FontWeight.Medium)
                Text("거래 ${seller.count}회 · 응답률 ${seller.response}%", color = Grey_400, style = MaterialTheme.typography.labelSmall)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row {
            InfoCell("판매자 ID", seller.id)
            Spacer(Modifier.width(12.dp))
            InfoCell("주소", seller.addr)
        }
        Spacer(Modifier.height(8.dp))
        Row {
            InfoCell("등록일", seller.regDate)
            Spacer(Modifier.width(12.dp))
            InfoCell("유효기간", seller.validDate)
        }
    }
}

@Composable
private fun InfoCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(label, color = Grey_400, style = MaterialTheme.typography.labelSmall)
        Text(value, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun BottomActionBar(
    currentPrice: String,
    startPriceText: String,
    remainText: String,
    onBid: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)

    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xFFFFF0F0))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(remainText, color = Red_1, style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(startPriceText, color = Grey_400, style = MaterialTheme.typography.labelSmall)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = currentPrice,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(Modifier.width(16.dp))
            // 여기에 입찰 버튼 추가하면 됨
        }
    }
}

@Composable
private fun SpecOneLine(
    specs: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    // 원하는 표시 순서
    val order = listOf("연료", "변속기", "배기량(cc)", "마력", "색상", "기타 정보", "사고 이력", "사고 설명")

    // 넘어온 스펙을 맵으로
    val map = remember(specs) { specs.toMap() }

    // 존재하는 항목만 "키: 값" 문자열로 생성
    val parts = remember(map) {
        order.mapNotNull { key ->
            map[key]?.takeIf { it.isNotBlank() }?.let { "$key: $it" }
        }
    }

    // 한 줄 + 가로 스크롤(길면 스크롤해서 보이게)
    Text(
        text = parts.joinToString(" · "),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
        maxLines = 1,
        overflow = TextOverflow.Clip, // 스크롤을 쓰므로 Ellipsis 대신 Clip
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    )
}
@Composable
private fun KeyValueLine(
    key: String,
    value: String,
    keyWidth: Dp = 86.dp
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top   // 값이 여러 줄일 때 윗줄 맞춤
    ) {
        Text(
            text = key,
            color = Color(0xFF818B99),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(keyWidth)
        )
        Text(
            text = value,                               // \n 그대로 표시
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SpecColumnOrdered(
    specs: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    val order = listOf("연료", "변속기", "배기량(cc)", "마력", "색상", "기타 정보", "사고 이력", "사고 설명")
    val map = remember(specs) { specs.toMap() }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        order.forEach { key ->
            val value = map[key]
            if (!value.isNullOrBlank()) {
                KeyValueLine(key, value)
            }
        }
    }
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

data class BidUi(val name: String, val amountText: String, val timeText: String)
data class SellerUi(val name: String, val id: String, val addr: String, val regDate: String, val validDate: String, val count: Int, val response: Int)

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
        BidUi("홍길동", "1억 2,500만원", "1분 전"),
        BidUi("오광운", "1억 2,000만원", "10분 전"),
        BidUi("최상근", "1억 1,000만원", "20분 전"),
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

/* 유틸 */
private fun formatKoreanWon(amount: Long): String {
    val eok = amount / 100_000_000
    val man = (amount % 100_000_000) / 10_000
    return buildString {
        if (eok > 0) append("${eok}억 ")
        if (man > 0) append("${man}만원")
        if (eok == 0L && man == 0L) append("0원")
    }.trim()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FullScreenImageViewer(
    images: List<String>,
    initialPage: Int,
    onClose: () -> Unit
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        var currentScale by remember { mutableStateOf(1f) }
        val pagerState = rememberPagerState(
            initialPage = initialPage.coerceIn(0, (images.size - 1).coerceAtLeast(0)),
            pageCount = { images.size.coerceAtLeast(1) }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = currentScale == 1f // 확대 중이면 페이지 스와이프 잠금
            ) { page ->
                ZoomableImage(
                    model = images.getOrNull(page),
                    onScaleChanged = { s -> currentScale = s }
                )
            }

            // 닫기
            Box(
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.TopStart)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0x66000000))
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "close", tint = Color.White)
            }

            PagerDotsIndicator(
                total = images.size,
                current = pagerState.currentPage,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }
}
@Composable
private fun PagerDotsIndicator(
    total: Int,
    current: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total) { i ->
            Box(
                modifier = Modifier
                    .size(if (i == current) 8.dp else 6.dp)
                    .clip(CircleShape)
                    .background(if (i == current) Color.White else Color(0x66FFFFFF))
            )
        }
    }
}

@Composable
private fun ZoomableImage(
    model: Any?,
    onScaleChanged: (Float) -> Unit = {}
) {
    val minScale = 1f
    val maxScale = 4f

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    val scope = rememberCoroutineScope()

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        val newScale = (scale * zoomChange).coerceIn(minScale, maxScale)
        // 스케일 변화 비율에 맞춰 패닝 적용
        val newOffset = if (newScale > 1f) offset + panChange else Offset.Zero

        scale = newScale
        offset = clampOffset(newOffset, containerSize, scale)
        onScaleChanged(scale)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it }
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { tap ->
                        scope.launch {
                            val target = if (scale > 1f) 1f else 2.5f
                            scale = target
                            // 더블탭 위치를 중심으로 살짝 이동(간단 처리)
                            offset = if (target == 1f) {
                                Offset.Zero
                            } else {
                                // 중앙 기준으로 약간 당겨주는 정도
                                (offset + (tap - Offset(containerSize.width / 2f, containerSize.height / 2f)) / 2f)
                            }
                            offset = clampOffset(offset, containerSize, scale)
                            onScaleChanged(scale)
                        }
                    }
                )
            }
            .transformable(transformState)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x
                translationY = offset.y
            }
    ) {
        AsyncImage(
            model = model,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}
private fun clampOffset(offset: Offset, container: IntSize, scale: Float): Offset {
    if (container.width == 0 || container.height == 0) return Offset.Zero
    if (scale <= 1f) return Offset.Zero

    val maxX = (container.width * (scale - 1f)) / 2f
    val maxY = (container.height * (scale - 1f)) / 2f

    return Offset(
        x = offset.x.coerceIn(-maxX, maxX),
        y = offset.y.coerceIn(-maxY, maxY)
    )
}


