package com.trever.android.data.repository

import com.trever.android.data.network.ApiClient
import com.trever.android.data.remote.*
import com.trever.android.domain.model.AuctionCar

import com.trever.android.domain.model.VehicleSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VehicleRepository(
    private val api: VehicleApi = ApiClient.vehicleApi
) {
    suspend fun getAuctions(
        page: Int = 0,
        size: Int = 10
    ): Result<List<AuctionCar>> = withContext(Dispatchers.IO) {
        try {
            val response = api.listVehicles(page, size, isAuction = true)
            if (response.success) {
                val auctionCars = response.data.vehicles.map { it.toAuctionCar() }
                Result.success(auctionCars)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 필요한 경우 다른 메서드 구현
}