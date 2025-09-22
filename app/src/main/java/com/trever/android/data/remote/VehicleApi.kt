package com.trever.android.data.remote

import com.trever.android.domain.model.AuctionCar
import com.trever.android.domain.model.CarRegistrationRequest
import com.trever.android.domain.model.Tag
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface VehicleApi {

    @GET("api/vehicles")
    suspend fun listVehicles(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("isAuction") isAuction: Boolean? = null
    ): ApiResponse<VehicleListResponse>

    // 신규: 내 등록 매물 리스트 API
    @GET("api/vehicles/my")
    suspend fun listMyVehicles(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20 // 충분한 개수를 가져오도록 기본값 설정
    ): ApiResponse<VehicleListResponse>

    @GET("api/cars/manufacturers")
    suspend fun getManufacturersByCategory(
        @Query("category") category: String
    ): ApiResponse<List<String>>

    @GET("api/cars/carnames")
    suspend fun getCarNames(
        @Query("category") category: String,
        @Query("manufacturer") manufacturer: String
    ): ApiResponse<List<String>>

    @GET("api/cars/modelnames")
    suspend fun getModelNames(
        @Query("category") category: String,
        @Query("manufacturer") manufacturer: String,
        @Query("carName") carName: String
    ): ApiResponse<List<String>>

    @GET("api/cars/years")
    suspend fun getYears(
        @Query("category") category: String,
        @Query("manufacturer") manufacturer: String,
        @Query("carName") carName: String,
        @Query("modelName") modelName: String
    ): ApiResponse<List<Int>>

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

@Serializable
data class VehicleListResponse(
    val vehicles: List<VehicleDto>,
    val totalCount: Int,
    val pageNumber: Int,
    val pageSize: Int
)

@Serializable
data class VehicleDto(
    val id: Long,
    val carName: String? = null,
    val manufacturer: String? = null,
    val model: String? = null,
    val year_value: Int? = null,
    val mileage: Int? = null,
    val transmission: String? = null,
    val fuelType: String? = null,
    val price: Long? = null,
    val isAuction: String? = null,
    val auctionId: Long? = null,
    val representativePhotoUrl: String? = null,
    val locationAddress: String? = null,
    val favoriteCount: Int? = null,
    val createdAt: String? = null,
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
    val auctionId: Long? = null,
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

private fun createTagsFromOptions(options: List<String>): List<Tag> {
    val tags = mutableListOf<Tag>()

    if (options.contains("내비게이션")) {
        tags.add(Tag.CERTIFIED)
    }

    return tags
}