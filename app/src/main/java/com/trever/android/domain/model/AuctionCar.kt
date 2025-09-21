package com.trever.android.domain.model

data class AuctionCar(
    val id: String,
    val title: String,
    val year: Int,
    val mileageKm: Int,
    val imageUrl: String?,
    val tags: List<Tag> = emptyList(),
    val mainOptions: List<String>, // ViewModel에서 항상 값을 전달하고 있으므로 기본값 없어도 현재는 괜찮음
                                  // 하지만, 더 안정적으로 하려면 = emptyList() 추가 고려
    val currentPriceWon: Long,
    val endsAtMillis: Long,      // 마감 시간 (epoch millis)
    val liked: Boolean = false,
    // 아래 3개 프로퍼티 추가
    val manufacturer: String? = null,
    val model: String? = null,
    val transactionType: String? = null 
)

enum class Tag { NEW, INSTANT, CERTIFIED }
