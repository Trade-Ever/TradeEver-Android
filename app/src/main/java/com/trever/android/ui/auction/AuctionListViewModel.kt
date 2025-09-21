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
import com.trever.android.data.repository.VehicleRepository
import com.trever.android.domain.model.AuctionCar
import com.trever.android.domain.model.VehicleSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.compareTo


//class AuctionListViewModel : ViewModel() {
//    private val repository = VehicleRepository(ApiClient.vehicleApi)
//    private val auctionRepository = AuctionRepository(ApiClient.vehicleApi)
//
//    private val _uiState = MutableStateFlow<AuctionListUiState>(AuctionListUiState.Loading)
//    val uiState: StateFlow<AuctionListUiState> = _uiState
//
//    init {
//        loadAuctions()
//    }
//
//    fun loadAuctions(page: Int = 0) {
//        _uiState.value = AuctionListUiState.Loading
//        viewModelScope.launch {
//            repository.getAuctions(page)
//                .onSuccess { auctions ->
//                    _uiState.value = AuctionListUiState.Success(
//                        auctions = auctions,
//                        currentPage = page,
//                        hasMorePages = auctions.isNotEmpty() // 정확한 페이징 정보는 응답에서 추출 필요
//                    )
//                }
//                .onFailure { error ->
//                    Log.e("AuctionListViewModel", "Error loading auctions", error)
//                    _uiState.value = AuctionListUiState.Error(error.message ?: "알 수 없는 오류")
//                }
//        }
//    }
//
//    fun loadAuctionsWithFirebaseData() {
//        _uiState.value = AuctionListUiState.Loading
//        viewModelScope.launch {
//            repository.getAuctions(0)
//                .onSuccess { initialAuctions ->
//                    // Firebase에서 최신 가격과 마감일 정보 가져와서 업데이트
//                    val updatedAuctions = auctionRepository.updateAuctionsWithFirebaseData(initialAuctions)
//                    _uiState.value = AuctionListUiState.Success(
//                        auctions = updatedAuctions,
//                        currentPage = 0,
//                        hasMorePages = updatedAuctions.isNotEmpty()
//                    )
//                }
//                .onFailure { error ->
//                    _uiState.value = AuctionListUiState.Error(error.message ?: "알 수 없는 오류")
//                }
//        }
//    }
//
//    fun setupFirebaseListener() {
//        Firebase.database.getReferenceFromUrl(
//            "https://trever-ec541-default-rtdb.asia-southeast1.firebasedatabase.app/"
//        ).addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                // Firebase 데이터 변경 시 목록 업데이트
//                loadAuctionsWithFirebaseData()
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                // 오류 처리
//            }
//        })
//    }
//}
//
//sealed class AuctionListUiState {
//    object Loading : AuctionListUiState()
//    data class Success(
//        val auctions: List<AuctionCar>,
//        val currentPage: Int,
//        val hasMorePages: Boolean
//    ) : AuctionListUiState()
//    data class Error(val message: String) : AuctionListUiState()
//}

class AuctionListViewModel : ViewModel() {
    private val repository = VehicleRepository(ApiClient.vehicleApi)
    private val auctionRepository = AuctionRepository(ApiClient.vehicleApi)



    private val _uiState = MutableStateFlow<AuctionListUiState>(AuctionListUiState.Loading)
    val uiState: StateFlow<AuctionListUiState> = _uiState

    private var currentPage = 0
    private var isLastPage = false
    private var isLoading = false

    init {
        loadAuctions()
    }

//    fun loadAuctions(page: Int = 0) {
//        _uiState.value = AuctionListUiState.Loading
//        viewModelScope.launch {
//            repository.getAuctions(page)
//                .onSuccess { auctions ->
//                    _uiState.value = AuctionListUiState.Success(
//                        auctions = auctions,
//                        currentPage = page,
//                        hasMorePages = auctions.isNotEmpty() // 정확한 페이징 정보는 응답에서 추출 필요
//                    )
//                }
//                .onFailure { error ->
//                    Log.e("AuctionListViewModel", "Error loading auctions", error)
//                    _uiState.value = AuctionListUiState.Error(error.message ?: "알 수 없는 오류")
//                }
//        }
//    }

