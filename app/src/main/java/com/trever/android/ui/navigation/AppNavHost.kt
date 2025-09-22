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
import com.trever.android.ui.auth.AuthViewModel
import com.trever.android.ui.auth.LoginScreen
import com.trever.android.ui.buy.BuyDetailScreen
import com.trever.android.ui.myPage.screens.LikedCarsScreen
import com.trever.android.ui.myPage.screens.MyAccountScreen
import com.trever.android.ui.myPage.screens.PrivacyPolicyScreen
import com.trever.android.ui.myPage.screens.RecentlyViewedCarsScreen
import com.trever.android.ui.myPage.screens.TermsScreen
import com.trever.android.ui.myPage.screens.TransactionHistoryScreen // 통합 스크린 임포트
import com.trever.android.ui.sellcar.SellListingScreen
import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel
import org.koin.androidx.compose.koinViewModel

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

const val ROUTE_AUCTION_DETAIL = "auction/detail/{carId}/{auctionId}"
const val ROUTE_BID_HISTORY = "auction/bid-history/{auctionId}"
const val ROUTE_LOGIN = "login"

@Composable
fun AppNavHost(
    navController: NavHostController,
    sellCarViewModel: SellCarViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = ROUTE_LOGIN,
        modifier = modifier
    ) {
        composable("main") {
            MainScreen(
                parentNavController = navController,
                sellCarViewModel = sellCarViewModel
            )
        }

        composable(ROUTE_LOGIN) {
            LoginScreenWrapper(
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo(ROUTE_LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = ROUTE_AUCTION_DETAIL,
            arguments = listOf(
                navArgument("carId") { type = NavType.StringType },
                navArgument("auctionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getString("carId") ?: ""
            val auctionId = backStackEntry.arguments?.getString("auctionId") ?: ""

            AuctionDetailScreen(
                carId = carId,
                auctionId = auctionId,
                onBack = { navController.popBackStack() },
                onShowBidHistory = { id ->
                    navController.navigate("auction/bid-history/$auctionId")
                }
            )
        }

        composable(
            route = ROUTE_BID_HISTORY,
            arguments = listOf(navArgument("auctionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val auctionId = backStackEntry.arguments?.getString("auctionId") ?: ""
            BidHistoryScreen(
                auctionId = auctionId,
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
                sellCarViewModel = sellCarViewModel
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
            // 판매 내역 클릭 시, 통합 스크린을 0번 탭(판매)으로 시작
            TransactionHistoryScreen(navController = navController, initialTabIndex = 0)
        }
        composable(ROUTE_MYPAGE_PURCHASE_HISTORY) {
            // 구매 내역 클릭 시, 통합 스크린을 1번 탭(구매)으로 시작
            TransactionHistoryScreen(navController = navController, initialTabIndex = 1)
        }
        composable(ROUTE_MYPAGE_TERMS) {
            TermsScreen(navController = navController)
        }
        composable(ROUTE_MYPAGE_PRIVACY_POLICY) {
            PrivacyPolicyScreen(navController = navController)
        }
    }
}

@Composable
fun LoginScreenWrapper(onLoginSuccess: () -> Unit) {
    val viewModel: AuthViewModel = koinViewModel()
    LoginScreen(
        viewModel = viewModel,
        onLoginSuccess = onLoginSuccess
    )
}
