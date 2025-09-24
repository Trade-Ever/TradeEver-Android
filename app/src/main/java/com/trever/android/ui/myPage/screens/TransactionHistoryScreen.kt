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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.trever.android.domain.model.Transaction // Transaction 모델 import
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
    navController: NavController,
    viewModel: TransactionViewModel = koinViewModel(),
    initialTabIndex: Int = 0
) {
    var selectedTabIndex by remember { mutableStateOf(initialTabIndex) }
    val tabs = listOf("판매", "구매")

    val transactions by viewModel.transactions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

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
                containerColor = MaterialTheme.colorScheme.cardBackgroundColor,
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
                TransactionList(transactions = transactions)
            }
        }
    }
}

@Composable
private fun TransactionList(transactions: List<Transaction>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(transactions, key = { it.transactionId }) { transaction ->
            TransactionListItem(transaction = transaction)
        }
    }
}

// 가격 포맷 함수 (예: 11230000L -> "1,123만원")
fun formatPriceToManwon(price: Long?): String {
    if (price == null) return "-"
    val manwon = price / 10000
    val formatter = DecimalFormat("#,###")
    return "${formatter.format(manwon)}만원"
}

// 날짜 포맷 함수 (예: "2023-09-24T10:00:00" -> "09/24")
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
private fun TransactionListItem(transaction: Transaction) {
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
                    onClick = { /* TODO: PDF 보기 로직 */ },
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

// Preview for individual list item
// @Preview(showBackground = true, backgroundColor = 0xFFF0F0F0)
// @Composable
// fun TransactionListItemPreview() {
//    // 실제 Transaction 모델을 사용할 수 없으므로, 프리뷰용 가짜 데이터 생성
//    // 이제 userName을 거래상대방으로 사용하므로, counterpartyName 필드는 샘플에서 제거하거나 userName으로 통일합니다.
//    val sampleTransaction = Transaction(
//        transactionId = "1",
//        vehicleId = 100,
//        sellerId = 1,
//        buyerId = 2,
//        price = 11230000L,
//        finalPrice = 11230000L,
//        status = "COMPLETED", // status는 현재 UI에 표시되지 않음
//        createdAt = "2023-09-24T10:00:00Z",
//        updatedAt = "2023-09-24T10:00:00Z",
//        vehicleName = "G80",
//        userName = "채상윤", // 이 필드가 거래 상대방 이름으로 사용됨
//        // 나머지 Transaction 필드들... (실제 모델에 맞게 추가/수정 필요)
//        auctionId = null,
//        bidId = null,
//        deliveryAddress = null,
//        deliveryAddressDetail = null,
//        deliveryCharge = null,
//        deliveryRequest = null,
//        deliveryStatus = null,
//        deliveryTrackingNumber = null,
//        isSellerReviewed = false,
//        isBuyerReviewed = false,
//        bankName = null,
//        accountNumber = null
//    )
//    AppTheme {
//        TransactionListItem(transaction = sampleTransaction)
//    }
// }
