package com.trever.android.data.network


import com.trever.android.data.auth.TokenStore
import com.trever.android.data.remote.AuthApi
import com.trever.android.data.remote.ProfileApi
import com.trever.android.data.remote.RefreshRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import kotlin.text.clear

/**
 * 매 요청에 Authorization 헤더를 붙여주는 Interceptor
 */
class AuthInterceptor(
    private val tokenStore: TokenStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val access = runBlocking { tokenStore.getAccessToken() }

        val req: Request = if (!access.isNullOrBlank()) {
            original.newBuilder()
                .header("Authorization", "Bearer $access")
                .build()
        } else original

        return chain.proceed(req)
    }
}

/**
 * 401 발생 시 자동으로 refresh 토큰을 사용해 토큰을 재발급하고
 * 원래 요청을 재시도하는 Authenticator
 */
class TokenAuthenticator(
    private val tokenStore: TokenStore,
    private val authApi: AuthApi,
    private val onTokenExpired: () -> Unit
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null

        val currentRefreshToken = runBlocking { tokenStore.getRefreshToken() }
        if (currentRefreshToken.isNullOrBlank()) {
            runBlocking { tokenStore.clear() }
            onTokenExpired() // 로그인 화면 이동 등 처리
            return null
        }

        synchronized(this) {
            val newAccessToken = runBlocking { tokenStore.getAccessToken() }
            val originalRequestAccessToken = response.request.header("Authorization")?.substringAfter("Bearer ")
            if (newAccessToken != null && originalRequestAccessToken != newAccessToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $newAccessToken")
                    .build()
            }

            val newTokens = runCatching {
                runBlocking {
                    authApi.refresh(RefreshRequest(currentRefreshToken))
                }
            }.getOrNull()?.data

            if (newTokens == null) {
                runBlocking { tokenStore.clear() }
                onTokenExpired() // 로그인 화면 이동 등 처리
                return null
            }

            runBlocking {
                tokenStore.saveTokens(newTokens.accessToken, newTokens.refreshToken)
            }

            return response.request.newBuilder()
                .header("Authorization", "Bearer ${newTokens.accessToken}")
                .build()
        }
    }

    private fun responseCount(response: Response): Int {
        var res = response
        var count = 1
        while (res.priorResponse != null) {
            count++
            res = res.priorResponse!!
        }
        return count
    }
}