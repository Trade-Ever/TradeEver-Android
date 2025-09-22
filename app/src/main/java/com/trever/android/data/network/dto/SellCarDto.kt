package com.trever.android.data.network.dto // 실제 패키지 경로에 맞게 수정

import kotlinx.serialization.Serializable

@Serializable
data class SellCarRequest(
    val carNumber: String,
    val carName: String,
    val description: String,
    val manufacturer: String,
    val model: String,
    val year_value: Int, // JSON 필드명과 일치
    val mileage: Int,
    val fuelType: String,
    val transmission: String,
    val accidentHistory: Boolean,
    val accidentDescription: String?, // 사고 이력 없을 시 null 또는 빈 문자열 가능
    val vehicleStatus: String, // 예: "ACTIVE"
    val engineCc: Int,
    val horsepower: Int,
    val color: String,
    val additionalInfo: String?,
    val isAuction: Boolean,
    val price: Long?, // 일반 판매 시 가격, 경매 시 null
    val locationAddress: String,
    val photoOrders: List<Int>, // 사진 순서
    val vehicleType: String, // 서버에서 정의한 Enum 값 (예: "SEMI_MID_SIZE")
    val options: List<String>?,

    // 경매 관련 필드 (isAuction = true 일 때만 의미 있음)
    val startPrice: Long? = null,
    val startAt: String? = null, // ISO 8601 DateTime 형식 (예: "2025-09-20T10:00:00")
    val endAt: String? = null    // ISO 8601 DateTime 형식
    // 참고: 실제 사진 파일/데이터 전송은 이 DTO에 포함되지 않았습니다.
    // 사진은 별도 API로 업로드 후 URL 목록을 받거나,
    // Multipart 요청을 사용해야 합니다. 여기서는 사진 정보(순서 등)만 있다고 가정합니다.
)

@Serializable
data class SellCarResponse(
    val success: Boolean, // API 성공 여부
    val message: String,  // 결과 메시지
    val listingId: String? = null // 등록된 매물 ID 등 추가 정보 (선택적)
)
