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
import com.trever.android.data.remote.BidData
import com.trever.android.data.repository.AuctionRepository
import com.trever.android.data.repository.VehicleRepository
import com.trever.android.domain.model.BidEntity
import com.trever.android.domain.model.FirebaseAuction
import com.trever.android.domain.model.VehicleDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

import kotlin.onSuccess
import kotlin.text.get

class AuctionDetailViewModel(
    private val repository: VehicleRepository = VehicleRepository(),
    private val auctionRepository: AuctionRepository = AuctionRepository(ApiClient.vehicleApi)
) : ViewModel() {

    private val PAGE_SIZE = 10

    private val _uiState = MutableStateFlow<AuctionDetailUiState>(AuctionDetailUiState.Loading)
    val uiState: StateFlow<AuctionDetailUiState> = _uiState

    // 입찰 내역을 저장할 별도의 상태
    private val _bids = MutableStateFlow<List<BidEntity>>(emptyList())
    val bids: StateFlow<List<BidEntity>> = _bids

    // 경매 정보를 저장할 상태 추가
    private val _auction = MutableStateFlow<FirebaseAuction?>(null)
    val auction: StateFlow<FirebaseAuction?> = _auction

    // 리스너를 저장하는 변수 추가
    private var bidsListener: ValueEventListener? = null

    fun toggleLike(vehicleId: String) {
        viewModelScope.launch {
            val result = repository.toggleLike(vehicleId)
            if (result.isSuccess) {
                val currentState = _uiState.value
                if (currentState is AuctionDetailUiState.Success) {
                    val vehicle = currentState.vehicle
                    val newLiked = !(vehicle.liked == true)
                    val newCount = if (newLiked) (vehicle.favoriteCount ?: 0) + 1 else (vehicle.favoriteCount ?: 0) - 1
                    val updated = vehicle.copy(liked = newLiked, favoriteCount = newCount)
                    _uiState.value = AuctionDetailUiState.Success(updated)
                }
            } else {
                // 필요시 에러 처리
            }
        }
    }

    fun loadVehicleDetail(vehicleId: String,auctionId: String) {
        _uiState.value = AuctionDetailUiState.Loading

        viewModelScope.launch {
            repository.getVehicleDetail(vehicleId)
                .onSuccess { vehicleDetail ->
                    _uiState.value = AuctionDetailUiState.Success(vehicleDetail)
                    // 실시간 리스너 설정으로 변경
                    setupBidsListener(auctionId)
                    loadAuctionInfo(auctionId)
                }
                .onFailure { error ->
                    Log.e("AuctionDetailViewModel", "Error loading vehicle detail", error)
                    _uiState.value = AuctionDetailUiState.Error(error.message ?: "알 수 없는 오류")
                }
        }
    }

    private fun loadAuctionInfo(auctionId: String) {
        viewModelScope.launch {
            try {
                val auction = auctionRepository.getAuctionById(auctionId)
                if (auction != null) {
                    _auction.value = auction
                } else {
                    Log.w("AuctionDetailViewModel", "Auction with ID $auctionId not found")
                }
            } catch (e: Exception) {
                Log.e("AuctionDetailViewModel", "Error loading auction info", e)
            }
        }
    }

    // 입찰 결과를 위한 상태 추가
    private val _bidResult = MutableStateFlow<Result<BidData>?>(null)
    val bidResult: StateFlow<Result<BidData>?> = _bidResult

    fun resetBidResult() {
        _bidResult.value = null
    }

    // 입찰 함수 추가
    fun placeBid(auctionId: Int, bidPrice: Long) {
        viewModelScope.launch {
            _bidResult.value = null // 초기화
            Log.d("AuctionDetailViewModel", "입찰 요청: auctionId=$auctionId, bidPrice=$bidPrice")
            try {
                auctionRepository.placeBid(auctionId, bidPrice)
                    .collect { result ->
                        _bidResult.value = result

                        // 입찰 성공 시 실시간 데이터 업데이트 (Firebase 리스너가 자동으로 갱신)
                        if (result.isSuccess) {
                            Log.d("AuctionDetailViewModel", "입찰 성공: ${result.getOrNull()}")
                        } else {
                            Log.e("AuctionDetailViewModel", "입찰 실패: ${result.exceptionOrNull()?.message}")
                        }
                    }
            } catch (e: Exception) {
                Log.e("AuctionDetailViewModel", "입찰 중 오류 발생", e)
                _bidResult.value = Result.failure(e)
            }
        }
    }



    // 실시간 입찰 내역 리스너 설정
    private fun setupBidsListener(vehicleId: String) {
        // 기존 리스너가 있으면 제거
        removeBidsListener()

        // Firebase 레퍼런스 가져오기
        val bidsRef = Firebase.database.getReference("bids/$vehicleId")

        // 새 리스너 생성 및 등록
        bidsListener = bidsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val bidsList = mutableListOf<BidEntity>()

                try {
                    // 데이터가 배열인 경우와 객체인 경우를 모두 처리
                    if (dataSnapshot.value is List<*>) {
                        // 배열 형식인 경우 (예: "65" 차량)
                        val bidsArray = dataSnapshot.value as List<*>
                        bidsArray.forEachIndexed { index, value ->
                            if (value != null && value is Map<*, *>) {
                                bidsList.add(mapToBidEntity(value))
                            }
                        }
                    } else if (dataSnapshot.value is Map<*, *>) {
                        // 객체 형식인 경우 (예: "107" 차량)
                        val bidsMap = dataSnapshot.value as Map<*, *>
                        bidsMap.values.forEach { value ->
                            if (value is Map<*, *>) {
                                bidsList.add(mapToBidEntity(value))
                            }
                        }
                    }

                    // 입찰가 기준 내림차순 정렬 (높은 금액이 먼저)
                    val sortedBids = bidsList.sortedByDescending { it.bidPrice }

                    // 상위 3개만 저장
                    _bids.value = sortedBids.take(3)
                } catch (e: Exception) {
                    Log.e("AuctionDetailViewModel", "Error parsing bids data", e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AuctionDetailViewModel", "Firebase bids listener error: ${error.message}")
            }
        })
    }

    // 리스너 제거 함수
    private fun removeBidsListener() {
        bidsListener?.let { listener ->
            try {
                Firebase.database.reference.removeEventListener(listener)
            } catch (e: Exception) {
                Log.e("AuctionDetailViewModel", "Error removing bids listener", e)
            }
        }
        bidsListener = null
    }

    // ViewModel이 소멸될 때 리스너 정리
    override fun onCleared() {
        super.onCleared()
        removeBidsListener()
    }

    // mapToBidEntity 메서드는 그대로 유지
    private fun mapToBidEntity(map: Map<*, *>): BidEntity {
        return BidEntity(
            id = (map["id"] as? Number)?.toLong() ?: 0,
            bidPrice = (map["bidPrice"] as? Number)?.toLong() ?: 0,
            bidderId = (map["bidderId"] as? Number)?.toLong() ?: 0,
            bidderName = map["bidderName"] as? String ?: "",
            createdAt = map["createdAt"] as? String ?: "",
            bidderAvatarUrl = map["bidderAvatarUrl"] as? String
        )
    }
}

sealed class AuctionDetailUiState {
    object Loading : AuctionDetailUiState()
    data class Success(val vehicle: VehicleDetail) : AuctionDetailUiState()
    data class Error(val message: String) : AuctionDetailUiState()
}