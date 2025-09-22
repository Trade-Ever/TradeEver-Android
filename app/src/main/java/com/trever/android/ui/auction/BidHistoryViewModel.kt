package com.trever.android.ui.auction

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.trever.android.data.network.ApiClient
import com.trever.android.data.repository.AuctionRepository
import com.trever.android.domain.model.BidResponse
import com.trever.android.domain.model.BidUi2
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BidHistoryViewModel(
    private val auctionRepository: AuctionRepository = AuctionRepository(ApiClient.vehicleApi)
) : ViewModel() {

    private val _bidList = MutableStateFlow<List<BidUi2>>(emptyList())
    val bidList = _bidList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private var currentAuctionId: String? = null
    private var bidListener: ValueEventListener? = null

    fun loadBids(auctionId: String) {
        currentAuctionId = auctionId
        setupFirebaseListener(auctionId)
    }

    private fun setupFirebaseListener(auctionId: String) {
        // 기존 리스너 제거
        removeFirebaseListener()

        _isLoading.value = true

        bidListener = Firebase.database.getReferenceFromUrl(
            "https://trever-ec541-default-rtdb.asia-southeast1.firebasedatabase.app/bids/$auctionId"
        ).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                viewModelScope.launch {
                    try {
                        val bidList = mutableListOf<BidResponse>()

                        snapshot.children.forEach { childSnapshot ->
                            val bid = childSnapshot.getValue(BidResponse::class.java)
                            if (bid != null) {
                                bidList.add(bid)
                            }
                        }

                        // 최신순 정렬
                        bidList.sortByDescending { it.id }

                        _bidList.value = bidList.map { it.toBidUi2() }
                        Log.d("BidHistory", "실시간 입찰 내역 업데이트: ${bidList.size}개")
                    } catch (e: Exception) {
                        Log.e("BidHistory", "입찰 내역 처리 실패", e)
                    } finally {
                        _isLoading.value = false
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                _isLoading.value = false
                Log.e("BidHistory", "Firebase 리스너 오류: ${error.message}")
            }
        })
    }

    private fun removeFirebaseListener() {
        currentAuctionId?.let { auctionId ->
            bidListener?.let { listener ->
                Firebase.database.getReferenceFromUrl(
                    "https://trever-ec541-default-rtdb.asia-southeast1.firebasedatabase.app/bids/$auctionId"
                ).removeEventListener(listener)
            }
        }
        bidListener = null
    }

    override fun onCleared() {
        removeFirebaseListener()
        super.onCleared()
    }

    private fun BidResponse.toBidUi2(): BidUi2 {
        return BidUi2(
            id = id,
            name = bidderName,
            amountText = won(bidPrice),
            timeText = formatDateTime(createdAt),
            avatarUrl = null // 프로필 이미지는 추후 추가
        )
    }
    private fun formatDateTime(dateTimeStr: String): String {
        return try {
            // 시간 문자열에서 나노초 부분 처리
            val simplified = if (dateTimeStr.contains(".")) {
                val parts = dateTimeStr.split(".")
                val base = parts[0]
                val decimal = parts[1].replace("Z", "")
                    .take(3)
                "$base.$decimal${if (dateTimeStr.endsWith("Z")) "Z" else ""}"
            } else {
                dateTimeStr
            }

            // 여러 날짜 포맷 시도
            val formats = listOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ss"
            )

            var date: Date? = null
            for (pattern in formats) {
                try {
                    val inputFormat = SimpleDateFormat(pattern, Locale.getDefault())
                    date = inputFormat.parse(simplified)
                    if (date != null) break
                } catch (e: Exception) {
                    // 다음 패턴 시도
                }
            }

            if (date != null) {
                val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA)
                outputFormat.format(date)
            } else {
                dateTimeStr
            }
        } catch (e: Exception) {
            dateTimeStr
        }
    }

    private fun won(amount: Long): String {
        val eok = amount / 100_000_000
        val man = (amount % 100_000_000) / 10_000
        return buildString {
            if (eok > 0) append("${eok}억 ")
            if (man > 0) append("${man}만원")
            if (isEmpty()) append("0원")
        }.trim()
    }
}