package com.trever.android.data.repository

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn

import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.trever.android.BuildConfig
import com.trever.android.data.auth.TokenStore
import com.trever.android.data.network.ApiClient
import com.trever.android.data.remote.AuthApi

import com.trever.android.data.remote.GoogleLoginRequest
import com.trever.android.data.remote.ProfileApi
import com.trever.android.data.remote.ProfileCompleteRequest
import com.trever.android.data.remote.ProfileCompleteResponse

class AuthRepository(
    private val tokenStore: TokenStore,
    private val authApi: AuthApi,
    private val context: Context,

) {
    private val profileApi: ProfileApi
        get() = ApiClient.profileApi
    private val googleSignInClient: GoogleSignInClient by lazy {

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.WEB_CLIENT_ID)
            .requestEmail()
            .build()


        GoogleSignIn.getClient(context, gso)
    }

    fun getGoogleSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    suspend fun handleGoogleSignInResult(idToken: String): Result<Boolean> {
        Log.d("AuthRepository", "Google ID Token: $idToken")
        return try {
            val apiResponse = authApi.googleLogin(GoogleLoginRequest(idToken)) // apiResponse는 ApiResponse<TokenResponse>

            if (apiResponse.success && apiResponse.data != null) {
                // apiResponse.data가 null이 아님을 확인했으므로 안전하게 접근 가능
                val tokenData = apiResponse.data
                tokenStore.saveTokens(tokenData.accessToken, tokenData.refreshToken)
                Result.success(tokenData.profileComplete)
            } else {
                // API 응답이 실패했거나 data가 null인 경우
                val errorMessage = "Google sign-in failed: ${apiResponse.message} (Data was ${if (apiResponse.data == null) "null" else "not null"})"
                Log.e("AuthRepository", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            // 네트워크 오류 또는 JSON 파싱 오류 등 기타 예외
            Log.e("AuthRepository", "Google sign-in exception: ${e.message}", e)
            Result.failure(e)
        }
    }


    suspend fun completeProfile(
        name: String,
        phone: String,
        locationCity: String,
        birthDate: String
    ): Result<ProfileCompleteResponse> {
        return try {
            val response = profileApi.completeProfile(
                ProfileCompleteRequest(name, phone, locationCity, birthDate)
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<Unit> {
        return try {
            val response = authApi.logout() // AuthApi에 추가된 logout 함수 호출
            if (response.success) {
                Log.d("AuthRepository", "로그아웃 API 호출 성공: ${response.message}")
                Result.success(Unit)
            } else {
                Log.e("AuthRepository", "로그아웃 API 호출 실패: ${response.message}")
                Result.failure(Exception(response.message ?: "로그아웃 실패"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "로그아웃 중 예외 발생", e)
            Result.failure(e)
        }
    }
}