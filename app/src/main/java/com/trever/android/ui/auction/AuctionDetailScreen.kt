package com.trever.android.ui.auction


import android.util.Log
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

import coil.compose.AsyncImage
import com.trever.android.data.remote.toAuctionDetailUi

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
import kotlinx.coroutines.launch


import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import kotlin.compareTo
import kotlin.div
import kotlin.rem
import kotlin.text.format

// 배경 확장 컬러 사용 (이미 네가 정의한 확장)




@Composable
fun AuctionDetailScreen(
    carId: String,
    auctionId: String,
    navController: NavHostController,
    viewModel: AuctionDetailViewModel = viewModel(),
    onBack: () -> Unit = {},
    onLike: () -> Unit = {},
    onBid: () -> Unit = {},
    onShowBidHistory: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val bids by viewModel.bids.collectAsState()
    val auction by viewModel.auction.collectAsState()

    // Snackbar 상태 관리 추가
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()



    // 화면 진입시 데이터 로드
    LaunchedEffect(carId) {
        viewModel.loadVehicleDetail(carId, auctionId)
    }

    var showBidSheet by remember { mutableStateOf(false) }
    val blur by animateDpAsState(if (showBidSheet) 12.dp else 0.dp, label = "")
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()

                .blur(blur)
        )
    {
        when (val state = uiState) {
            is AuctionDetailUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is AuctionDetailUiState.Success -> {
                // API 응답 데이터를 UI 모델로 변환
                val detailUi = state.vehicle.toAuctionDetailUi()


                // 판매자 정보 추출
                val sellerUi = SellerUi(
                    name = state.vehicle.sellerName ?: "",
                    id = state.vehicle.sellerId?.toString() ?: "",
                    addr = state.vehicle.sellerLocationCity ?: "",

                    avatarUrl = state.vehicle.sellerProfileImageUrl
                )

                val isSeller = state.vehicle.isSeller == true


                // 입찰 내역을 UI 모델에 통합
                val bidUiList = bids.map { bid ->
                    BidUi(
                        name = bid.bidderName,
                        amountText = formatKoreanWon(bid.bidPrice),
                        timeText = formatDateTime(bid.createdAt),
                        avatarUrl = bid.bidderAvatarUrl
                    )
                }

                // 최상위 입찰자 정보 가져오기
                val topBidder = bids.maxByOrNull { it.bidPrice }


                // currentBidPrice가 null 또는 0이면 startPrice 사용
                val currentPrice = if ((topBidder?.bidPrice ?: 0L) > 0L) {
                    topBidder?.bidPrice ?: 0L
                } else {
                    auction?.startPrice ?: detailUi.priceWon
                }
                val currentPriceText = formatKoreanWon(currentPrice)

                val now = System.currentTimeMillis()
                val startAtMillis = auction?.startAt?.takeIf { !it.isNullOrBlank() }?.let { parseDateTimeToMillis(it) } ?: Long.MAX_VALUE
                val endAtMillis = auction?.endAt?.takeIf { !it.isNullOrBlank() }?.let { parseDateTimeToMillis(it) } ?: 0L

                val isBeforeStart = now < startAtMillis
                val isAfterEnd = now > endAtMillis
                val bidEnabled = !isSeller && !isBeforeStart && !isAfterEnd

                Log.d("AuctionDetail", "now=$now, startAtMillis=$startAtMillis, endAtMillis=$endAtMillis, bidEnabled=$bidEnabled")

                // 시작가 텍스트
                val startPrice = auction?.startPrice ?: detailUi.priceWon
                val startPriceText = "시작가 ${formatKoreanWon(startPrice)}"

//                val endAtMillis = auction?.endAt?.let { endAt ->
//                    try {
//                        Log.d("AuctionDetail", "원본 종료 시간: $endAt")
//
//                        // 시간 문자열에서 나노초 부분 처리 (가변적인 길이 처리)
//                        val simplified = if (endAt.contains(".")) {
//                            val parts = endAt.split(".")
//                            val base = parts[0]
//                            val decimal = parts[1].replace("Z", "") // Z 제거
//                                .take(3) // 밀리초 3자리만 사용
//                            "$base.$decimal${if (endAt.endsWith("Z")) "Z" else ""}"
//                        } else {
//                            endAt
//                        }
//
//                        Log.d("AuctionDetail", "단순화된 종료 시간: $simplified")
//
//                        // 여러 날짜 포맷을 시도
//                        val formats = listOf(
//                            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
//                            "yyyy-MM-dd'T'HH:mm:ss.SSS",
//                            "yyyy-MM-dd'T'HH:mm:ss'Z'",
//                            "yyyy-MM-dd'T'HH:mm:ss"
//                        )
//
//                        var parsedTime: Long? = null
//                        for (pattern in formats) {
//                            try {
//                                val inputFormat = SimpleDateFormat(pattern, Locale.getDefault())
//                                // 서버에서 오는 시간이 이미 로컬 시간이므로 UTC 설정 제거
//                                // inputFormat.timeZone = TimeZone.getTimeZone("UTC")
//
//                                val date = inputFormat.parse(simplified)
//                                if (date != null) {
//                                    parsedTime = date.time
//                                    Log.d("AuctionDetail", "성공적으로 파싱됨: $pattern, 결과: ${Date(parsedTime)}")
//                                    break
//                                }
//                            } catch (e: Exception) {
//                                Log.e("AuctionDetail", "패턴 실패: $pattern - ${e.message}")
//                            }
//                        }
//
//                        parsedTime ?: run {
//                            Log.e("AuctionDetail", "모든 날짜 패턴으로 파싱 실패")
//                            System.currentTimeMillis() + 24 * 60 * 60 * 1000 // 기본값
//                        }
//                    } catch (e: Exception) {
//                        Log.e("AuctionDetail", "날짜 파싱 오류: ${e.message}")
//                        System.currentTimeMillis() + 24 * 60 * 60 * 1000
//                    }
//                } ?: (System.currentTimeMillis() + 24 * 60 * 60 * 1000)

                DetailContent(
                    item = detailUi.copy(bids = bidUiList, seller = sellerUi, priceWon = currentPrice, priceWonText = currentPriceText),
                    onBack = onBack,
                    badge = { AuctionBadge() },
                    showBidSection = true,
                    onMoreBids = { onShowBidHistory(carId) },
                    bottomBar = {
                        BottomActionBar(
                            currentPrice = currentPriceText,
                            startPriceText = startPriceText,
                            topBidderName = topBidder?.bidderName,
                            topBidderAvatarUrl = topBidder?.bidderAvatarUrl,
                            onBid = {
                                viewModel.resetBidResult()
                                showBidSheet = true
                            },
                            endAtMillis = endAtMillis,
                            bidEnabled = bidEnabled,
                        )
                    },
                    onToggleLike = {viewModel.toggleLike(carId)
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("likeChanged", true)}
                )
            }

            is AuctionDetailUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.message)
                }
            }
        }
    }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 50.dp)
                .zIndex(3f)
        )
    }


    if (showBidSheet) {

        val topBidder = bids.maxByOrNull { it.bidPrice }

        // 현재 최고 입찰가 계산 - 입찰가가 없거나 0이면 시작가 사용
        val currentTopPrice = if ((topBidder?.bidPrice ?: 0L) > 0L) {
            topBidder?.bidPrice ?: 0L
        } else {
            auction?.startPrice ?: ((uiState as? AuctionDetailUiState.Success)?.vehicle?.toAuctionDetailUi()?.priceWon ?: 0L)
        }

        // 시작가 가져오기
        val startPrice = auction?.startPrice
            ?: ((uiState as? AuctionDetailUiState.Success)?.vehicle?.toAuctionDetailUi()?.priceWon ?: 0L)

        val bidResult by viewModel.bidResult.collectAsState()

        // bidResult 상태에 따른 UI 업데이트 처리
        LaunchedEffect(bidResult) {
            bidResult?.let { result ->
                result.fold(
                    onSuccess = { bidData ->
                        // 성공 시 입찰 시트 닫기
                        showBidSheet = false
                        // 성공 메시지 스낵바 표시
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "입찰이 성공적으로 완료되었습니다",
                                duration = SnackbarDuration.Short
                            )
                        }
                    },
                    onFailure = { error ->
                        // 실패 메시지 스낵바 표시
                        showBidSheet = false
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "입찰에 실패했습니다",
                                duration = SnackbarDuration.Long
                            )
                        }
                    }
                )
            }
        }

        PlaceBidSheet(
            currentTopPrice = currentTopPrice,
            startPrice = startPrice,
            onConfirm = { newBid ->
                // auctionId, 새 입찰가, 사용자 ID로 API 호출
                val auctionIdInt = auctionId.toIntOrNull() ?: 0


                viewModel.placeBid(auctionIdInt, newBid)
                // 여기서는 즉시 닫지 않고, 결과에 따라 LaunchedEffect에서 처리
            },
            onDismiss = { showBidSheet = false }
        )
    }
}
private fun formatDateTime(dateTimeStr: String): String {
    Log.d("TimeFormat", "원본 시간: $dateTimeStr")
    return try {
        // 시간 문자열에서 나노초 부분 처리 (가변적인 길이 처리)
        val simplified = if (dateTimeStr.contains(".")) {
            val parts = dateTimeStr.split(".")
            val base = parts[0]
            val decimal = parts[1].replace("Z", "") // Z 제거
                .take(3) // 밀리초 3자리만 사용
            "$base.$decimal${if (dateTimeStr.endsWith("Z")) "Z" else ""}"
        } else {
            dateTimeStr
        }

        Log.d("TimeFormat", "단순화된 시간: $simplified")

        // 여러 날짜 포맷을 시도
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss"
        )

        var date: Date? = null
        for (pattern in formats) {
            try {
                val inputFormat = SimpleDateFormat(pattern, Locale.getDefault())

                // 서버에서 오는 시간이 이미 로컬 시간이므로 UTC 변환을 제거
                // inputFormat.timeZone = TimeZone.getTimeZone("UTC")

                date = inputFormat.parse(simplified)
                if (date != null) {
                    Log.d("TimeFormat", "성공한 패턴: $pattern")
                    break
                }
            } catch (e: Exception) {
                Log.e("TimeFormat", "패턴 실패: $pattern - ${e.message}")
            }
        }

        if (date != null) {
            val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA)
            // 로컬 시간으로 변환할 필요가 없으므로 기본 시간대 사용
            // outputFormat.timeZone = TimeZone.getDefault()
            val formattedDate = outputFormat.format(date)
            Log.d("TimeFormat", "변환된 시간: $formattedDate")
            formattedDate
        } else {
            Log.e("TimeFormat", "모든 시간 패턴 파싱 실패")
            dateTimeStr
        }
    } catch (e: Exception) {
        Log.e("TimeFormat", "시간 변환 오류: ${e.message}")
        dateTimeStr
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceBidSheet(
    currentTopPrice: Long,
    startPrice: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val green = Color(0xFF00C364)

    // 최초 입찰인지 확인 (현재가가 시작가와 동일하면 최초 입찰)
    val isFirstBid = currentTopPrice == startPrice

    // ▼ 단일 소스 상태 (만원 단위) - 최초 입찰이면 0부터 시작
    var addMan by remember { mutableStateOf(if (isFirstBid) 0 else 1) }
    val proposedBid = currentTopPrice + (addMan) * 10_000L

    // 입찰 가능 조건: 최초 입찰이면 0도 가능, 아니면 1 이상
    val canBid = if (isFirstBid) addMan >= 0 else addMan >= 1

    // ▼ 표시/계산용 파생 값
    val safeAddMan = if (isFirstBid) addMan.coerceAtLeast(0) else addMan.coerceAtLeast(1)
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
            // 시작가 표시
            Text(
                text = "시작가 ${formatKoreanWon(startPrice)}",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = cs.G_200
            )
            Spacer(Modifier.height(2.dp))
            // 현재가 표시
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BidAmountRow(
                    valueMan = addMan.toString(),
                    onValueChange = { s ->
                        addMan = s.filter(Char::isDigit).take(6).toIntOrNull() ?: 0
                    },
                    onMinus = {
                        // 최소값 제한: 최초 입찰이면 0, 아니면 1
                        addMan = (addMan - 1).coerceAtLeast(if (isFirstBid) 0 else 1)
                    },
                    onPlus  = { addMan = (addMan + 1).coerceAtMost(999_999) }
                )
            }

            Spacer(Modifier.height(16.dp))

            AppFilledButton(
                text = "상위 입찰",
                onClick = { onConfirm(proposedBid) },
                enabled = canBid,
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
    remainText: String = "",            // "1시간 15분"
    topBidderName: String?,        // 상위 입찰자 이름
    topBidderAvatarUrl: String? = null,
    onBid: () -> Unit,
    modifier: Modifier = Modifier,
    endAtMillis: Long = 0,
    bidEnabled: Boolean = true, // 추가
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
                if (endAtMillis > 0) {
                    CountdownText(endsAtMillis = endAtMillis)
                } else {
                    Text(
                        remainText,
                        color = Red_1,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
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
                    enabled = bidEnabled, // 적용
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
    val carName: String,
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
data class SellerUi(
    val name: String,
    val id: String,
    val addr: String,
    val avatarUrl: String? = null,
    val phoneNumber: String? = null
)
/* 샘플 */

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

    val d = TimeUnit.MILLISECONDS.toDays(remain.coerceAtLeast(0))
    val h = TimeUnit.MILLISECONDS.toHours(remain.coerceAtLeast(0)) % 24
    val m = TimeUnit.MILLISECONDS.toMinutes(remain.coerceAtLeast(0)) % 60
    val s = TimeUnit.MILLISECONDS.toSeconds(remain.coerceAtLeast(0)) % 60

    val text = when {
        remain <= 0L -> "종료"
        d > 0 -> "${d}일 ${h}시간 ${m}분"
        h > 0 -> "${h}시간 ${m}분"
        m >= 10 -> "${m}분"
        m > 0 -> "${m}분 ${s}초"
        else -> "${s}초"
    }

    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
        color = Red_1
    )
}

private fun parseDateTimeToMillis(dateTimeStr: String): Long {
    val formats = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm" // 추가
    )
    val simplified = if (dateTimeStr.contains(".")) {
        val parts = dateTimeStr.split(".")
        val base = parts[0]
        val decimal = parts[1].replace("Z", "").take(3)
        "$base.$decimal${if (dateTimeStr.endsWith("Z")) "Z" else ""}"
    } else {
        dateTimeStr
    }
    for (pattern in formats) {
        try {
            val sdf = java.text.SimpleDateFormat(pattern, java.util.Locale.getDefault())
            val date = sdf.parse(simplified)
            if (date != null) return date.time
        } catch (_: Exception) {}
    }
    return System.currentTimeMillis()
}





