package com.trever.android.ui.auction


import android.R.attr.translationY
import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.trever.android.ui.auction.IconStepButton
import com.trever.android.ui.auction.SectionHeader
import com.trever.android.ui.components.AuctionBadge
import com.trever.android.ui.theme.G_100
import com.trever.android.ui.theme.G_200
import com.trever.android.ui.theme.G_300
import com.trever.android.ui.theme.Green
import com.trever.android.ui.theme.Red_1
import com.trever.android.ui.theme.Red_2
import com.trever.android.ui.theme.Grey_100
import com.trever.android.ui.theme.Grey_400
import com.trever.android.ui.theme.backgroundColor
import com.trever.android.ui.theme.bottomBarUnselected
import com.trever.android.ui.theme.noticeContainer
import com.trever.android.ui.theme.noticeOutline
import kotlinx.coroutines.launch

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
    val cs = MaterialTheme.colorScheme

    var bottomBarHeightPx by remember { mutableStateOf(0) }
    val bottomBarHeightDp = with(LocalDensity.current) { bottomBarHeightPx.toDp() }
    var showBidSheet by remember { mutableStateOf(false) }

    // 화면 전체
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.backgroundColor)
    ) {
        // 스크롤 영역
        Box(Modifier.then(if (showBidSheet) Modifier.blur(12.dp) else Modifier)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    bottom = bottomBarHeightDp
                // 여유 8dp
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
                        BidSection(
                            bids = item.bids,
                            onMore = { onShowBidHistory(carId) }
                        )
                    }
                }

                // 6) 판매자 정보
                item { SellerSection(seller = item.seller) }
            }
        }
        // 하단 고정 바
        BottomActionBar(
            currentPrice = item.priceWonText,
            startPriceText = item.startPriceText,
            remainText = item.remainText,
            topBidderName = item.bids.firstOrNull()?.name,      // 상위 입찰자
            topBidderAvatarUrl = item.bids.firstOrNull()?.avatarUrl,
            onBid = { showBidSheet = true },
            modifier = Modifier.align(Alignment.BottomCenter)
                .onSizeChanged { bottomBarHeightPx = it.height }
        )
        // 모달 바텀시트
        if (showBidSheet) {
            PlaceBidSheet(
                currentTopPrice = item.priceWon,               // Long
                onConfirm = { newBidWon ->
                    // TODO: 서버 호출 등 처리
                    showBidSheet = false
                },
                onDismiss = { showBidSheet = false }
            )
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
fun AppFilledButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 50.dp,
    shape: RoundedCornerShape = RoundedCornerShape(14.dp),
    @DrawableRes leadingIconRes: Int? = null,
    @DrawableRes trailingIconRes: Int? = null,
    iconSize: Dp = 18.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp)
) {
    val cs = MaterialTheme.colorScheme
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(height),
        shape = shape,
        contentPadding = contentPadding,
        colors = ButtonDefaults.buttonColors(
            containerColor = cs.primary,              // ✅ 항상 primary
            contentColor = cs.backgroundColor,
            disabledContainerColor = cs.G_300,     // ✅ 비활성화 배경
            disabledContentColor = cs.backgroundColor
        )
    ) {
        if (leadingIconRes != null) {
            Icon(painterResource(leadingIconRes), null, Modifier.size(iconSize))
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        if (trailingIconRes != null) {
            Spacer(Modifier.width(8.dp))
            Icon(painterResource(trailingIconRes), null, Modifier.size(iconSize))
        }
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
private fun TitleSection(
    title: String,
    subTitle: String,
    priceWon: Long,
    likeCount: Int,
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
            .background(cs.noticeContainer)
            .border(1.dp,cs.noticeOutline, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(text = text, color = cs.onSurface)
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



