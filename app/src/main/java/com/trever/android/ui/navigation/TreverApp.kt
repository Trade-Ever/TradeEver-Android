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
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination   // ✅ 현재 destination 자체

    val isAuctionDetail = currentDestination
        ?.hierarchy
        ?.any { it.route == ROUTE_AUCTION_DETAIL } == true

    val hideBottomBarRoutes = setOf(ROUTE_AUCTION_DETAIL)
    val showBottomBar = currentDestination?.route !in hideBottomBarRoutes  // ✅ route 직접 비교

    val cs = MaterialTheme.colorScheme

    Scaffold(

        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = Color.White) {
                    MainTabs.forEach { tab ->
                        // ✅ hierarchy 에 tab.route 가 포함되면 선택 상태
                        val selected = currentDestination
                            ?.hierarchy
                            ?.any { it.route == tab.route } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
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
        }
    ) { padding ->
        val top = if (isAuctionDetail) 0.dp else padding.calculateTopPadding()
        val bottom = if (showBottomBar) padding.calculateBottomPadding() else 0.dp

        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(top = top, bottom = bottom)
        )
    }
}