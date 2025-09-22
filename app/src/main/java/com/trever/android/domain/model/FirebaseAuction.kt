package com.trever.android.domain.model

data class FirebaseAuction(
    val id: Long = 0,
    val vehicleId: Long = 0,
    val startPrice: Long = 0,
    val currentBidPrice: Long = 0,
    val currentBidUserId: Long? = null,
    val currentBidUserName: String? = null,
    val startAt: String = "", // String -> Long
    val endAt: String = "",   // String -> Long
    val lastBidTime: String? = null, // String? -> Long?
    val status: String = ""
) {
    // Firebase는 기본 생성자가 필요함
    constructor() : this(0, 0, 0, 0, null, null, "", "", null, "") // 기본값 수정
}