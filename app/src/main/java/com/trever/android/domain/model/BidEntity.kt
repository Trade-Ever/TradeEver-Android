package com.trever.android.domain.model

data class BidEntity(
    val id: Long = 0,
    val bidPrice: Long = 0,
    val bidderId: Long = 0,
    val bidderName: String = "",
    val createdAt: String = "",
    val bidderAvatarUrl: String? = null
)
data class BidResponse(
    var id: Long = 0,
    var bidPrice: Long = 0,
    var bidderId: Long = 0,
    var bidderName: String = "",
    var createdAt: String = "",

    var avatarUrl: String? = null
) {
    constructor() : this(0, 0, 0, "", "")
}
data class BidUi2(
    val id: Long,
    val name: String,
    val amountText: String,
    val timeText: String,
    val avatarUrl: String? = null
)
