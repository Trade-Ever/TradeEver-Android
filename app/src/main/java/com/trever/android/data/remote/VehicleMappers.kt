package com.trever.android.data.remote

import com.trever.android.domain.model.AuctionCar
import com.trever.android.domain.model.SearchCarItem
import com.trever.android.domain.model.Tag
import com.trever.android.domain.model.VehicleDetail
import com.trever.android.domain.model.VehicleSummary
import com.trever.android.ui.auction.AuctionDetailUi
import com.trever.android.ui.auction.SellerUi
import com.trever.android.ui.utils.formatKoreanWon
import com.trever.android.ui.utils.formatMileage
import java.text.NumberFormat
import java.util.Locale

fun VehicleDetail.toBuyDetailUi(): AuctionDetailUi {
    return AuctionDetailUi(
        images = photos,
        liked = liked,
        title = title,
        subTitle = "${year}년 · ${formatMileage(mileage)}",
        priceWon = price ?: 0L,
        priceWonText = formatKoreanWon(price ?: 0L),
        startPriceText = "",
        likeCount = favoriteCount,
        remainText = "",
        specs = createSpecsList(),
        notice = description,
        bids = emptyList(),
        seller = SellerUi(
            name = sellerName ?: "",
            id = sellerId ?: "",
            addr = sellerLocationCity ?: "",
            avatarUrl = sellerProfileImageUrl,
            phoneNumber = sellerPhone
        ),
        carName = carName
    )
}



fun VehicleDto.toAuctionCar(): AuctionCar {
    return AuctionCar(
        id = id.toString(),
        title = "${manufacturer} ${model}"?: "",
        year = year_value ?: 0,
        mileageKm = mileage ?: 0,
        imageUrl = representativePhotoUrl ?: "",
        tags = createTagsFromOptions(mainOptions ?: emptyList()),
        mainOptions = mainOptions ?: emptyList(),
        currentPriceWon = price ?: 0L,
        endsAtMillis = System.currentTimeMillis() + 86400000,
        startAtMillis = System.currentTimeMillis() + 86400000,
        liked = isFavorite ?: false,
        auctionId = auctionId ?: 0,
        transactionType = if (isAuction.equals("true", ignoreCase = true) || isAuction.equals("경매", ignoreCase = true)) "경매" else "일반"
    )
}

