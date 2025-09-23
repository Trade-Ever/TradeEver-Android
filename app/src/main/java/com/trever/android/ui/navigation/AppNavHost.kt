package com.trever.android.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
// import androidx.navigation.compose.navigation // 현재 사용되지 않으므로 제거해도 무방
import androidx.navigation.navArgument
import com.trever.android.data.remote.toAuctionCar
import com.trever.android.data.remote.toSearchCarItem
import com.trever.android.ui.auction.AuctionDetailScreen
// import com.trever.android.ui.auction.AuctionListScreen // AppNavHost에서 직접 사용되지 않음
import com.trever.android.ui.auction.BidHistoryScreen
import com.trever.android.ui.auth.AuthViewModel
import com.trever.android.ui.auth.LoginScreen
import com.trever.android.ui.auth.ProfileInputScreen
import com.trever.android.ui.buy.BuyDetailScreen
import com.trever.android.ui.myPage.screens.LikedCarsScreen
import com.trever.android.ui.myPage.screens.MyAccountScreen
import com.trever.android.ui.myPage.screens.PrivacyPolicyScreen
import com.trever.android.ui.myPage.screens.PurchaseHistoryScreen
import com.trever.android.ui.myPage.screens.RecentlyViewedCarsScreen
import com.trever.android.ui.myPage.screens.SalesHistoryScreen
import com.trever.android.ui.myPage.screens.TermsScreen
import com.trever.android.ui.search.SearchResultScreen
import com.trever.android.ui.search.SearchScreen
import com.trever.android.ui.search.SearchSelectCarModelScreen
import com.trever.android.ui.search.SearchSelectCarNameScreen
import com.trever.android.ui.search.SearchSelectManufacturerScreen
import com.trever.android.ui.search.SearchViewModel
import org.koin.androidx.compose.koinViewModel

import com.trever.android.ui.sellcar.SellListingScreen
import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel // ViewModel 임포트 추가

// const val ROUTE_AUCTION_LIST = "auction/list" // 현재 사용되지 않음
//const val ROUTE_AUCTION_DETAIL = "auction/detail/{carId}"
//const val ROUTE_BID_HISTORY = "auction/bid-history/{carId}"

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
// 입찰 내역 경로도 함께 수정
const val ROUTE_BID_HISTORY = "auction/bid-history/{auctionId}"

const val ROUTE_LOGIN = "login"

const val PROFILE_INPUT = "profile_input"

const val ROUTE_SEARCH = "search"

