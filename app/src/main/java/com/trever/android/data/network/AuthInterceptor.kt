package com.trever.android.data.network


import com.trever.android.data.auth.TokenStore
import com.trever.android.data.remote.AuthApi
import com.trever.android.data.remote.RefreshRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

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
    private val authApi: AuthApi
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // 동일 요청 재시도 무한루프 방지
        if (responseCount(response) >= 2) return null

        val refreshToken = runBlocking { tokenStore.getRefreshToken() } ?: return null

        val newTokens = runCatching {
            runBlocking {
                authApi.refresh(RefreshRequest(refreshToken))
            }
        }.getOrNull() ?: return null

        // 저장
        runBlocking {
            tokenStore.saveTokens(newTokens.accessToken, newTokens.refreshToken)
        }

        // 새로운 액세스 토큰으로 헤더 교체하여 재요청
        return response.request.newBuilder()
            .header("Authorization", "Bearer ${newTokens.accessToken}")
            .build()
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