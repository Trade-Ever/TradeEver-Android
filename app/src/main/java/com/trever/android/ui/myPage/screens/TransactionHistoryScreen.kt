package com.trever.android.ui.myPage.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.trever.android.domain.model.Transaction
import com.trever.android.ui.myPage.TransactionType
import com.trever.android.ui.myPage.TransactionViewModel
import com.trever.android.ui.theme.AppTheme
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    navController: NavController,
    viewModel: TransactionViewModel = koinViewModel(),
    initialTabIndex: Int = 0 // 시작 탭을 외부에서 지정할 수 있도록 파라미터 추가
) {
    var selectedTabIndex by remember { mutableStateOf(initialTabIndex) } // 파라미터로 초기 상태 지정
    val tabs = listOf("판매", "구매")

    val transactions by viewModel.transactions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // 탭이 변경될 때마다 해당 내역을 로드합니다.
    LaunchedEffect(selectedTabIndex) {
        val type = if (selectedTabIndex == 0) TransactionType.SALES else TransactionType.PURCHASES
        viewModel.loadTransactions(type)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("거래 내역", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로 가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF0F0F0)
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
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
                                // isSelected 값에 따라 텍스트 색상을 동적으로 변경
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black
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
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(transactions, key = { it.transactionId }) { transaction ->
            TransactionListItem(transaction = transaction)
        }
    }
}

@Composable
private fun TransactionListItem(transaction: Transaction) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("차량명: ${transaction.vehicleName}", fontWeight = FontWeight.Bold)
            Text("거래 금액: ${transaction.finalPrice}원")
            Text("거래 상태: ${transaction.status}")
            Text("거래일: ${transaction.createdAt}")
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
