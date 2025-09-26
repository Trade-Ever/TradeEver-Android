package com.trever.android.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.trever.android.R
import com.trever.android.data.auth.TokenStore
import com.trever.android.data.repository.ProfileRepository
import com.trever.android.ui.navigation.PROFILE_INPUT
import com.trever.android.ui.navigation.ROUTE_LOGIN
import com.trever.android.ui.navigation.ROUTE_MAIN
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun SplashScreen(navController: NavController, tokenStore: TokenStore) {
    val profileRepository: ProfileRepository = koinInject()

    LaunchedEffect(Unit) {
        val accessToken = tokenStore.getAccessToken()
        if (!accessToken.isNullOrBlank()) {
            val result = profileRepository.getProfile()
            val userProfile = result.getOrNull()
            if (userProfile?.profileComplete == false) {
                navController.navigate(PROFILE_INPUT) {
                    popUpTo("splash") { inclusive = true }
                }
            } else {
                navController.navigate(ROUTE_MAIN) {
                    popUpTo("splash") { inclusive = true }
                }
            }
        } else {
            navController.navigate(ROUTE_LOGIN) {
                popUpTo("splash") { inclusive = true }
            }
        }
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_purple),
            contentDescription = "Logo"
        )
    }

    // 스플래쉬 UI 표시
}