package com.trever.android.domain.model

data class FirebaseAuction(
    val id: Long = 0,
    val vehicleId: Long = 0,
    val startPrice: Long = 0,
    val currentBidPrice: Long = 0,
    val currentBidUserId: Long? = null,
    val currentBidUserName: String? = null,
    val startAt: String = "",
    val endAt: String = "",
    val lastBidTime: String? = null,
    val status: String = ""
) {
    // Firebase는 기본 생성자가 필요함
    constructor() : this(0, 0, 0, 0, null, null, "", "", null, "")
}