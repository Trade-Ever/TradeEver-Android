package com.trever.android.data.repository

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn

import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.trever.android.BuildConfig
import com.trever.android.data.auth.TokenStore
import com.trever.android.data.remote.AuthApi

import com.trever.android.data.remote.GoogleLoginRequest

class AuthRepository(
    private val tokenStore: TokenStore,
    private val authApi: AuthApi,
    private val context: Context
) {
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

    suspend fun handleGoogleSignInResult(idToken: String): Result<Unit> {
        Log.d("AuthRepository", "Google ID Token: $idToken") // 토큰 값 로그 출력
        return try {
            val response = authApi.googleLogin(GoogleLoginRequest(idToken))
            tokenStore.saveTokens(response.data.accessToken, response.data.refreshToken)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "로그인 실패12: ${e.message}", e)
            Result.failure(e)
        }
    }
}