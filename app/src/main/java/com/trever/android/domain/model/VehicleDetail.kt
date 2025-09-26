package com.trever.android.domain.model

import com.trever.android.ui.auction.AuctionDetailUi
import kotlin.div
import kotlin.text.toDoubleOrNull
import kotlin.toString

data class VehicleDetail(
    val id: String,
    val title: String,
    val carName: String,
    val description: String,
    val year: Int,
    val mileage: Int,
    val fuelType: String,
    val transmission: String,
    val engineCc: Int,
    val horsepower: Int,
    val color: String,
    val price: Long,
    val sellerPhone: String?,
    val vehicleStatus: String?,
    val accidentHistory: Boolean,
    val accidentDescription: String,
    val photos: List<String>,
    val options: List<String>,
    val isSeller: Boolean, // 본인 매물 여부
    val sellerId: String?,
    val sellerName: String?,
    val sellerLocationCity: String?,
    val sellerProfileImageUrl: String?,
    val vehicleTypeName: String?,
    val liked: Boolean,
    val favoriteCount: Int,
)

data class SellerInfo(
    val id: String,
    val name: String,
    val createdAt: Long
)

