package com.trever.android.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.trever.android.ui.auction.AuctionDetailScreen
import com.trever.android.ui.auction.AuctionListScreen
import com.trever.android.ui.buy.BuyDetailScreen
import com.trever.android.ui.sellcar.SellListingScreen

private const val ROUTE_AUCTION_LIST = "auction/list"
const val ROUTE_AUCTION_DETAIL = "auction/detail/{carId}"

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = MainTab.Buy.route,
        modifier = modifier
    ) {
        composable(MainTab.Buy.route)  { BuyDetailScreen() }
        composable(MainTab.Sell.route) { SellListingScreen() }

        // ✅ 경매 탭을 중첩 그래프로 구성
        navigation(
            startDestination = ROUTE_AUCTION_LIST,
            route = MainTab.Auction.route
        ) {
            composable(ROUTE_AUCTION_LIST) {
                AuctionListScreen(
                    onItemClick = { carId ->
                        navController.navigate("auction/detail/$carId")
                    }
                )
            }
            composable(
                ROUTE_AUCTION_DETAIL,
                arguments = listOf(navArgument("carId") { type = NavType.StringType })
            ) { backStackEntry ->
                val carId = backStackEntry.arguments?.getString("carId") ?: ""
                AuctionDetailScreen(
                    // 필요하면 carId 파라미터 추가해 써도 됨 (아래 2) 참고)
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(MainTab.My.route) { BuyDetailScreen() }
    }
}
/** 나중에 각 탭의 실제 화면으로 교체하면 됩니다. */
@Composable
private fun PlaceholderScreen(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.titleLarge)
    }
}