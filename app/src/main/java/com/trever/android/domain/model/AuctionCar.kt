package com.trever.android.domain.model

data class AuctionCar(
    val id: String,
    val title: String,
    val year: Int,
    val mileageKm: Int,
    val imageUrl: String?,
    val tags: List<Tag> = emptyList(),
    val mainOptions: List<String>,
    val currentPriceWon: Long,
    val endsAtMillis: Long,      // 마감 시간 (epoch millis)
    val liked: Boolean = false,
    val auctionId: Long ?= null
)

enum class Tag { NEW, INSTANT, CERTIFIED }