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
    val endsAtMillis: Long,
    val startAtMillis: Long,
    val liked: Boolean?,
    val auctionId: Long?= null,
    val manufacturer: String? = null,
    val model: String? = null,
    val transactionType: String? = null
)

enum class Tag { NEW, INSTANT, CERTIFIED }