fun VehicleSummaryDto.toAuctionCar(): AuctionCar {
    return AuctionCar(
        id = id.toString(),
        title = carName ?: "",
        year = year_value ?: 0,
        mileageKm = mileage ?: 0,
        imageUrl = representativePhotoUrl ?: "",
        tags = createTagsFromOptions(mainOptions ?: emptyList()),
        mainOptions = mainOptions ?: emptyList(),
        currentPriceWon = price ?: 0L,
        // TODO: API 응답에 경매 종료 시간이 없으므로 임시값 사용
        endsAtMillis = System.currentTimeMillis() + 86400000,
        startAtMillis = System.currentTimeMillis(),
        liked = isFavorite ?: false,
        auctionId = auctionId ?: 0,
        // isAuction 값을 기반으로 transactionType 설정
        transactionType = if (isAuction.equals("true", ignoreCase = true) || isAuction.equals("경매", ignoreCase = true)) "경매" else "일반",
        manufacturer = manufacturer,
        model = model
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


fun VehicleDetailResponse.toVehicleDetail(): VehicleDetail {
    return VehicleDetail(
        id = id.toString(),
        title = "$manufacturer $model",
        carName = carName ?: "",
        description = description ?: "",
        year = year_value ?: 0,
        mileage = mileage ?: 0,
        fuelType = fuelType ?: "",
        transmission = transmission ?: "",
        engineCc = engineCc ?: 0,
        horsepower = horsepower ?: 0,
        color = color ?: "",
        price = price ?: 0L,
        accidentHistory = accidentHistory == "Y",
        accidentDescription = accidentDescription ?: "",
        photos = photos.map { it.photoUrl },
        options = options ?: emptyList(),
        isSeller = isSeller ?: false,
        sellerId = sellerId?.toString(),
        sellerName = sellerName,
        sellerLocationCity = sellerLocationCity, // ← 여기!
        sellerProfileImageUrl = sellerProfileImageUrl,
        sellerPhone = sellerPhone,
        vehicleStatus = vehicleStatus,
        vehicleTypeName = vehicleTypeName,
        liked = favorite ?: false,
        favoriteCount = favoriteCount ?: 0
    )
}

fun VehicleDto.toVehicleSummary(): VehicleSummary {
    return VehicleSummary(
        id = id,
        carName = carName ?: "",
        manufacturer = manufacturer ?: "",
        model = model ?: "",
        year = year_value ?: 0,
        mileageKm = mileage ?: 0,
        transmission = transmission ?: "",
        fuelType = fuelType ?: "",
        priceWon = price?.toLong(),
        isAuction = isAuction == "Y",
        auctionId = auctionId,
        imageUrl = representativePhotoUrl,
        locationAddress = null, // API 응답에 없는 필드
        favoriteCount = favoriteCount ?: 0,
        createdAt = createdAt ?: "",
        vehicleTypeName = vehicleTypeName,
        mainOptions = mainOptions ?: emptyList(),
        totalOptionsCount = totalOptionsCount ?: 0,
        liked = isFavorite ?: false // isFavorite 필드 매핑 추가
    )
}

fun VehicleSummary.toAuctionCarForDisplay(): AuctionCar {
    return AuctionCar(
        id = id.toString(),
        title = "$manufacturer $model",
        year = year,
        mileageKm = mileageKm,
        imageUrl = imageUrl ?: "",
        tags = emptyList(), // 필요한 경우 태그 생성 로직 추가
        mainOptions = mainOptions,
        currentPriceWon = priceWon ?: 0L,
        endsAtMillis = 0L, // 경매 종료 시간
        startAtMillis = 0L,
        liked = liked,
        auctionId = auctionId ?: 0,
        transactionType = if (isAuction) "경매" else "일반" // VehicleSummary의 isAuction (Boolean) 사용
    )
}

fun Vehicle.toSearchCarItem(): SearchCarItem =
    if (this.isAuction == "Y") {
        SearchCarItem.Auction(
            id = (this.id ?: 0L).toString(),
            title = "${this.manufacturer ?: ""} ${this.model ?: ""}",
            year = this.year_value ?: 0,
            mileageKm = this.mileage ?: 0,
            imageUrl = this.representativePhotoUrl,
            liked = this.isFavorite ?: false,
            currentPriceWon = (this.price ?: 0).toLong(),
            endsAtMillis = 0L, // 실제 종료 시간 필요시 매핑
            startAtMillis = 0L,
            mainOptions = this.mainOptions ?: emptyList(),
            auctionId = this.auctionId ?: 0L
        )
    } else {
        SearchCarItem.General(
            id = (this.id ?: 0L).toString(),
            title = "${this.manufacturer ?: ""} ${this.model ?: ""}",
            year = this.year_value ?: 0,
            mileageKm = this.mileage ?: 0,
            imageUrl = this.representativePhotoUrl,
            liked = this.isFavorite ?: false,
            priceWon = (this.price ?: 0).toLong(),
            mainOptions = this.mainOptions ?: emptyList()
        )
    }



private fun VehicleDetail.createSpecsList(): List<Pair<String, String>> {
    val specs = mutableListOf<Pair<String, String>>()

    specs.add("연료" to (fuelType ?: ""))
    specs.add("변속기" to (transmission ?: ""))
    specs.add("배기량(cc)" to (engineCc?.toString() ?: "")) // displacement 대신 engineCc 사용
    specs.add("마력" to (horsepower?.toString() ?: "")) // 타입 변환 수정
    specs.add("색상" to (color ?: ""))
    specs.add("차종" to (vehicleTypeName ?: ""))

    specs.add("사고이력" to (if (accidentHistory == true) "있음" else "없음")) // Boolean 타입 처리
    if (!accidentDescription.isNullOrBlank()) {
        specs.add("사고설명" to accidentDescription)
    }

    val optionsText = options?.joinToString(", ") ?: ""
    if (optionsText.isNotEmpty()) {
        specs.add("기타정보" to optionsText)
    }

    return specs
}








fun VehicleDetail.toAuctionDetailUi(): AuctionDetailUi {
    return AuctionDetailUi(
        images = photos,
        liked = liked,
        title = title,
        subTitle = "${year}년 · ${formatMileage(mileage)}",
        priceWon = price ?: 0L,
        priceWonText = formatKoreanWon(price ?: 0L),
        startPriceText = "", // 필요시 채우기
        likeCount = favoriteCount,
        remainText = "",
        specs = createSpecsList(),
        notice = description,
        bids = emptyList(),
        seller = SellerUi(
            name = sellerName ?: "",
            id = sellerId ?: "",
            addr = sellerLocationCity ?: "",
            avatarUrl = sellerProfileImageUrl
        ),
        carName = carName
    )
}

