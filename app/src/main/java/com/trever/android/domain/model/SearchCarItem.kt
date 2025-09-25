package com.trever.android.domain.model

sealed class SearchCarItem {
    data class Auction(
        val id: String,
        val title: String,
        val year: Int,
        val mileageKm: Int,
        val imageUrl: String?,
        val liked: Boolean,
        val currentPriceWon: Long,
        val endsAtMillis: Long,
        val startAtMillis: Long,
        val mainOptions: List<String>,
        val auctionId: Long
    ) : SearchCarItem()

    data class General(
        val id: String,
        val title: String,
        val year: Int,
        val mileageKm: Int,
        val imageUrl: String?,
        val liked: Boolean,
        val priceWon: Long,
        val mainOptions: List<String>
    ) : SearchCarItem()
}

fun SearchCarItem.Auction.toAuctionCar(): AuctionCar = AuctionCar(
    id = id,
    title = title,
    year = year,
    mileageKm = mileageKm,
    imageUrl = imageUrl ?: "",
    tags = emptyList(),
    mainOptions = mainOptions,
    currentPriceWon = currentPriceWon,
    endsAtMillis = endsAtMillis,
    startAtMillis = startAtMillis,
    liked = liked,
    auctionId = auctionId
)

fun SearchCarItem.General.toAuctionCarForDisplay(): AuctionCar = AuctionCar(
    id = id,
    title = title,
    year = year,
    mileageKm = mileageKm,
    imageUrl = imageUrl ?: "",
    tags = emptyList(),
    mainOptions = mainOptions,
    currentPriceWon = priceWon,
    endsAtMillis = 0L,
    startAtMillis = 0L,
    liked = liked,
    auctionId = 0L
)
