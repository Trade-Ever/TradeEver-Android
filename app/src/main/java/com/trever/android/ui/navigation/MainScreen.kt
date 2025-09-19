package com.trever.android.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.trever.android.ui.auction.AuctionListScreen
import com.trever.android.ui.buy.BuyListScreen
import com.trever.android.ui.myPage.MyPageScreen
import com.trever.android.ui.sellcar.SellEntryScreen
import com.trever.android.ui.theme.backgroundColor
import com.trever.android.ui.theme.bottomBarUnselected


@Composable
fun MainScreen(
    parentNavController: NavHostController   // 상세로 갈 때 이걸 사용!
) {
    val innerNav = rememberNavController()

    Scaffold(
        bottomBar = { TreverBottomBar(innerNav) }
    ) { padding ->
        NavHost(
            navController = innerNav,
            startDestination = MainTab.Buy.route,
            modifier = Modifier.padding(padding)
        ) {
            // 탭: 구매
            composable(MainTab.Buy.route) {
                BuyListScreen()
            }

            // 탭: 판매
            composable(MainTab.Sell.route) {
                SellEntryScreen(parentNavController = parentNavController)
            }

            // 탭: 경매(리스트만) — 상세는 최상위(AppNavHost)에서!
            composable(MainTab.Auction.route) {
                AuctionListScreen(
                    onItemClick = { carId ->
                        parentNavController.navigate("auction/detail/$carId")
                    }
                )
            }

            // 탭: 마이페이지
            composable(MainTab.My.route) {
                MyPageScreen()
            }
        }
    }
}

@Composable
private fun TreverBottomBar(navController: NavHostController) {
    val cs = MaterialTheme.colorScheme
    val backStack = navController.currentBackStackEntryAsState()
    val currentRoute = backStack.value?.destination?.route

    NavigationBar(containerColor = cs.backgroundColor) {
        MainTabs.forEach { tab ->
            val selected = currentRoute == tab.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(tab.route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                    }
                },
                icon = {
                    val iconRes = if (selected) tab.iconResSelected else tab.iconResUnselected
                    Icon(painterResource(iconRes), contentDescription = tab.label)
                },
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = cs.primary,
                    selectedTextColor = cs.primary,
                    unselectedIconColor = cs.bottomBarUnselected,
                    unselectedTextColor = cs.bottomBarUnselected,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}