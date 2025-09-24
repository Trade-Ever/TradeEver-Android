package com.trever.android.ui.navigation

import SearchResultScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.trever.android.data.remote.toSearchCarItem
import com.trever.android.domain.model.SearchCarItem
import com.trever.android.ui.auction.AuctionDetailScreen
import com.trever.android.ui.auction.BidHistoryScreen
import com.trever.android.ui.auth.AuthViewModel
import com.trever.android.ui.auth.LoginScreen
import com.trever.android.ui.auth.ProfileInputScreen
import com.trever.android.ui.buy.BuyDetailScreen
import com.trever.android.ui.buy.ContractScreen
//import com.trever.android.ui.main.MainScreen
import com.trever.android.ui.myPage.screens.MyAccountScreen
//import com.trever.android.ui.main.MainScreen
//import com.trever.android.ui.myPage.screens.MyAccountScreen
import com.trever.android.ui.myPage.screens.PrivacyPolicyScreen
import com.trever.android.ui.myPage.screens.RecentlyViewedCarsScreen
import com.trever.android.ui.myPage.screens.TermsScreen
import com.trever.android.ui.myPage.screens.TransactionHistoryScreen

import com.trever.android.ui.search.SearchScreen
import com.trever.android.ui.search.SearchSelectCarModelScreen
import com.trever.android.ui.search.SearchSelectCarNameScreen
import com.trever.android.ui.search.SearchSelectManufacturerScreen
import com.trever.android.ui.search.SearchViewModel
import com.trever.android.ui.sellcar.SellListingScreen
import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel
import org.koin.androidx.compose.koinViewModel

const val ROUTE_SELL_FLOW = "sell/flow"
const val ROUTE_BUY_DETAIL = "buy/detail/{carId}"

// MyPage Sub-Screen Routes
const val ROUTE_MYPAGE_ACCOUNT = "myPage/account"
const val ROUTE_MYPAGE_RECENTLY_VIEWED = "myPage/recentlyViewed/{initialTabIndex}" // 탭 인덱스 인자 추가
const val ROUTE_MYPAGE_SALES_HISTORY = "myPage/salesHistory"
const val ROUTE_MYPAGE_PURCHASE_HISTORY = "myPage/purchaseHistory"
const val ROUTE_MYPAGE_TERMS = "myPage/terms"
const val ROUTE_MYPAGE_PRIVACY_POLICY = "myPage/privacyPolicy"

const val ROUTE_AUCTION_DETAIL = "auction/detail/{carId}/{auctionId}"
const val ROUTE_BID_HISTORY = "auction/bid-history/{auctionId}"
const val ROUTE_LOGIN = "login"

const val PROFILE_INPUT = "profile_input"

const val ROUTE_SEARCH = "search"

const val ROUTE_CONTRACT = "contract/{contractPdfUrl}"

@Composable
fun AppNavHost(
    navController: NavHostController,
    sellCarViewModel: SellCarViewModel,
    modifier: Modifier = Modifier
) {
    val searchViewModel: SearchViewModel = koinViewModel()
    NavHost(
        navController = navController,
        startDestination = ROUTE_LOGIN,
        modifier = modifier
    ) {
        composable("main") {
            MainScreen(
                parentNavController = navController
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
                onBack = { navController.popBackStack() }, // 추가
                onClearRecent = { keyword ->
                    searchViewModel.deleteRecentSearch(keyword)
                },
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
                onCarClick = { car ->
                    when (car) {
                        is SearchCarItem.Auction -> navController.navigate("auction/detail/${car.id}/${car.auctionId}")
                        is SearchCarItem.General -> navController.navigate("buy/detail/${car.id}")
                    }
                },
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
                navController = navController,
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
                onBuy = { /* 구매 처리 로직 */ },
                navController = navController
            )
        }

        composable("contracts/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toLongOrNull() ?: return@composable
            ContractScreen(
                navController = navController,
                contractId = id
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
        composable(
            route = ROUTE_MYPAGE_RECENTLY_VIEWED,
            arguments = listOf(navArgument("initialTabIndex") {
                type = NavType.IntType
                defaultValue = 0
            })
        ) { backStackEntry ->
            val initialTabIndex = backStackEntry.arguments?.getInt("initialTabIndex") ?: 0
            RecentlyViewedCarsScreen(
                navController = navController,
                initialTabIndex = initialTabIndex
            )
        }
        composable(ROUTE_MYPAGE_SALES_HISTORY) {
            TransactionHistoryScreen(navController = navController, initialTabIndex = 0)
        }
        composable(ROUTE_MYPAGE_PURCHASE_HISTORY) {
            TransactionHistoryScreen(navController = navController, initialTabIndex = 1)
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
