package com.trever.android.ui.navigation


import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.trever.android.ui.theme.backgroundColor
import com.trever.android.ui.theme.bottomBarUnselected
import com.trever.android.ui.theme.primaryLight


@Composable
fun TreverApp() {
    val navController = rememberNavController()
    // 바텀바를 여기서 관리하지 않음! (MainScreen 내부로 이동)
    AppNavHost(navController = navController)
//    val navController = rememberNavController()
//    val backStackEntry by navController.currentBackStackEntryAsState()
//    val currentDestination = backStackEntry?.destination
//    val cs = MaterialTheme.colorScheme
//
//    // ★ 바텀바 숨김 대상: 상세 + 입찰내역
//    val hideBottomBar = currentDestination
//        ?.hierarchy
//        ?.any { dest ->
//            dest.route == ROUTE_AUCTION_DETAIL || dest.route == ROUTE_BID_HISTORY
//        } == true
//
//    Scaffold(
//        // 시스템 인셋은 각 화면에서 직접 처리하도록 0으로
//        contentWindowInsets = WindowInsets(0),
//        bottomBar = {
//            if (!hideBottomBar) {
//                NavigationBar(containerColor = Color.White) {
//                    MainTabs.forEach { tab ->
//                        val selected = currentDestination
//                            ?.hierarchy
//                            ?.any { it.route == tab.route } == true
//
//                        NavigationBarItem(
//                            selected = selected,
//                            onClick = {
//                                navController.navigate(tab.route) {
//                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
//                                    launchSingleTop = true
//                                    restoreState = true
//                                }
//                            },
//                            icon = {
//                                val iconRes = if (selected) tab.iconResSelected else tab.iconResUnselected
//                                Icon(painterResource(iconRes), contentDescription = tab.label)
//                            },
//                            label = { Text(tab.label) },
//                            colors = NavigationBarItemDefaults.colors(
//                                selectedIconColor = cs.primary,
//                                selectedTextColor = cs.primary,
//                                unselectedIconColor = cs.bottomBarUnselected,
//                                unselectedTextColor = cs.bottomBarUnselected,
//                                indicatorColor = Color.Transparent
//                            )
//                        )
//                    }
//                }
//            }
//        }
//    ) { padding ->
//        // bottomBar가 있을 때만 그 높이만큼만 패딩 적용
//        val bottom = if (!hideBottomBar) padding.calculateBottomPadding() else 0.dp
//        AppNavHost(
//            navController = navController,
//            modifier = Modifier.padding(bottom = bottom)
//        )
//    }
}