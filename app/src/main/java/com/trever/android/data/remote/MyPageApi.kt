package com.trever.android.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.GET

/**
 * 마이페이지 관련 API 엔드포인트를 정의하는 인터페이스입니다.
 */
interface MyPageApi {

    @GET("api/v1/recent-views")
    suspend fun getRecentlyViewedCars(): ApiResponse<RecentlyViewedDataWrapper> // <--- 수정됨

    @GET("api/v1/favorites")
    suspend fun getLikedCars(): ApiResponse<List<VehicleSummaryDto>> // TODO: 이 응답도 실제 구조 확인 필요
}

/**
 * "최근 본 차량" API의 'data' 필드 내부 구조를 위한 래퍼 클래스
 */
@Serializable
data class RecentlyViewedDataWrapper(
    val vehicles: List<RecentlyViewedCarDto>
)

/**
 * "최근 본 차량" API의 응답을 받기 위한 데이터 전송 객체(DTO)입니다.
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
    val totalCount: Int? = null,
    val pageNumber: Int? = null,
    val vehicleTypeName: String? = null,
    val mainOptions: List<String>? = null,
    val totalOptionsCount: Int? = null
)
