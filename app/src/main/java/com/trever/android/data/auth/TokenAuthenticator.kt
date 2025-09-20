package com.trever.android.data.auth

import com.trever.android.data.remote.AuthApi
// AuthApi.kt에 정의된 DTO를 사용하므로, 별도 DTO 임포트는 필요 없음
// import com.trever.android.data.remote.dto.request.RefreshTokenRequest // 삭제 또는 주석 처리
// import com.trever.android.data.remote.dto.response.TokenResponse // 삭제 또는 주석 처리
import com.trever.android.data.remote.RefreshRequest // AuthApi.kt 내부 DTO 임포트
import com.trever.android.data.remote.RefreshResponse // AuthApi.kt 내부 DTO 임포트
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val tokenStore: TokenStore,
    private val authApi: AuthApi
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val currentRefreshToken = runBlocking { tokenStore.refreshTokenFlow.first() }

        if (currentRefreshToken.isNullOrBlank()) {
            // TODO: 로그아웃 처리 또는 로그인 화면으로 이동
            return null
        }

        // 중복 토큰 갱신 방지 (선택적이지만 권장)
        // 만약 여러 요청이 동시에 401을 받고 이 authenticator를 실행하려 할 때,
        // 첫 번째 요청만 토큰 갱신을 시도하고 나머지는 대기하거나 새 토큰을 사용하도록 합니다.
        // 이를 위해서는 synchronized 블록이나 다른 동기화 메커니즘이 필요할 수 있습니다.
        // 여기서는 단순화된 버전을 제공합니다.
        synchronized(this) {
            // 다시 한번 현재 토큰과 방금 받은 요청의 토큰이 다른지 확인 (토큰이 이미 갱신되었을 수 있음)
            val newAccessToken = runBlocking { tokenStore.accessTokenFlow.first() }
            val originalRequestAccessToken = response.request.header("Authorization")?.substringAfter("Bearer ")
            if (newAccessToken != null && originalRequestAccessToken != newAccessToken) {
                // 토큰이 이미 다른 요청에 의해 갱신된 경우, 새 토큰으로 요청 재구성
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $newAccessToken")
                    .build()
            }


            // 토큰 재발급 요청
            val refreshedTokens: RefreshResponse? = runBlocking {
                try {
                    // AuthApi.kt에 정의된 RefreshRequest 사용
                    val refreshRequestBody = RefreshRequest(currentRefreshToken)
                    // AuthApi.kt에 정의된 refresh 함수 사용
                    val tokenRefreshResponse = authApi.refresh(refreshRequestBody)

                    // 여기서는 isSuccessful 체크 대신, 반환된 객체가 null이 아닌지로 판단 (AuthApi의 refresh 함수가 Response<T>가 아닌 객체를 직접 반환할 경우)
                    // 만약 authApi.refresh가 Response<RefreshResponse>를 반환한다면, 이전 코드처럼 isSuccessful 등을 체크해야 합니다.
                    // 현재 AuthApi.kt의 정의로는 객체를 직접 반환하므로, 성공 시 객체가, 실패 시 예외가 발생할 것으로 예상됩니다.
                    // 따라서, 아래 코드는 authApi.refresh가 RefreshResponse를 직접 반환하거나 예외를 던진다고 가정합니다.
                    // 만약 authApi.refresh가 Response<RefreshResponse>를 반환하면, 그에 맞게 수정 필요.

                    // 실제로는 API 응답 구조에 따라 성공/실패 처리를 명확히 해야 합니다.
                    // 예를 들어, Retrofit의 Response<T>를 사용한다면 tokenRefreshResponse.isSuccessful 등으로 확인합니다.
                    // 여기서는 직접 RefreshResponse 객체를 받는다고 가정하고, 예외 발생 시 catch 블록에서 처리합니다.
                    tokenStore.saveTokens(tokenRefreshResponse.accessToken, tokenRefreshResponse.refreshToken)
                    tokenRefreshResponse
                } catch (e: Exception) {
                    // 네트워크 오류, 서버 오류 (예: 리프레시 토큰 만료 등)
                    // TODO: 특정 예외 타입에 따라 다른 처리 (예: 리프레시 토큰도 만료 시 로그아웃)
                    runBlocking { tokenStore.clear() } // 토큰 삭제 후 로그아웃 유도
                    null
                }
            }

            return if (refreshedTokens != null) {
                response.request.newBuilder()
                    .header("Authorization", "Bearer ${refreshedTokens.accessToken}")
                    .build()
            } else {
                null // 최종적으로 토큰 갱신 실패
            }
        }
    }
}
