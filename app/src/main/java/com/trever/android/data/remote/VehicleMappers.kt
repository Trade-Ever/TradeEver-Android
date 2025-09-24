package com.trever.android.data.remote

import com.trever.android.domain.model.AuctionCar
import com.trever.android.domain.model.SearchCarItem
import com.trever.android.domain.model.SellerInfo
import com.trever.android.domain.model.Tag
import com.trever.android.domain.model.VehicleDetail
import com.trever.android.domain.model.VehicleSummary
import com.trever.android.ui.auction.AuctionDetailUi
import com.trever.android.ui.auction.BidUi
import com.trever.android.ui.auction.SellerUi

import kotlin.div

import kotlin.toString

fun VehicleDetail.toBuyDetailUi(): AuctionDetailUi {
    return AuctionDetailUi(
        images = photos,
        liked = false,
        title = title,
        subTitle = "${year}년 · ${formatMileage(mileage)}",
        priceWon = price ?: 0L,
        priceWonText = formatKoreanWon(price ?: 0L),
        startPriceText = "",
        likeCount = 0,
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

fun VehicleDto.toDomain(): VehicleSummary = VehicleSummary(
    id = id,
    carName = carName ?: "",
    manufacturer = manufacturer ?: "",
    model = model ?: "",
    year = year_value ?: 0,
    mileageKm = mileage ?: 0,
    transmission = transmission ?: "",
    fuelType = fuelType ?: "",
    priceWon = price ?: 0L,
    isAuction = isAuction?.equals("Y", ignoreCase = true) ?: false, // null 체크 추가
    auctionId = auctionId ?: 0,
    imageUrl = representativePhotoUrl ?: "",
    locationAddress = locationAddress ?: "",
    favoriteCount = favoriteCount ?: 0,
    createdAt = createdAt ?: "",
    vehicleTypeName = vehicleTypeName ?: "",
    mainOptions = mainOptions ?: emptyList(),
    totalOptionsCount = totalOptionsCount ?: 0
)

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
        liked = false,
        auctionId = auctionId ?: 0,
        // isAuction 값을 기반으로 transactionType 설정
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
fun VehiclesPageDto.toDomain(): Page<VehicleSummary> =
    Page(
        items = vehicles.map { it.toDomain() },
        totalCount = totalCount,
        pageNumber = pageNumber,
        pageSize = pageSize
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
        vehicleStatus = vehicleStatus
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
        totalOptionsCount = totalOptionsCount ?: 0
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
        liked = false,
        auctionId = auctionId ?: 0,
        transactionType = if (isAuction) "경매" else "일반" // VehicleSummary의 isAuction (Boolean) 사용
    )
}

fun Vehicle.toSearchCarItem(): SearchCarItem =
    if (this.isAuction == "Y") {
        SearchCarItem.Auction(
            id = (this.id ?: 0L).toString(),
            title = "${this.manufacturer ?: ""} ${this.carName ?: ""} ${this.model ?: ""}",
            year = this.year_value ?: 0,
            mileageKm = this.mileage ?: 0,
            imageUrl = this.representativePhotoUrl,
            liked = this.isFavorite ?: false,
            currentPriceWon = (this.price ?: 0) * 10000L,
            endsAtMillis = 0L, // 실제 종료 시간 필요시 매핑
            startAtMillis = 0L,
            mainOptions = this.mainOptions ?: emptyList(),
            auctionId = this.auctionId ?: 0L
        )
    } else {
        SearchCarItem.General(
            id = (this.id ?: 0L).toString(),
            title = "${this.manufacturer ?: ""} ${this.carName ?: ""} ${this.model ?: ""}",
            year = this.year_value ?: 0,
            mileageKm = this.mileage ?: 0,
            imageUrl = this.representativePhotoUrl,
            liked = this.isFavorite ?: false,
            priceWon = (this.price ?: 0) * 10000L,
            mainOptions = this.mainOptions ?: emptyList()
        )
    }

//fun VehicleDetail.toBuyDetailUi(): AuctionDetailUi {
//    val priceValue = price
//    val priceText = formatKoreanWon(priceValue)
//
//    return AuctionDetailUi(
//        images = photos, // photos를 사용
//        liked = false, // isLiked 대신 기본값
//        title = title, // manufacturer, model 대신 title 사용
//        subTitle = "${year}년 · ${formatMileage(mileage)}",
//        priceWon = priceValue,
//        priceWonText = priceText,
//        startPriceText = "",
//        likeCount = 0, // favoriteCount 대신 기본값
//        remainText = "",
//        specs = createSpecsList(),
//        notice = description ?: "",
//        bids = emptyList(),
//        seller = SellerUi(
//            name = sellerInfo.name,
//            id = sellerInfo.id,
//            addr = "", // sellerAddress 대신 빈 값
//            regDate = "", // sellerRegisteredAt 대신 빈 값
//            validDate = "",
//            count = 0, // sellerProductCount 대신 기본값
//            response = 0, // sellerResponseRate 대신 기본값
//            avatarUrl = null // sellerProfileImage 대신 null
//        )
//    )
//}

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
    val price: Long?,
    val accidentHistory: Boolean,
    val accidentDescription: String?,
    val photos: List<String>,
    val options: List<String>,
    val isSeller: Boolean,
    val sellerId: String?,
    val sellerName: String?,
    val sellerLocationCity: String?,
    val sellerProfileImageUrl: String?
)

