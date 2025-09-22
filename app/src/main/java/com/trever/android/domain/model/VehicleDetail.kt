package com.trever.android.domain.model

import com.trever.android.ui.auction.AuctionDetailUi
import kotlin.div
import kotlin.text.toDoubleOrNull
import kotlin.toString

data class VehicleDetail(
    val id: String,
    val title: String,
    val description: String,
    val year: Int,
    val mileage: Int,
    val fuelType: String,
    val transmission: String,
    val engineCc: Int,
    val horsepower: Int,
    val color: String,
    val price: Long,
    val accidentHistory: Boolean,
    val accidentDescription: String,
    val photos: List<String>,
    val options: List<String>,
    val sellerInfo: SellerInfo
)

data class SellerInfo(
    val id: String,
    val name: String,
    val createdAt: Long
)

