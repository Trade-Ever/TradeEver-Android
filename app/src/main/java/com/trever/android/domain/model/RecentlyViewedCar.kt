package com.trever.android.domain.model

/**
 * UI 레이어에서 "최근 본 차량" 목록의 개별 아이템을 나타내는 데이터 클래스입니다.
 */
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
    val isFavorite: Boolean? = false // <--- 찜 여부 필드 추가
)
