package com.trever.android.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface WalletApi {

    // 지갑 충전
    @POST("api/v1/wallets/deposit")
    suspend fun deposit(
        @Query("amount") amount: Long
    ): ApiBaseResponse<Unit>

    // 지갑 출금
    @POST("api/v1/wallets/withdraw")
    suspend fun withdraw(
        @Query("amount") amount: Long
    ): ApiBaseResponse<Unit>

    // 지갑 잔액 조회
    @GET("api/v1/wallets")
    suspend fun getBalance(): ApiResponse<Long>
}


// 공통 응답 형태 (data 없을 때)
@Serializable
data class ApiBaseResponse<T>(
    val status: Int,
    val success: Boolean,
    val message: String,
    val data: T? = null
)