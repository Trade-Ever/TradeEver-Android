package com.trever.android.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): RefreshResponse

    @POST("api/v1/users/auth/google/login")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): ApiResponse<TokenResponse>

//    @POST("auth/google/login")
//    suspend fun googleLogin(@Body request: GoogleAuthCodeRequest): TokenResponse
}



@Serializable
data class GoogleLoginRequest(val idToken: String)


@Serializable
data class TokenResponse(val accessToken: String, val refreshToken: String, val profileComplete: Boolean)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String
)

@Serializable
data class RefreshRequest(
    @SerialName("refresh_token") val refreshToken: String
)

@Serializable
data class RefreshResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String
)