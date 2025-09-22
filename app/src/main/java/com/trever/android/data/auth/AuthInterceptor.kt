package com.trever.android.data.auth // 실제 패키지 경로로 수정해주세요

import android.util.Log // Log 임포트
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
//import javax.inject.Inject // 만약 Hilt/Dagger를 사용한다면, 아니면 생성자 주입으로 TokenStore 받기

// Hilt/Dagger 사용하지 않을 경우, 생성자로 TokenStore를 직접 받습니다.
// class AuthInterceptor @Inject constructor(private val tokenStore: TokenStore) : Interceptor {
class AuthInterceptor(private val tokenStore: TokenStore) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        // <<< 로그 추가: 인터셉터 실행 시작 >>>
        Log.d("AuthInterceptor", "AuthInterceptor 실행됨.")

        val accessToken =
            runBlocking {
                // <<< 로그 추가: 토큰 가져오기 시도 및 결과 >>>
                Log.d("AuthInterceptor", "TokenStore에서 AccessToken 가져오기 시도...")
                // 만약 tokenStore.getAccessToken()이 Flow<String?>을 반환하고 .first()를 사용해야 한다면,
                // 다음 라인의 주석을 풀고 아래 'val token =' 라인은 주석 처리합니다.
                // tokenStore.getAccessToken().first().also { tokenValue -> Log.d("AuthInterceptor", "가져온 AccessToken (from flow): $tokenValue") }

                // 만약 tokenStore.getAccessToken()이 직접 String?을 반환한다면 (예: SharedPreferences 직접 접근)
                val token = tokenStore.getAccessToken() // 여기가 Flow<String?> 이라면 .first()가 필요할 수 있습니다.
                Log.d("AuthInterceptor", "가져온 AccessToken: $token")
                token // 반환
            }
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        if (!accessToken.isNullOrBlank()) {
            // <<< 로그 추가: 토큰 발견 및 헤더 추가 시도 >>>
            Log.d("AuthInterceptor", "AccessToken 발견됨 ('$accessToken'). 헤더에 추가합니다: Bearer $accessToken")
            requestBuilder.header("Authorization", "Bearer $accessToken")
        } else {
            // <<< 로그 추가: 토큰 없음 >>>
            Log.d("AuthInterceptor", "AccessToken이 null이거나 비어있습니다. 헤더에 추가하지 않습니다.")
        }

        val request = requestBuilder.build()
        // <<< 로그 추가: 최종 요청 정보 및 헤더 목록 >>>
        Log.d("AuthInterceptor", "최종 요청 URL: ${request.url}")
        Log.d("AuthInterceptor", "--- 최종 요청 헤더 목록 시작 ---")
        request.headers.forEach { header ->
            Log.d("AuthInterceptor", "헤더: ${header.first} = ${header.second}")
        }
        Log.d("AuthInterceptor", "--- 최종 요청 헤더 목록 끝 ---")

        return chain.proceed(request)
    }
}
