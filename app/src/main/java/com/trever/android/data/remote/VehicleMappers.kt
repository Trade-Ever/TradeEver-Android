package com.trever.android.data.remote

import com.trever.android.domain.model.AuctionCar
import com.trever.android.domain.model.Tag
import com.trever.android.domain.model.VehicleSummary

import kotlin.toString


fun VehicleDto.toDomain(): VehicleSummary = VehicleSummary(
    id = id,
    carName = carName,
    manufacturer = manufacturer,
    model = model,
    year = year_value, // yearValue -> year_value로 수정
    mileageKm = mileage,
    transmission = transmission,
    fuelType = fuelType,
    priceWon = price,
    isAuction = isAuction.equals("Y", ignoreCase = true),
    auctionId = auctionId,
    imageUrl = representativePhotoUrl,
    locationAddress = locationAddress,
    favoriteCount = favoriteCount,
    createdAt = createdAt,
    vehicleTypeName = vehicleTypeName,
    mainOptions = mainOptions,
    totalOptionsCount = totalOptionsCount
)

fun VehicleDto.toAuctionCar(): AuctionCar {
    return AuctionCar(
        id = id.toString(),
        title = carName,
        year = year_value,
        mileageKm = mileage,
        imageUrl = representativePhotoUrl,
        tags = createTagsFromOptions(mainOptions),
        mainOptions = mainOptions,
        currentPriceWon = price ?: 0L,
        endsAtMillis = System.currentTimeMillis() + 86400000, // 임시로 1일 후로 설정
        liked = false
    )
}

private fun createTagsFromOptions(options: List<String>): List<Tag> {
    val tags = mutableListOf<Tag>()
    if (options.contains("내비게이션")) tags.add(Tag.CERTIFIED)
    return tags
}

data class Page<T>(
    val items: List<T>,
    val totalCount: Int,
    val pageNumber: Int,
    val pageSize: Int
)
data class VehiclesPageDto(
    val vehicles: List<VehicleDto>,
    val totalCount: Int,
    val pageNumber: Int,
    val pageSize: Int
)
fun VehiclesPageDto.toDomain(): Page<VehicleSummary> =
    Page(
        items = vehicles.map { it.toDomain() },
        totalCount = totalCount,
        pageNumber = pageNumber,
        pageSize = pageSize
    )