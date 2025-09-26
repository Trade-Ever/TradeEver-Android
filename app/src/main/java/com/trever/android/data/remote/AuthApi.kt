package com.trever.android.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @POST("api/v1/users/reissue")
    suspend fun refresh(@Body request: RefreshRequest): ApiResponse<ProfileCompleteData>

    @POST("api/v1/users/auth/google/login")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): ApiResponse<TokenResponse>

    @POST("api/v1/users/logout")
    suspend fun logout(): ApiResponse<Unit> // 로그아웃 API 추가

//    @POST("auth/google/login")
//    suspend fun googleLogin(@Body request: GoogleAuthCodeRequest): TokenResponse
}

@Serializable
data class ProfileCompleteData(
    val accessToken: String,
    val refreshToken: String,
    val profileComplete: Boolean
)




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
    val refreshToken: String
)

@Serializable
data class RefreshResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String
)
// ApiResponse<T>가 이미 다른 곳에 정의되어 있다고 가정합니다.
// 만약 없다면 아래와 같이 정의해야 합니다.
// (TransactionApi.kt에 있는 ApiResponse와 동일한 것을 사용하거나 공통 모듈로 옮겨야 할 수 있습니다)
// @Serializable
// data class ApiResponse<T>(
//    val status: Int,
//    val success: Boolean,
//    val message: String,
//    val data: T? = null // data는 nullable일 수 있고, 없을 수도 있습니다.
// )