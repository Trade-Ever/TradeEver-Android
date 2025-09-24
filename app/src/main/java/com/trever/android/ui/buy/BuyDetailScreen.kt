package com.trever.android.ui.buy


import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.trever.android.data.remote.toBuyDetailUi
import com.trever.android.ui.auction.SellerUi
import com.trever.android.ui.components.AppFilledButton
import com.trever.android.ui.components.AppOutlinedButton
import com.trever.android.ui.theme.G_200
import com.trever.android.ui.theme.backgroundColor

@Composable
fun BuyDetailScreen(
    carId: String,
    viewModel: BuyDetailViewModel = viewModel(),
    onBack: () -> Unit = {},
    onLike: () -> Unit = {},
    onInquiry: () -> Unit = {},
    onBuy: () -> Unit = {},
    navController: NavHostController,
) {
    val uiState by viewModel.uiState.collectAsState()
    var showInquirySheet by remember { mutableStateOf(false) }



    // 화면 진입시 데이터 로드
    LaunchedEffect(carId) {
        viewModel.loadVehicleDetail(carId)
    }

    var showBuySheet by remember { mutableStateOf(false) }
    var showBuyerSelectSheet by remember { mutableStateOf(false) }
    val buyerList by viewModel.buyerList.collectAsState()
    var showContractSheet by remember { mutableStateOf(false) }
    var contractIdForSheet by remember { mutableStateOf<Long?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()

        ) {
            when (val state = uiState) {
                is BuyDetailUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is BuyDetailUiState.Success -> {
                    // API 응답 데이터를 UI 모델로 변환
                    val isSeller = state.vehicle.isSeller
                    val isSoldOut = state.vehicle.vehicleStatus == "판매완료"
                    val detailUi = state.vehicle.toBuyDetailUi()
                    val sellerPhone = detailUi.seller?.phoneNumber ?: ""

                    DetailContent(
                        item = detailUi,
                        onBack = onBack,
                        badge = { SellingBadge() },  // 구매 뱃지
                        showBidSection = false,  // 입찰 섹션 표시 안 함
                        onMoreBids = null,       // 입찰 내역 보기 기능 비활성화
                        bottomBar = {
                            if (isSoldOut) {
                                // 판매완료 표시만
                                SoldOutBottomActionBar()
                            }
                            else if (isSeller) {
                                SellerBottomActionBar(
                                    onComplete = {
                                        viewModel.loadBuyRequests(carId)
                                        showBuyerSelectSheet = true
                                    }
                                )
                            } else {
                                BuyBottomActionBar(
                                    price = detailUi.priceWonText,
                                    onBuy = { showBuySheet = true },
                                    onInquiry = { showInquirySheet = true }
                                )
                            }
                        },
                        onToggleLike = { // 1) VM에 토글 수행
                            viewModel.toggleLike(carId)

                            // 2) 현재 UI 상태 기반으로 "토글 후" 값을 계산해서 결과 전달

                            }
                    )
                    if (showInquirySheet) {
                        InquirySheet(
                            sellerPhone = sellerPhone,
                            onDismiss = { showInquirySheet = false }
                        )
                    }

                    if (showBuySheet) {
                        val context = LocalContext.current

                        BuyConfirmSheet(
                            onConfirm = {
                                viewModel.applyBuy(carId) { success, message ->
                                    Toast.makeText(
                                        context,
                                        if (success) "신청 성공: $message" else "신청 실패: $message",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    Log.d("BuyDetailScreen", "Buy application result: $message")
                                }
                                showBuySheet = false
                            },
                            onDismiss = { showBuySheet = false }
                        )
                    }
//                    // 3. 바텀시트(구매자 선택)
//                    if (showBuyerSelectSheet) {
//                        val context = LocalContext.current
//                        BuyerSelectSheet(
//                            buyers = buyerList.map { it.buyerName },
//                            onSelect = { selectedBuyerName ->
//                                val selectedBuyer = buyerList.find { it.buyerName == selectedBuyerName }
//                                selectedBuyer?.let {
//                                    viewModel.selectBuyer(carId, it.buyerId) { success, message, response ->
//                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
//                                        Toast.makeText(context, "contractId: ${response?.contractId}", Toast.LENGTH_SHORT).show()
//                                        if (success && response?.contractId != null) {
//                                            navController.navigate("contracts/${response.contractId}")
//                                        }
//                                        // 필요시 showBuyerSelectSheet = false
//                                    }
//                                }
//                                showBuyerSelectSheet = false
//                            },
//                            onDismiss = { showBuyerSelectSheet = false }
//                        )
//                    }

                    if (showBuyerSelectSheet) {
                        val context = LocalContext.current
                        BuyerSelectSheet(
                            buyers = buyerList.map { it.buyerName },
                            onSelect = { selectedBuyerName ->
                                val selectedBuyer = buyerList.find { it.buyerName == selectedBuyerName }
                                selectedBuyer?.let {
                                    viewModel.selectBuyer(carId, it.buyerId) { success, message, response ->
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                        if (success && response?.contractId != null) {
                                            // ⬇️ 여기서 네비게이션 대신 바텀시트 오픈
                                            contractIdForSheet = response.contractId
                                            showContractSheet = true
                                        }
                                    }
                                }
                                showBuyerSelectSheet = false
                            },
                            onDismiss = { showBuyerSelectSheet = false }
                        )
                    }

                    if (showContractSheet && contractIdForSheet != null) {
                        ContractBottomSheet(
                            open = showContractSheet,
                            contractId = contractIdForSheet!!,
                            onDismissRequest = {
                                showContractSheet = false
                                contractIdForSheet = null
                                // 필요하면 목록 새로고침 등 후처리
                                // viewModel.refresh()
                            }
                        )
                    }
                }

                is BuyDetailUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(state.message)
                    }
                }
            }
        }
    }


}