    fun loadAuctions(page: Int = 0, isRefresh: Boolean = true) {
        if (isLoading) return
        isLoading = true

        if (isRefresh) {
            _uiState.value = AuctionListUiState.Loading
            currentPage = 0
            isLastPage = false
        }

        viewModelScope.launch {
            repository.getAuctions(currentPage)
                .onSuccess { auctions ->
                    val currentState = _uiState.value
                    val newState = if (currentState is AuctionListUiState.Success && !isRefresh) {
                        // 기존 목록에 새로운 아이템 추가
                        currentState.copy(
                            auctions = currentState.auctions + auctions,
                            currentPage = currentPage,
                            hasMorePages = auctions.isNotEmpty() && auctions.size == 10 // 페이지 크기가 10이면
                        )
                    } else {
                        // 새로운 목록 설정
                        AuctionListUiState.Success(
                            auctions = auctions,
                            currentPage = currentPage,
                            hasMorePages = auctions.isNotEmpty() && auctions.size == 10
                        )
                    }

                    _uiState.value = newState
                    isLastPage = auctions.isEmpty() || auctions.size < 10
                }
                .onFailure { error ->
                    Log.e("AuctionListViewModel", "Error loading auctions", error)
                    _uiState.value = AuctionListUiState.Error(error.message ?: "알 수 없는 오류")
                }

            isLoading = false
        }
    }

    fun loadNextPage() {
        if (!isLoading && !isLastPage) {
            currentPage++
            loadAuctions(currentPage, false)
        }
    }

    fun loadAuctionsWithFirebaseData(isRefresh: Boolean = true) {
        if (isLoading) return
        isLoading = true

        if (isRefresh) {
            _uiState.value = AuctionListUiState.Loading
            currentPage = 0
            isLastPage = false
        }

        viewModelScope.launch {
            repository.getAuctions(0)
                .onSuccess { initialAuctions ->
                    // Firebase에서 최신 가격과 마감일 정보 가져와서 업데이트
                    val updatedAuctions = auctionRepository.updateAuctionsWithFirebaseData(initialAuctions)
                    _uiState.value = AuctionListUiState.Success(
                        auctions = updatedAuctions,
                        currentPage = 0,
                        hasMorePages = updatedAuctions.isNotEmpty() && updatedAuctions.size == 10
                    )
                }
                .onFailure { error ->
                    _uiState.value = AuctionListUiState.Error(error.message ?: "알 수 없는 오류")
                }
            isLoading = false
        }
    }

    fun setupFirebaseListener() {
        Firebase.database.getReferenceFromUrl(
            "https://trever-ec541-default-rtdb.asia-southeast1.firebasedatabase.app/auctions"
        ).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // 현재 상태가 Success인 경우에만 업데이트
                val currentState = _uiState.value
                if (currentState is AuctionListUiState.Success) {
                    // 기존 목록을 유지하면서 Firebase 데이터로 업데이트
                    viewModelScope.launch {
                        val updatedAuctions = auctionRepository.updateAuctionsWithFirebaseData(
                            currentState.auctions
                        )
                        _uiState.value = currentState.copy(auctions = updatedAuctions)
                    }
                } else {
                    // 아직 데이터가 로드되지 않은 경우에만 전체 로드
                    loadAuctionsWithFirebaseData()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AuctionListViewModel", "Firebase listener error: ${error.message}")
            }
        })
    }
}

sealed class AuctionListUiState {
    object Loading : AuctionListUiState()
    data class Success(
        val auctions: List<AuctionCar>,
        val currentPage: Int,
        val hasMorePages: Boolean
    ) : AuctionListUiState()
    data class Error(val message: String) : AuctionListUiState()
}