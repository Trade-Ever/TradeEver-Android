package com.trever.android.domain.model

data class BidEntity(
    val id: Long = 0,
    val bidPrice: Long = 0,
    val bidderId: Long = 0,
    val bidderName: String = "",
    val createdAt: String = "",
    val bidderAvatarUrl: String? = null
)

// BidResponse.kt 파일 수정
data class BidResponse(
    var id: Long = 0,
    var bidPrice: Long = 0,
    var bidderId: Long = 0,
    var bidderName: String = "",
    var createdAt: String = "",

    // 다른 필드들이 있다면 여기에 추가 (모두 기본값 설정)
    var avatarUrl: String? = null
) {
    // Firebase가 필요로 하는 기본 생성자
    constructor() : this(0, 0, 0, "", "")
}
data class BidUi2(
    val id: Long,
    val name: String,
    val amountText: String,
    val timeText: String,
    val avatarUrl: String? = null
)
