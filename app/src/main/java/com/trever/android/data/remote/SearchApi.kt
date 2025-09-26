package com.trever.android.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SearchApi {

    @GET("api/v1/recent-searches")
    suspend fun getRecentSearches(): ApiResponse<List<String>>

    @GET("/api/vehicles/manufacturers")
    suspend fun getManufacturers(): ApiResponse<List<ManufacturerCategory>>

    @GET("/api/vehicles/manufacturers/{manufacturer}/car-names")
    suspend fun getCarNames(
        @retrofit2.http.Path("manufacturer") manufacturer: String
    ): ApiResponse<List<CarName>>

    @GET("/api/vehicles/manufacturers/{manufacturer}/car-names/{carName}/car-models")
    suspend fun getCarModels(
        @retrofit2.http.Path("manufacturer") manufacturer: String,
        @retrofit2.http.Path("carName") carName: String
    ): ApiResponse<List<CarModel>>

    @POST("/api/vehicles/search")
    suspend fun searchVehicles(
        @Body request: VehicleSearchRequest
    ): ApiResponse<VehicleSearchResponse>

    @DELETE("api/v1/recent-searches")
    suspend fun deleteRecentSearch(@Query("keyword") keyword: String): RecentResponse
}

@Serializable
data class RecentResponse(
    val status: Int,
    val success: Boolean,
    val message: String,
)

@Serializable
data class VehicleSearchRequest(
    val keyword: String? = null,
    val manufacturer: String? = null,
    val carName: String? = null,
    val carModel: String? = null,
    val yearStart: Int? = null,
    val yearEnd: Int? = null,
    val mileageStart: Int? = null,
    val mileageEnd: Int? = null,
    val priceStart: Int? = null,
    val priceEnd: Int? = null,
    val vehicleType: String? = null,
    val page: Int = 0,
    val size: Int = 20
)
@Serializable
data class Vehicle(
    val id: Long?,
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
data class VehicleSearchResponse(
    val vehicles: List<Vehicle>,
    val totalCount: Int,
    val pageNumber: Int,
    val pageSize: Int
)

@Serializable
data class CarModel(
    val carModel: String,
    val count: Int
)


@Serializable
data class ManufacturerCategory(
    val category: String,
    val manufacturers: List<Manufacturer>
)

@Serializable
data class Manufacturer(
    val manufacturer: String,
    val count: Int
)

@Serializable
data class CarName(
    val carName: String,
    val count: Int
)


