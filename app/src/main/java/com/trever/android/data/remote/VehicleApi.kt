package com.trever.android.data.remote

import com.trever.android.domain.model.AuctionCar
import com.trever.android.domain.model.CarRegistrationRequest
import com.trever.android.domain.model.Tag
import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface VehicleApi {

    @GET("api/vehicles/check-car-number")
    suspend fun checkCarNumber(@Query("carNumber") carNumber: String): ApiResponse<CarNumberCheckDto>

    @GET("api/vehicles")
    suspend fun listVehicles(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("isAuction") isAuction: Boolean? = null
    ): ApiResponse<VehicleListResponse>

    @GET("api/vehicles/my-vehicles")
    suspend fun getMyVehicles(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sortBy") sortBy: String? = null
    ): ApiResponse<MyVehiclesResponse>

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

    @POST("api/v1/transactions/apply/{vehicleId}")
    suspend fun applyBuy(@Path("vehicleId") vehicleId: String): ApiResponse<BuyApplyData>

    @GET("api/v1/transactions/requests/{vehicleId}")
    suspend fun getBuyRequests(@Path("vehicleId") vehicleId: String): ApiResponse<List<BuyApplyData>>

    @POST("api/v1/transactions/select/{vehicleId}")
    suspend fun selectBuyer(
        @Path("vehicleId") vehicleId: String,
        @Query("buyerId") buyerId: Long
    ): ApiResponse<SelectBuyerResponse>

    @POST("api/v1/favorites/{vehicleId}/toggle")
    suspend fun toggleFavorite(@Path("vehicleId") vehicleId: String): ApiResponse<Boolean>

    @GET("api/v1/contracts/{id}/pdf")
    suspend fun getContractPdf(@Path("id") id: Long): Response<ResponseBody>

    @GET("api/v1/contracts/{id}")
    suspend fun getContract(@Path("id") id: Long): ApiResponse<ContractDetail>



}


@Serializable
data class ContractDetail(
    val contractId: Long,
    val transactionId: Long,
    val buyerName: String,
    val sellerName: String,
    val status: String,
    val signedAt: String,          // ISO
    val contractPdfUrl: String
)

@Serializable
data class CarNumberCheckDto(
    val carNumber: String,
    val exists: Boolean
)
@Serializable
data class SelectBuyerResponse(
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

@Serializable
data class BuyApplyData(
    val id: Long,
    val buyerId: Long,
    val vehicleId: Long,
    val buyerName: String,
    val vehicleName: String,
    val createdAt: String
)


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

// 스웨거 응답 형식에 맞게 단일 data 객체로 수정
@Serializable
data class MyVehiclesResponse(
    val vehicles: List<VehicleSummaryDto>,
    val totalCount: Int,
    val pageNumber: Int,
    val pageSize: Int
)

@Serializable
data class VehicleSummaryDto(
    val id: Long,
    val carName: String?,
    val carNumber: String?,
    val manufacturer: String?,
    val model: String?,
    val year_value: Int?,
    val mileage: Int?,
    val transmission: String?,
    val vehicleStatus: String?,
    val fuelType: String?,
    val price: Long?,
    val isAuction: String?,
    val auctionId: Long?,
    val representativePhotoUrl: String?,
    val favoriteCount: Int?,
    val createdAt: String?,
    val isFavorite: Boolean?,
    val vehicleTypeName: String?,
    val mainOptions: List<String>?,
    val totalOptionsCount: Int?
)

@Serializable
data class VehicleDto(
    val id: Long,
    val isFavorite: Boolean? = null,
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
    val isSeller: Boolean? = null,
    val favorite: Boolean? = null,
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
    val sellerLocationCity: String? = null, // 추가
    val sellerPhone: String? = null,        // 추가
    val sellerProfileImageUrl: String? = null, // 추가
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
