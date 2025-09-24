package com.trever.android.ui.buy

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalConfiguration

import com.trever.android.ui.components.AppFilledButton
import com.trever.android.ui.components.AppOutlinedButton
import com.trever.android.ui.components.ZoomImage
import com.trever.android.R

import com.trever.android.ui.components.ZoomImage


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ContractBottomSheet(
    open: Boolean,
    onDismissRequest: () -> Unit,
    contractId: Long,
    vm: ContractSheetViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    val ctx = LocalContext.current
    val density = LocalDensity.current
    val cfg = LocalConfiguration.current
    val maxHeight = (cfg.screenHeightDp * 0.9f).dp

    LaunchedEffect(open, contractId) {
        if (open) vm.load(contractId)
    }

    if (!open) return

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden })


    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxHeight)          // 핵심 ①: 시트 높이 제한
                .verticalScroll(  rememberScrollState()) // 핵심 ②: 내부 스크롤
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // 헤더 (중앙 타이틀, 우측 닫기)
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(Modifier.width(48.dp))
                Text("거래 완료", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                AssistChip(onClick = onDismissRequest, label = { Text("닫기") })
            }

            Spacer(Modifier.height(8.dp))

            // 초록 체크 + 메세지
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF18C37D),
                    modifier = Modifier.size(56.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text("거래가 완료되었습니다!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))

            when (val s = state) {
                is ContractSheetState.Loading -> Box(
                    Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                is ContractSheetState.Error -> Text("오류: ${s.message}", color = MaterialTheme.colorScheme.error)

                is ContractSheetState.Ready -> {
                    // 요약 카드
                    SummaryCard(s.summary)

                    Spacer(Modifier.height(16.dp))

                    // PDF 페이지 (가로 페이저 + 핀치줌)
                    val pageCount = s.pageCount
                    val pagerState = rememberPagerState { pageCount }
                    val widthPx = with(density) { ctx.resources.displayMetrics.widthPixels }

                    Text("계약서", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))

                    HorizontalPager(
                        state = pagerState,
                        pageSpacing = 12.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 320.dp)
                    ) { page ->
                        var bmp by remember(page) { mutableStateOf<android.graphics.Bitmap?>(null) }
                        LaunchedEffect(page, widthPx) {
                            vm.getOrRenderPage(page, widthPx) { rendered -> bmp = rendered }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.large)
                                .background(Color(0xFFF4F6F8))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (bmp == null) {
                                LinearProgressIndicator(Modifier.fillMaxWidth(0.5f))
                            } else {
                                ZoomImage(bitmap = bmp!!, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // 버튼들
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                        AppFilledButton(
                            onClick = {
                                vm.downloadToDownloads { ok ->
                                    Toast.makeText(
                                        ctx,
                                        if (ok) "다운로드 완료 (Downloads 폴더)" else "다운로드 실패",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),

                            text = "PDF 다운로드",
                            leadingIconRes = R.drawable.filearrowdown// 아이콘 추가
                        )
                        AppOutlinedButton(

                            onClick = {
                                vm.confirm(contractId) { ok ->
                                    if (ok) onDismissRequest()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),

                            text = "확인"
                        )

                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SummaryCard(summary: ContractSummaryUi) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(Color(0xFFF7F8FA))
            .padding(vertical = 12.dp)
    ) {

        SummaryRow("계약서 ID", "${summary.contractId}")
        SummaryRow("거래 ID", "${summary.transactionId}")
        SummaryRow("구매자", summary.buyerName)
        SummaryRow("판매자", summary.sellerName)
        SummaryRow("서명 일시", summary.signedAtText)
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color(0xFF9AA3AE))
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}