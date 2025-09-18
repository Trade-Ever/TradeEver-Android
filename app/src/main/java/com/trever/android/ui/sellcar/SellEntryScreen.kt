package com.trever.android.ui.sellcar

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.trever.android.ui.navigation.ROUTE_SELL_FLOW

@Composable
fun SellEntryScreen(
    parentNavController: NavHostController
) {
    val cs = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "내 차 팔기",
            style = MaterialTheme.typography.headlineSmall,
            color = cs.onSurface
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "몇 단계만 거치면 쉽고 빠르게 등록할 수 있어요.",
            style = MaterialTheme.typography.bodyMedium,
            color = cs.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { parentNavController.navigate(ROUTE_SELL_FLOW) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Text("등록 시작")
        }
    }
}