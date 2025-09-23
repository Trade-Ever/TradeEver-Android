package com.trever.android.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.GET

/**
 * 거래 관련 API 엔드포인트를 정의하는 인터페이스입니다.
 */
interface TransactionApi {

    @GET("api/v1/transactions/my/sales")
    suspend fun getSalesHistory(): ApiResponse<List<TransactionDto>>

    @GET("api/v1/transactions/my/purchases")
    suspend fun getPurchaseHistory(): ApiResponse<List<TransactionDto>>
}

/**
 * 거래 내역 API의 응답을 받기 위한 데이터 전송 객체(DTO)입니다.
 */
@Serializable
data class TransactionDto(
    val transactionId: Long,
    val vehicleId: Long,
    val vehicleName: String,
    val buyerName: String,
    val sellerName: String,
    val finalPrice: Long,
    val status: String,
    val createdAt: String,
    val contractId: Long,
    val contractPdfUrl: String
)
