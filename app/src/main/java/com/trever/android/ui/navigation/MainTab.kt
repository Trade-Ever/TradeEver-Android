package com.trever.android.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector
import com.trever.android.R

sealed class MainTab(
    val route: String,
    val label: String,
    val iconResSelected: Int,
    val iconResUnselected: Int
) {
    data object Buy : MainTab(
        "buy",
        "내차사기",
        R.drawable.buy_car_fill,       // 선택시
        R.drawable.buy_car_outlined    // 비선택시
    )
    data object Sell : MainTab(
        "sell",
        "내차팔기",
        R.drawable.cell_car_fill,
        R.drawable.cell_car_outlined
    )
    data object Auction : MainTab(
        "auction",
        "경매",
        R.drawable.auction_fill,
        R.drawable.auction_outlined
    )
    data object My : MainTab(
        "my",
        "마이페이지",
        R.drawable.mypage_fill,
        R.drawable.mypage_outlined
    )
}

val MainTabs = listOf(MainTab.Buy, MainTab.Sell, MainTab.Auction, MainTab.My)