@Composable
fun AppNavHost(
    navController: NavHostController,
    sellCarViewModel: SellCarViewModel, // sellCarViewModel 파라미터 추가
    modifier: Modifier = Modifier
) {
    val searchViewModel: SearchViewModel = koinViewModel()
    NavHost(
        navController = navController,
        startDestination = ROUTE_LOGIN, // "main"이 MainScreen을 의미
        modifier = modifier
    ) {
        // ▼ 바텀바가 있는 탭 영역 전용 화면
        composable("main") {
            MainScreen(
                parentNavController = navController,
                sellCarViewModel = sellCarViewModel // MainScreen에 ViewModel 전달
            )
        }

        composable(ROUTE_SEARCH) {
            SearchScreen(
                onShowResults = { navController.navigate("search/results") },
                viewModel = searchViewModel,
                onFilterClick = { filterType ->
                    if (filterType == "model") {
                        navController.navigate("search/selectManufacturer")
                    }
                },
                onBack = { navController.popBackStack() } // 추가
            )
        }



        composable(
            route = "search?manufacturer={manufacturer}&carName={carName}&carModel={carModel}",
            arguments = listOf(
                navArgument("manufacturer") { nullable = true; defaultValue = "" },
                navArgument("carName") { nullable = true; defaultValue = "" },
                navArgument("carModel") { nullable = true; defaultValue = "" }
            )
        ) { backStackEntry ->



            SearchScreen(
                viewModel = searchViewModel,
                onShowResults = { navController.navigate("search/results") },
                onFilterClick = { filterType ->
                    if (filterType == "model") {
                        navController.navigate("search/selectManufacturer")
                    }
                },
                onBack = { navController.popBackStack() } // 추가
            )
        }

        composable("search/selectManufacturer") {

            SearchSelectManufacturerScreen(
                viewModel = searchViewModel,
                onSystemBack = { navController.popBackStack() },
                onManufacturerSelected = { manufacturer ->
                    searchViewModel.selectedManufacturer.value = manufacturer
                    searchViewModel.selectedCarName.value = null
                    searchViewModel.selectedCarModel.value = null
                    navController.navigate("search/selectCarName/$manufacturer")
                }
            )
        }

        composable("search/selectCarModel/{manufacturer}/{carName}") { backStackEntry ->
            val manufacturer = backStackEntry.arguments?.getString("manufacturer") ?: ""
            val carName = backStackEntry.arguments?.getString("carName") ?: ""

            SearchSelectCarModelScreen(
                viewModel = searchViewModel,
                manufacturer = manufacturer,
                carName = carName,
                onSystemBack = { navController.popBackStack() },
                onCarModelSelected = { carModel ->
                    searchViewModel.selectedCarModel.value = carModel
                    navController.navigate("search") {
                        popUpTo("search") { inclusive = true }
                    }
                }
            )
        }

        composable(ROUTE_LOGIN) {
            val viewModel: AuthViewModel = koinViewModel()
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    if (viewModel.profileComplete.value == false) {
                        navController.navigate(PROFILE_INPUT) {
                            popUpTo(ROUTE_LOGIN) { inclusive = true }
                        }
                    } else {
                        navController.navigate("main") {
                            popUpTo(ROUTE_LOGIN) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable("search/selectCarName/{manufacturer}") { backStackEntry ->
            val manufacturer = backStackEntry.arguments?.getString("manufacturer") ?: ""

            SearchSelectCarNameScreen(
                viewModel = searchViewModel,
                manufacturer = manufacturer,
                onSystemBack = { navController.popBackStack() },
                onCarNameSelected = { carName ->
                    searchViewModel.selectedCarName.value = carName
                    searchViewModel.selectedCarModel.value = null
                    navController.navigate("search/selectCarModel/$manufacturer/$carName")
                }
            )
        }

        composable("search/results") {
            val searchResult by searchViewModel.searchResult.collectAsState()
            val cars = searchResult?.vehicles?.map { it.toSearchCarItem() } ?: emptyList()
            val yearRange by searchViewModel.yearRange.collectAsState()
            val distanceRange by searchViewModel.distanceRange.collectAsState()
            val priceRange by searchViewModel.priceRange.collectAsState()
            val selectedType by searchViewModel.selectedType.collectAsState()

            SearchResultScreen(
                viewModel = searchViewModel,
                cars = cars,
                onBack = { navController.popBackStack() },
                onCarClick = { /* 상세 이동 */ },
                onToggleLike = { /* 찜 처리 */ },
                selectedPriceRange = "",
                onPriceRangeClick = { /* 바텀시트 등 구현 */ },
                selectedDistance = "",
                onDistanceClick = { /* 바텀시트 등 구현 */ },
                selectedSort = "",
                onSortClick = { /* 정렬 바텀시트 등 구현 */ },
                yearRange = yearRange,
                distanceRange = distanceRange,
                priceRange = priceRange,
                selectedType = selectedType,
                onYearRangeClick = { /* 바텀시트 등 구현 */ },
                onTypeClick = { /* 바텀시트 등 구현 */ }
            )
        }

        // ▼ 바텀바 없는 풀스크린들
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

//        composable(
//            route = ROUTE_AUCTION_DETAIL,
//            arguments = listOf(navArgument("carId") { type = NavType.StringType })
//        ) { backStackEntry ->
//            val carId = backStackEntry.arguments?.getString("carId") ?: ""
//            AuctionDetailScreen(
//                carId = carId,
//                onBack = { navController.popBackStack() },
//                onShowBidHistory = { id ->
//                    navController.navigate("auction/bid-history/$id")
//                }
//            )
//        }

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

        composable(PROFILE_INPUT) {
            val viewModel: AuthViewModel = koinViewModel()
            ProfileInputScreen(
                viewModel = viewModel,
                onComplete = {
                    navController.navigate("main") {
                        popUpTo("profile_input") { inclusive = true }
                    }
                }
            )
        }

    }
}

@Composable
fun LoginScreenWrapper(onLoginSuccess: () -> Unit) {
    // Koin을 사용하는 경우
    val viewModel: AuthViewModel = koinViewModel()
    // 또는 Hilt를 사용하는 경우
    // val viewModel: AuthViewModel = hiltViewModel()

    LoginScreen(
        viewModel = viewModel,
        onLoginSuccess = onLoginSuccess
    )
}
