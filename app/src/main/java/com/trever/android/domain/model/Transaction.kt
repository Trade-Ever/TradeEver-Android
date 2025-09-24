package com.trever.android.domain.model

/**
 * UI 레이어에서 거래 내역(판매/구매) 목록의 개별 아이템을 나타내는 데이터 클래스입니다.
 */
data class Transaction(
    val transactionId: Long,
    val vehicleId: Long,
    val vehicleName: String,
    val buyerName: String?,    // <--- 이 필드를 추가하세요!
    val sellerName: String?,   // <--- 이 필드를 추가하세요!
    val finalPrice: Long,
    val status: String,
    val createdAt: String,
    val contractPdfUrl: String?
    // 필요하다면 DTO에 있던 contractId도 추가할 수 있습니다.
    // val contractId: Long?
)