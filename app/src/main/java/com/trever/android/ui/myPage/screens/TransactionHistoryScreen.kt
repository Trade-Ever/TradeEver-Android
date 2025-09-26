package com.trever.android.ui.myPage.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment // PDF 아이콘으로 사용
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.trever.android.domain.model.Transaction // Transaction 모델 import
import com.trever.android.ui.buy.ContractBottomSheet // ContractBottomSheet import
import com.trever.android.ui.myPage.TransactionType
import com.trever.android.ui.myPage.TransactionViewModel
import com.trever.android.ui.theme.AppTheme
import com.trever.android.ui.theme.backgroundColor
import com.trever.android.ui.theme.cardBackgroundColor
import com.trever.android.ui.theme.textPrimaryColor
import com.trever.android.ui.theme.textSecondaryColor
import org.koin.androidx.compose.koinViewModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    navController: NavHostController,
    viewModel: TransactionViewModel = koinViewModel(),
    initialTabIndex: Int = 0
) {
    var selectedTabIndex by remember { mutableStateOf(initialTabIndex) }
    val tabs = listOf("판매", "구매")

    val transactions by viewModel.transactions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showContractSheet by remember { mutableStateOf(false) }
    var currentContractIdForSheet by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(selectedTabIndex) {
        val type = if (selectedTabIndex == 0) TransactionType.SALES else TransactionType.PURCHASES
        viewModel.loadTransactions(type)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("거래 내역", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.textPrimaryColor) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로 가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.backgroundColor)
            )
        },
        containerColor = MaterialTheme.colorScheme.backgroundColor

    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.backgroundColor,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTabIndex == index
                    Tab(
                        selected = isSelected,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.textPrimaryColor
                            )
                        }
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (transactions.isEmpty()) {
                EmptyState(message = if(selectedTabIndex == 0) "판매 내역이 없습니다." else "구매 내역이 없습니다.")
            } else {
                TransactionList(
                    transactions = transactions, 
                    selectedTabIndex = selectedTabIndex, 
                    onPdfViewClick = { contractId ->
                        currentContractIdForSheet = contractId
                        showContractSheet = true
                    }
                )
            }
        }
    }

    if (showContractSheet && currentContractIdForSheet != null) {
        ContractBottomSheet(
            open = true, 
            onDismissRequest = { showContractSheet = false }, 
            contractId = currentContractIdForSheet!!
        )
    }
}

@Composable
private fun TransactionList(
    transactions: List<Transaction>,
    selectedTabIndex: Int,
    onPdfViewClick: (contractId: Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(transactions, key = { it.transactionId }) { transaction ->
            TransactionListItem(
                transaction = transaction, 
                selectedTabIndex = selectedTabIndex, 
                onPdfViewClick = onPdfViewClick
            )
        }
    }
}

// 가격 포맷 함수
fun formatPriceToManwon(price: Long?): String {
    if (price == null) return "-"
    val manwon = price / 10000
    val formatter = DecimalFormat("#,###")
    return "${formatter.format(manwon)}만원"
}

// 날짜 포맷 함수
fun formatDateToMMdd(dateString: String?): String {
    if (dateString.isNullOrBlank()) return "-"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = parser.parse(dateString)
        SimpleDateFormat("MM/dd", Locale.getDefault()).format(date!!)
    } catch (e: Exception) {
        if (dateString.length >= 10) dateString.substring(5,10).replace("-","/") else "-"
    }
}

@Composable
private fun TransactionListItem(
    transaction: Transaction,
    selectedTabIndex: Int,
    onPdfViewClick: (contractId: Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.cardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.vehicleName ?: "차량명 없음",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                    color = MaterialTheme.colorScheme.textPrimaryColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                val counterpartyDisplayName = if (selectedTabIndex == 0) { // 판매 탭
                    transaction.buyerName ?: "-"
                } else { // 구매 탭
                    transaction.sellerName ?: "-"
                }
                Text(
                    text = "거래 상대: ${transaction.buyerName ?: "-"}", // counterpartyName 대신 userName 사용
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.textSecondaryColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatDateToMMdd(transaction.createdAt),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.textSecondaryColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatPriceToManwon(transaction.finalPrice),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00897B), 
                        fontSize = 18.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp)) 
                OutlinedButton(
                    onClick = {
                        // contractId가 Long 타입이고 non-null이라고 가정 (Transaction DTO/모델에 맞게)
                        // 만약 Transaction 모델에서 contractId가 nullable (Long?)이라면,
                        // transaction.contractId?.let { id -> onPdfViewClick(id) } 와 같이 호출합니다.
                        // 현재 Transaction 모델에 contractId가 Long (non-null)으로 되어있다고 가정합니다.
                        if (transaction.contractId != 0L) { // contractId가 유효한 경우 (0이 아니라고 가정)
                           onPdfViewClick(transaction.contractId)
                        } else {
                            // contractId가 없는 경우 또는 유효하지 않은 경우 처리 (예: Toast 메시지)
                            // 이 부분은 필요시 ViewModel을 통해 Toast를 표시하거나 다른 방식으로 처리하는 것이 좋음
                        }
                    },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    border = BorderStroke(1.dp, Color.LightGray)
                ) {
                    Text("PDF 보기", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = "PDF 보기", modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}


@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}


@Preview(showBackground = true)
@Composable
fun TransactionHistoryScreenPreview() {
    AppTheme {
        TransactionHistoryScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF0F0F0)
@Composable
fun TransactionListItemPreview() {
    val sampleTransactionForSale = Transaction(
        transactionId = 1L,
        vehicleId = 100L,
        vehicleName = "G80",
        buyerName = "채상윤 (구매자)",
        sellerName = "김판매 (판매자)", 
        finalPrice = 11230000L,
        status = "COMPLETED",
        createdAt = "2023-09-24T10:00:00Z",
        contractPdfUrl = "dummy_url.pdf",
        contractId = 1001L 
    )
    val sampleTransactionForPurchase = Transaction(
        transactionId = 2L,
        vehicleId = 101L,
        vehicleName = "팰리세이드",
        buyerName = "박구매 (구매자)",
        sellerName = "이판매 (판매자)",
        finalPrice = 8500000L,
        status = "COMPLETED",
        createdAt = "2023-09-22T14:30:00Z",
        contractPdfUrl = "dummy_url2.pdf",
        contractId = 1002L
    )
    AppTheme {
        Column {
            TransactionListItem(
                transaction = sampleTransactionForSale, 
                selectedTabIndex = 0, // 판매 탭
                onPdfViewClick = { contractId -> println("Preview: PDF View Clicked for $contractId") }
            )
            Spacer(Modifier.height(10.dp))
            TransactionListItem(
                transaction = sampleTransactionForPurchase, 
                selectedTabIndex = 1, // 구매 탭
                onPdfViewClick = { contractId -> println("Preview: PDF View Clicked for $contractId") }
            )
        }
    }
}