@Composable
private fun SoldOutBottomActionBar(
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
            AppOutlinedButton(
                text = "판매완료",
                onClick = {},
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


@Composable
private fun SellerBottomActionBar(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {


    val cs = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = cs.backgroundColor,
        contentColor = cs.onSurface,
        tonalElevation = 2.dp,
        shadowElevation = 12.dp
    ) { Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        AppOutlinedButton(
            text = "판매완료로 변경하기",
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()

        )
    }}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BuyerSelectSheet(
    buyers: List<String>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val (selected, setSelected) = remember { mutableStateOf<String?>(null) }
    val cs = MaterialTheme.colorScheme

    ModalBottomSheet(
        containerColor = cs.backgroundColor,
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("구매자를 선택하세요", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                buyers.forEach { buyer ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(1f)
                            .padding(vertical = 6.dp)
                            .clickable { setSelected(buyer) }
                            .background(
                                if (selected == buyer) cs.primary.copy(alpha = 0.1f) else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = buyer,
                            color = if (selected == buyer) cs.primary else cs.G_200
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            AppFilledButton(
                text = "구매자 선택하기",
                onClick = { selected?.let { onSelect(it) } },
                enabled = selected != null,
                modifier = Modifier.fillMaxWidth()
            )
        }
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

//@OptIn(ExperimentalMaterial3Api::class)
/*@Composable
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
}*/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InquirySheet(
    sellerPhone: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val cs = MaterialTheme.colorScheme

    ModalBottomSheet(
        containerColor = cs.backgroundColor,
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("문의 방법 선택", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            AppFilledButton(
                text = "전화 문의",
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$sellerPhone")
                    }
                    context.startActivity(intent)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            AppOutlinedButton(
                text = "문자 문의",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("sms:$sellerPhone")
                    }
                    context.startActivity(intent)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BuyConfirmSheet(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val cs = MaterialTheme.colorScheme

    ModalBottomSheet(
        containerColor = cs.backgroundColor,
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("구매 신청", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))


            AppFilledButton(
                text = "신청하기",
                onClick = {
                    onConfirm()
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            AppOutlinedButton(
                text = "취소",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
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
