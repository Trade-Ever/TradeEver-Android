package com.trever.android.data.remote

import com.trever.android.domain.model.AuctionCar
import com.trever.android.domain.model.Tag
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query
import kotlin.toString

interface VehicleApi {

    @GET("api/vehicles")
    suspend fun listVehicles(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("isAuction") isAuction: Boolean? = null // null이면 필터 미적용
    ): ApiResponse<VehicleListResponse>
}


@Serializable
data class ApiResponse<T>(
    val status: Int,
    val success: Boolean,
    val message: String,
    val data: T
)

// 차량 목록 응답 모델
@Serializable
data class VehicleListResponse(
    val vehicles: List<VehicleDto>,
    val totalCount: Int,
    val pageNumber: Int,
    val pageSize: Int
)

// 차량 정보 DTO
@Serializable
data class VehicleDto(
    val id: Long,
    val carName: String,
    val carNumber: String,
    val manufacturer: String,
    val model: String,
    val year_value: Int, // API 응답과 일치하도록 사용
    val mileage: Int,
    val transmission: String,
    val vehicleStatus: String,
    val fuelType: String,
    val price: Long? = null,
    val isAuction: String,
    val auctionId: Long?,
    val representativePhotoUrl: String?,
    val locationAddress: String,
    val favoriteCount: Int,
    val createdAt: String,
    val vehicleTypeName: String,
    val mainOptions: List<String>,
    val totalOptionsCount: Int
)


// 도메인 모델로 변환 확장 함수

private fun createTagsFromOptions(options: List<String>): List<Tag> {
    val tags = mutableListOf<Tag>()

    if (options.contains("내비게이션")) {
        tags.add(Tag.CERTIFIED)
    }

    // 추가 태그 로직은 비즈니스 요구사항에 맞게 구현
    // 예: 신규 매물인 경우 NEW 태그 추가

    return tags
}