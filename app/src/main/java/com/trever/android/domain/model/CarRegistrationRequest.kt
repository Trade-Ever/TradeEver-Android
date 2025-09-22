package com.trever.android.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CarRegistrationRequest(
    val carNumber: String,
    val carName: String,
    val description: String,
    val manufacturer: String,
    val model: String,
    val year_value: Int,
    val mileage: Int,
    val fuelType: String,
    val transmission: String,
    val accidentHistory: Boolean,
    val accidentDescription: String,
    val vehicleStatus: String = "ACTIVE",
    val engineCc: Int,
    val horsepower: Int,
    val color: String,
    val additionalInfo: String,
    val isAuction: Boolean,
    val price: Int? = null,
    val locationAddress: String,
    val photoOrders: List<Int>,
    val vehicleType: String,
    val options: List<String>,
    val startPrice: Int? = null,  // 경매 시작가
    val startAt: String? = null,  // 경매 시작일
    val endAt: String? = null     // 경매 종료일
)