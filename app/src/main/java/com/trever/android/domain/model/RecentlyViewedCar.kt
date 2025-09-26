package com.trever.android.domain.model

data class RecentlyViewedCar(
    val id: String,
    val title: String,
    val year: Int,
    val mileageKm: Int,
    val imageUrl: String?,
    val priceWon: Long,
    val isAuction: Boolean,
    val manufacturer: String?,
    val model: String?,
    val mainOptions: List<String>? = null,
    val isFavorite: Boolean? = false
)