data class SellerUi(
    val name: String,
    val id: String,
    val addr: String,
    val avatarUrl: String? = null
)

//fun VehicleDetailResponse.toVehicleDetail(): VehicleDetail {
//    return VehicleDetail(
//        id = id.toString(),
//        title = "$manufacturer $model",
//        description = description ?: "",
//        year = year_value ?: 0,
//        mileage = mileage ?: 0,
//        fuelType = fuelType ?: "",
//        transmission = transmission ?: "",
//        engineCc = engineCc ?: 0,
//        horsepower = horsepower ?: 0,
//        color = color ?: "",
//        price = price,
//        accidentHistory = accidentHistory == "Y",
//        accidentDescription = accidentDescription,
//        photos = photos.map { it.photoUrl },
//        options = options ?: emptyList(),
//        isSeller = isSeller ?: false,
//        sellerId = sellerId?.toString(),
//        sellerName = sellerName,
//        sellerLocationCity = sellerLocationCity,
//        sellerProfileImageUrl = sellerProfileImageUrl
//    )
//}

private fun VehicleDetail.createSpecsList(): List<Pair<String, String>> {
    val specs = mutableListOf<Pair<String, String>>()

    specs.add("연료" to (fuelType ?: ""))
    specs.add("변속기" to (transmission ?: ""))
    specs.add("배기량(cc)" to (engineCc?.toString() ?: "")) // displacement 대신 engineCc 사용
    specs.add("마력" to (horsepower?.toString() ?: "")) // 타입 변환 수정
    specs.add("색상" to (color ?: ""))

    val optionsText = options?.joinToString("\n") ?: ""
    if (optionsText.isNotEmpty()) {
        specs.add("기타 정보" to optionsText)
    }

    specs.add("사고 이력" to (if (accidentHistory == true) "있음" else "없음")) // Boolean 타입 처리
    if (!accidentDescription.isNullOrBlank()) {
        specs.add("사고 설명" to accidentDescription)
    }

    return specs
}

private fun formatKoreanWon(amount: Long): String {
    val 억 = amount / 100_000_000
    val 만 = (amount % 100_000_000) / 10_000

    return buildString {
        if (억 > 0) append("${억}억 ")
        if (만 > 0) append("${만}만원")
        if (억 == 0L && 만 == 0L) append("0원")
    }.trim()
}

private fun formatMileage(mileageKm: Int): String {
    return if (mileageKm >= 10000) {
        val man = mileageKm / 10000
        val remainder = (mileageKm % 10000) / 1000
        if (remainder > 0) {
            "$man.${remainder}만km"
        } else {
            "${man}만km"
        }
    } else {
        // 천 단위 콤마 표시
        "${String.format("%,d", mileageKm)}km"
    }
}





fun VehicleDetail.toAuctionDetailUi(): AuctionDetailUi {
    return AuctionDetailUi(
        images = photos,
        liked = false,
        title = title,
        subTitle = "${year}년 · ${formatMileage(mileage)}",
        priceWon = price ?: 0L,
        priceWonText = formatKoreanWon(price ?: 0L),
        startPriceText = "", // 필요시 채우기
        likeCount = 0,
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


//fun VehicleDetail.toAuctionDetailUi(): AuctionDetailUi {
//    // 임시 가격 설정 (실제로는 API에서 가져오거나 계산 필요)
//    val priceWon = 125_000_000L
//
//    // API 데이터로부터 스펙 생성
//    val specs = mutableListOf<Pair<String, String>>()
//    specs.add("연료" to fuelType)
//    specs.add("변속기" to transmission)
//    specs.add("배기량(cc)" to engineCc.toString())
//    specs.add("마력" to "${horsepower}마력")
//    specs.add("색상" to color)
//    if (options.isNotEmpty()) {
//        specs.add("기타 정보" to options.joinToString("\n"))
//    }
//    specs.add("사고 이력" to if (accidentHistory == true) "있음" else "없음")
//    if (!accidentDescription.isNullOrBlank()) {
//        specs.add("사고 설명" to accidentDescription)
//    }
//
//    // 임의의 입찰 데이터와 판매자 정보 (실제로는 Firebase에서 가져올 예정)
//    val bids = listOf(
//        BidUi("홍길동", "1억 2,500만원", "2025-09-15 18:15"),
//        BidUi("오광운", "1억 2,000만원", "2025-09-15 18:15"),
//        BidUi("최상근", "1억 1,000만원", "2025-09-15 18:15")
//    )
//
//    val seller = SellerUi(
//        name = "태민",
//        id = "seller123",
//        addr = "경기 수원시 영통구",
//        regDate = "2025.09.12",
//        validDate = "2025.09.16",
//        count = 39,
//        response = 96
//    )
//
//    return AuctionDetailUi(
//        images = if (photos.isEmpty()) listOf("https://picsum.photos/id/1018/1600/900") else photos,
//        liked = false,
//        title = title,
//        subTitle = "${year}년 · ${formatMileage(mileage)}",
//        priceWon = priceWon,
//        priceWonText = "1억 2,500만원", // 실제로는 포맷팅 필요
//        startPriceText = "시작가 1억원",
//        likeCount = 0,
//        remainText = "1시간 15분",
//        specs = specs,
//        notice = "판매자 안내사항이 없습니다.", // API에 해당 필드가 없음
//        bids = bids,
//        seller = seller
//    )
//}
