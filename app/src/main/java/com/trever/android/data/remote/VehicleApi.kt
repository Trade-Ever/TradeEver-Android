package com.trever.android.data.remote

import com.trever.android.domain.model.AuctionCar
import com.trever.android.domain.model.CarRegistrationRequest
import com.trever.android.domain.model.Tag // Tag가 실제로 사용되지 않는다면 제거 고려
import kotlinx.serialization.SerialName // 사용되지 않는다면 제거 고려
import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body // 사용되지 않는다면 제거 고려
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
// import kotlin.toString // 일반적으로 불필요

interface VehicleApi {

    @GET("api/vehicles")
    suspend fun listVehicles(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("isAuction") isAuction: Boolean? = null
    ): ApiResponse<VehicleListResponse>

    // (신규) category를 파라미터로 받는 새 제조사 목록 조회 함수
    @GET("api/cars/manufacturers")
    suspend fun getManufacturersByCategory(
        @Query("category") category: String
    ): ApiResponse<List<String>>

    // 차명 목록 조회 API 변경
    @GET("api/cars/carnames")
    suspend fun getCarNames(
        @Query("category") category: String,
        @Query("manufacturer") manufacturer: String
    ): ApiResponse<List<String>>

    @Multipart
    @POST("api/vehicles")
    suspend fun registerVehicle(
        @Part("request") request: RequestBody,
        @Part photos: List<MultipartBody.Part>
    ): ApiResponse<Int>

    @GET("api/vehicles/{id}")
    suspend fun getVehicleDetail(@Path("id") id: String): ApiResponse<VehicleDetailResponse>
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
    val carName: String? = null,
    val manufacturer: String? = null,
    val model: String? = null,
    val year_value: Int? = null,  // 반드시 Int?로 선언되어야 함
    val mileage: Int? = null,
    val transmission: String? = null,
    val fuelType: String? = null,
    val price: Long? = null,
    val isAuction: String? = null,
    val auctionId: Long? = null,
    val representativePhotoUrl: String? = null,
    val locationAddress: String? = null,
    val favoriteCount: Int? = null,
    val createdAt: String? = null,  // 현재 문자열로 처리 중인데 Long으로 바꿔주세요
    val vehicleTypeName: String? = null,
    val mainOptions: List<String>? = null,
    val totalOptionsCount: Int? = null
)

@Serializable
data class VehicleDetailResponse(
    val id: Int,
    val carNumber: String? = null,
    val carName: String? = null,
    val description: String? = null,
    val manufacturer: String? = null,
    val model: String? = null,
    val year_value: Int? = null,
    val mileage: Int? = null,
    val fuelType: String? = null,
    val transmission: String? = null,
    val accidentHistory: String? = null,
    val accidentDescription: String? = null,
    val engineCc: Int? = null,
    val horsepower: Int? = null,
    val color: String? = null,
    val price: Long? = null,
    val isAuction: String? = null,
    val vehicleStatus: String? = null,
    val auctionId: String? = null,
    val favoriteCount: Int? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val sellerId: Int? = null,
    val sellerName: String? = null,
    val photos: List<PhotoResponse> = emptyList(),
    val vehicleTypeName: String? = null,
    val options: List<String>? = null
)

@Serializable
data class PhotoResponse(
    val id: Int,
    val photoUrl: String
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