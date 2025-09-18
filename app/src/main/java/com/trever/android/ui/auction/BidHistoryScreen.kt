package com.trever.android.ui.auction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.trever.android.ui.theme.Grey_100
import com.trever.android.ui.theme.Grey_400
import com.trever.android.ui.theme.G_100
import com.trever.android.ui.theme.G_300
import com.trever.android.ui.theme.backgroundColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BidHistoryScreen(
    carId: String,
    onBack: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val bids = remember(carId) { demoBids(count = 20) }

    Scaffold(
        containerColor = cs.backgroundColor,
        topBar = {
            TopAppBar(

                title = { Text("입찰 내역") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = cs.backgroundColor,
                    titleContentColor = cs.onSurface,
                    navigationIconContentColor = cs.onSurface
                )
            )
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)               // ← 시스템 인셋/탑바 높이 자동 반영
                .padding(horizontal = 16.dp), // 좌우 여백
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp) // 리스트 위/아래 살짝 띄움
        ) {
            items(bids) { bid -> BidHistoryRow(bid) }
        }
    }
}

@Composable
private fun BidHistoryRow(
    bid: BidUi,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val green = Color(0xFF00C364)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = cs.backgroundColor,
        tonalElevation = 1.dp,
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 아바타
            if (!bid.avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = bid.avatarUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(28.dp).clip(CircleShape)
                )
            } else {
                Surface(
                    shape = CircleShape,
                    color = Grey_100,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = bid.name.take(1),
                            style = MaterialTheme.typography.labelMedium,
                            color = cs.onSurface
                        )
                    }
                }
            }

            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(bid.name, style = MaterialTheme.typography.bodyMedium, color = cs.onSurface)
                Text(
                    bid.timeText, // "yyyy-MM-dd HH:mm"
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.G_300
                )
            }

            Text(
                bid.amountText,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = green
            )
        }
    }
}

/* ----------------- 더미/유틸 ----------------- */

private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA)

private fun won(amount: Long): String {
    val eok = amount / 100_000_000
    val man = (amount % 100_000_000) / 10_000
    return buildString {
        if (eok > 0) append("${eok}억 ")
        if (man > 0) append("${man}만원")
        if (isEmpty()) append("0원")
    }.trim()
}

private fun demoBids(count: Int = 20): List<BidUi> {
    val base = System.currentTimeMillis()
    val step = 5 * 60 * 1000L
    return (0 until count).map { i ->
        val ts = base - i * step
        BidUi(
            name = "입찰자 ${i + 1}",
            amountText = won(120_000_000L + i * 500_000L),
            timeText = sdf.format(Date(ts)),
            avatarUrl = null
        )
    }
}