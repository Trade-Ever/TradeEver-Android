package com.trever.android.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

interface AuctionApi {
    @POST("api/auctions/bid")
    suspend fun placeBid(@Body request: BidRequest): BaseResponse<BidData>
}

@Serializable
data class BaseResponse<T>(
    val status: Int,
    val success: Boolean,
    val message: String,
    val data: T?
)

// 입찰 데이터 클래스
@Serializable
data class BidData(
    val id: Int,
    val bidPrice: Long,
    val bidderId: Int,
    val bidderName: String,
    val createdAt: String,
    val auctionId: Int,
    val isHighestBid: Boolean
)

// 입찰 요청 클래스
@Serializable
data class BidRequest(
    val auctionId: Int,
    val bidPrice: Long,
    val bidderId: Int
)