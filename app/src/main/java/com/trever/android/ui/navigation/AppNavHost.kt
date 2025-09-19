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
// import androidx.navigation.compose.navigation // 현재 사용되지 않으므로 제거해도 무방
import androidx.navigation.navArgument
import com.trever.android.ui.auction.AuctionDetailScreen
// import com.trever.android.ui.auction.AuctionListScreen // AppNavHost에서 직접 사용되지 않음
import com.trever.android.ui.auction.BidHistoryScreen
import com.trever.android.ui.sellcar.SellListingScreen

// const val ROUTE_AUCTION_LIST = "auction/list" // 현재 사용되지 않음
const val ROUTE_AUCTION_DETAIL = "auction/detail/{carId}"
const val ROUTE_BID_HISTORY = "auction/bid-history/{carId}"

const val ROUTE_SELL_FLOW = "sell/flow"

@Composable
fun AppNavHost(
    navController: NavHostController, // 이 navController를 SellListingScreen에 전달
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "main", // "main"이 MainScreen을 의미
        modifier = modifier
    ) {
        // ▼ 바텀바가 있는 탭 영역 전용 화면
        composable("main") {
            MainScreen(parentNavController = navController)
        }

        // ▼ 바텀바 없는 풀스크린들
        composable(
            route = ROUTE_AUCTION_DETAIL,
            arguments = listOf(navArgument("carId") { type = NavType.StringType })
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getString("carId") ?: ""
            AuctionDetailScreen(
                carId = carId,
                onBack = { navController.popBackStack() },
                onShowBidHistory = { id ->
                    navController.navigate("auction/bid-history/$id")
                }
            )
        }

        composable(
            route = ROUTE_BID_HISTORY,
            arguments = listOf(navArgument("carId") { type = NavType.StringType })
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getString("carId") ?: ""
            BidHistoryScreen(
                carId = carId,
                onBack = { navController.popBackStack() }
            )
        }

        // 참고: ROUTE_AUCTION_DETAIL이 중복 정의된 것 같습니다. 하나는 제거해도 됩니다.
        // 만약 다른 로직이라면 그대로 두어도 괜찮습니다.
        // composable(
        //     route = ROUTE_AUCTION_DETAIL,
        //     arguments = listOf(navArgument("carId") { type = NavType.StringType })
        // ) { backStackEntry ->
        //     val carId = backStackEntry.arguments?.getString("carId") ?: ""
        //     AuctionDetailScreen(
        //         carId = carId,
        //         onBack = { navController.popBackStack() },
        //         onShowBidHistory = { id ->
        //             navController.navigate("auction/bid-history/$id")
        //         }
        //     )
        // }

        composable(ROUTE_SELL_FLOW) {
            // SellListingScreen을 호출할 때 AppNavHost의 navController를 전달합니다.
            SellListingScreen(appNavController = navController)
        }
    }
}
