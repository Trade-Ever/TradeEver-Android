package com.trever.android.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.trever.android.ui.auction.AuctionDetailUi
import com.trever.android.ui.auction.BidUi
import com.trever.android.ui.auction.SellerUi
import com.trever.android.ui.theme.G_100
import com.trever.android.ui.theme.G_300
import com.trever.android.ui.theme.Green
import com.trever.android.ui.theme.Grey_100
import com.trever.android.ui.theme.backgroundColor
import com.trever.android.ui.theme.noticeContainer
import com.trever.android.ui.theme.noticeOutline
import kotlinx.coroutines.launch
import kotlin.collections.forEach

@Composable
fun DetailContent(
    item: AuctionDetailUi,
    onBack: () -> Unit,
    badge: @Composable () -> Unit,
    showBidSection: Boolean,
    onMoreBids: (() -> Unit)?,
    bottomBar: (@Composable () -> Unit)? = null,


) {
    val cs = MaterialTheme.colorScheme
    var bottomBarHeightPx by remember { mutableStateOf(0) }
    val bottomBarHeightDp = with(LocalDensity.current) { bottomBarHeightPx.toDp() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.backgroundColor)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = if (bottomBar != null) bottomBarHeightDp else 0.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ImageHeader(
                    images = item.images,
                )
            }
            item {
                TitleSection(
                    title = item.title,
                    subTitle = item.subTitle,
                    priceWon = item.priceWon,
                    likeCount = item.likeCount,
                    priceColor = cs.Green,
                    badge = badge
                )
            }
            item {
                SpecColumnOrdered(
                    specs = item.specs,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            if (item.notice.isNotBlank()) {
                item { NoticeCard(text = item.notice) }
            }
            if (showBidSection && item.bids.isNotEmpty()) {
                item {
                    BidSection(
                        bids = item.bids,
                        onMore = { onMoreBids?.invoke() }
                    )
                }
            }
            item { SellerSection(seller = item.seller) }
        }

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

        if (bottomBar != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .onSizeChanged { bottomBarHeightPx = it.height }
            ) {
                bottomBar()
            }
        }
    }
}

@Composable
private fun TitleSection(
    title: String,
    subTitle: String,
    priceWon: Long,
    likeCount: Int,
    priceColor: Color = MaterialTheme.colorScheme.Green,
    badge: (@Composable () -> Unit)? = { AuctionBadge() } // 기본은 경매 배지
) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                color = cs.onSurface,
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
                    color = priceColor,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(Modifier.width(8.dp))
            badge?.invoke()
        }
        Spacer(Modifier.height(8.dp))
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ImageHeader(
    images: List<String>,

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
private fun NoticeCard(text: String) {

    val cs = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(cs.noticeContainer)
            .border(1.dp,cs.noticeOutline, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(text = text, color = cs.onSurface)
    }
}

@Composable
private fun BidSection(
    bids: List<BidUi>,
    onMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme

    val sectionBg = cs.G_100

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(sectionBg)
            .padding(vertical = 12.dp)
    ) {
        Column {
            SectionHeader(
                title = "입찰 내역",
                actionText = "더보기",
                onAction = onMore,
                actionIconRes = com.trever.android.R.drawable.arrow_right_1  // ⬅️ 네 리소스 이름에 맞춰 변경
            )
            Spacer(Modifier.height(8.dp))
            bids.forEach { bid ->
                BidRowPill(
                    bid = bid,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun SellerSection(
    seller: SellerUi,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(cs.backgroundColor)           // ✅ 섹션 회색 배경
            .padding(vertical = 12.dp)
    ) {
        Column {
            // 헤더(제목 + 우측 아바타)
            SectionHeader(
                title = "판매자 정보",
                trailing = {
                    if (!seller.avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = seller.avatarUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Grey_100),
                            contentAlignment = Alignment.Center
                        ) { Text(seller.name.take(1)) }
                    }
                }
            )
            Spacer(Modifier.height(8.dp))

            // 카드

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val keyW = 72.dp
                KeyValueLine(key = "판매자 ID", value = seller.id,       keyWidth = keyW)
                KeyValueLine(key = "주소",     value = seller.addr,     keyWidth = keyW)
                Spacer(Modifier.height(4.dp))
                KeyValueLine(key = "등록일",   value = seller.regDate,  keyWidth = keyW)
                KeyValueLine(key = "수정일",   value = seller.validDate, keyWidth = keyW)
            }

        }
    }
}

private fun formatKoreanWon(amount: Long): String {
    val eok = amount / 100_000_000
    val man = (amount % 100_000_000) / 10_000
    return buildString {
        if (eok > 0) append("${eok}억 ")
        if (man > 0) append("${man}만원")
        if (eok == 0L && man == 0L) append("0원")
    }.trim()
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
private fun BidRowPill(
    bid: BidUi,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    val green = Color(0xFF00C364) // 앱에서 쓰는 초록과 맞춤

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = cs.backgroundColor,
        tonalElevation = 1.dp,      // 다크/라이트 모두 자연스러운 톤
        shadowElevation = 3.dp      // 은은한 그림자
    ) {
        Row(
            modifier = Modifier
                .clickable { /* TODO: 클릭 액션 */ }
                .padding(horizontal = 12.dp, vertical = 10.dp)
            ,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 아바타 (이미지 있으면 이미지, 없으면 이니셜)
            if (!bid.avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = bid.avatarUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Grey_100),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = bid.name.take(1),
                        style = MaterialTheme.typography.labelMedium,
                        color = cs.onSurface
                    )
                }
            }

            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bid.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = bid.timeText,                    // ex) "2025-09-15 18:15"
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.G_300             // 다크/라이트 대응
                )
            }

            // 금액 (초록, 볼드)
            Text(
                text = bid.amountText,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = cs.Green
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
private fun SectionHeader(
    title: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    actionIconRes: Int? = null,
    actionTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    trailing: (@Composable (() -> Unit))? = null       // ✅ 추가
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))

        when {
            trailing != null -> trailing()              // ✅ 아바타 등 임의의 우측 콘텐츠
            actionText != null && onAction != null -> {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .clickable { onAction() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(actionText, color = actionTint, style = MaterialTheme.typography.labelLarge)
                    if (actionIconRes != null) {
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            painter = painterResource(id = actionIconRes),
                            contentDescription = null,
                            tint = actionTint,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
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




