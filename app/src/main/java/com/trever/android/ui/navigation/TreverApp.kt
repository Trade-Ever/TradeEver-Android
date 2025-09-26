package com.trever.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel

@Composable
fun TreverApp(sellCarViewModel: SellCarViewModel) {
    val navController = rememberNavController()
    AppNavHost(navController = navController, sellCarViewModel = sellCarViewModel)
}
