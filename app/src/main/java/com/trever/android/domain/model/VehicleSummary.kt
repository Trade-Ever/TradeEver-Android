package com.trever.android.domain.model



data class VehicleSummary(
    val id: Long,
    val carName: String,
    val manufacturer: String,
    val model: String,
    val year: Int,
    val mileageKm: Int,
    val transmission: String,
    val fuelType: String,
    val priceWon: Long?,             // 경매일 수 있어 null 허용
    val isAuction: Boolean,
    val auctionId: Long?,
    val imageUrl: String?,
    val locationAddress: String?,
    val favoriteCount: Int,
    val createdAt: String,
    val vehicleTypeName: String?,
    val mainOptions: List<String>,
    val totalOptionsCount: Int
)