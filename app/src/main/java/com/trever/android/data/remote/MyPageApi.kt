package com.trever.android.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.GET

/**
 * 마이페이지 관련 API 엔드포인트를 정의하는 인터페이스입니다.
 */
interface MyPageApi {

    @GET("api/v1/recent-views")
    suspend fun getRecentlyViewedCars(): ApiResponse<List<RecentlyViewedCarDto>>

    @GET("api/v1/favorites")
    suspend fun getLikedCars(): ApiResponse<List<VehicleSummaryDto>>
}

/**
 * "최근 본 차량" API의 응답을 받기 위한 데이터 전송 객체(DTO)입니다.
 * 이 파일 내에서만 사용됩니다.
 */
@Serializable
data class RecentlyViewedCarDto(
    val id: Long,
    val carName: String?,
    val carNumber: String?,
    val manufacturer: String?,
    val model: String?,
    val year_value: Int?,
    val mileage: Int?,
    val transmission: String?,
    val vehicleStatus: String? = null,
    val fuelType: String?,
    val price: Long?,
    val isAuction: String?,
    val auctionId: Long?,
    val representativePhotoUrl: String?,
    val favoriteCount: Int?,
    val createdAt: String?,
    val isFavorite: Boolean?,
    val vehicleTypeName: String? = null,
    val mainOptions: List<String>? = null,
    val totalOptionsCount: Int? = null
)
