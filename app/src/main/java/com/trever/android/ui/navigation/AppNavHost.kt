package com.trever.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.trever.android.ui.auction.AuctionDetailScreen
import com.trever.android.ui.auction.BidHistoryScreen
import com.trever.android.ui.buy.BuyDetailScreen
import com.trever.android.ui.myPage.screens.LikedCarsScreen
import com.trever.android.ui.myPage.screens.MyAccountScreen
import com.trever.android.ui.myPage.screens.PrivacyPolicyScreen
import com.trever.android.ui.myPage.screens.PurchaseHistoryScreen
import com.trever.android.ui.myPage.screens.RecentlyViewedCarsScreen
import com.trever.android.ui.myPage.screens.SalesHistoryScreen
import com.trever.android.ui.myPage.screens.TermsScreen
import com.trever.android.ui.sellcar.SellListingScreen
import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel // ViewModel 임포트 추가

// Existing Routes
const val ROUTE_AUCTION_DETAIL = "auction/detail/{carId}"
const val ROUTE_BID_HISTORY = "auction/bid-history/{carId}"
const val ROUTE_SELL_FLOW = "sell/flow"
const val ROUTE_BUY_DETAIL = "buy/detail/{carId}"

// MyPage Sub-Screen Routes
const val ROUTE_MYPAGE_ACCOUNT = "myPage/account"
const val ROUTE_MYPAGE_RECENTLY_VIEWED = "myPage/recentlyViewed"
const val ROUTE_MYPAGE_LIKED_CARS = "myPage/likedCars"
const val ROUTE_MYPAGE_SALES_HISTORY = "myPage/salesHistory"
const val ROUTE_MYPAGE_PURCHASE_HISTORY = "myPage/purchaseHistory"
const val ROUTE_MYPAGE_TERMS = "myPage/terms"
const val ROUTE_MYPAGE_PRIVACY_POLICY = "myPage/privacyPolicy"

@Composable
fun AppNavHost(
    navController: NavHostController,
    sellCarViewModel: SellCarViewModel, // sellCarViewModel 파라미터 추가
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "main",
        modifier = modifier
    ) {
        composable("main") {
            MainScreen(
                parentNavController = navController,
                sellCarViewModel = sellCarViewModel // MainScreen에 ViewModel 전달
            )
        }

        // --- Existing Fullscreen Destinations ---
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

        composable(
            route = ROUTE_BUY_DETAIL,
            arguments = listOf(navArgument("carId") { type = NavType.StringType })
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getString("carId") ?: ""
            BuyDetailScreen(
                carId = carId,
                onBack = { navController.popBackStack() },
                onBuy = { /* 구매 처리 로직 */ }
            )
        }

        composable(ROUTE_SELL_FLOW) {
            SellListingScreen(
                appNavController = navController,
                sellCarViewModel = sellCarViewModel // SellListingScreen에 ViewModel 전달
            )
        }

        // --- MyPage Sub-Screen Destinations ---
        composable(ROUTE_MYPAGE_ACCOUNT) {
            MyAccountScreen(navController = navController)
        }
        composable(ROUTE_MYPAGE_RECENTLY_VIEWED) {
            RecentlyViewedCarsScreen(navController = navController)
        }
        composable(ROUTE_MYPAGE_LIKED_CARS) {
            LikedCarsScreen(navController = navController)
        }
        composable(ROUTE_MYPAGE_SALES_HISTORY) {
            SalesHistoryScreen(navController = navController)
        }
        composable(ROUTE_MYPAGE_PURCHASE_HISTORY) {
            PurchaseHistoryScreen(navController = navController)
        }
        composable(ROUTE_MYPAGE_TERMS) {
            TermsScreen(navController = navController)
        }
        composable(ROUTE_MYPAGE_PRIVACY_POLICY) {
            PrivacyPolicyScreen(navController = navController)
        }
    }
}
