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
                    val updatedAuctions = auctionRepository.updateAuctionsWithFirebaseData(auctions)
                    val uniqueAuctions = updatedAuctions.distinctBy { it.id }
                    val currentState = _uiState.value
                    val newState = if (currentState is AuctionListUiState.Success && !isRefresh) {
                        currentState.copy(
                            auctions = (currentState.auctions + uniqueAuctions).distinctBy { it.id },
                            currentPage = currentPage,
                            hasMorePages = uniqueAuctions.isNotEmpty() && uniqueAuctions.size == 10
                        )
                    } else {
                        AuctionListUiState.Success(
                            auctions = uniqueAuctions,
                            currentPage = currentPage,
                            hasMorePages = uniqueAuctions.isNotEmpty() && uniqueAuctions.size == 10
                        )
                    }
                    _uiState.value = newState
                    isLastPage = uniqueAuctions.isEmpty() || uniqueAuctions.size < 10
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

    fun toggleLike(carId: String) {
        Log.d("AuctionListViewModel", "toggleLike 호출됨: $carId")
        viewModelScope.launch {
            val result = repository.toggleLike(carId)
            if (result.isSuccess) {
                Log.d("AuctionListViewModel", "toggleLike 성공: $carId")
                // 현재 상태에서 해당 차량의 liked 값만 토글
                val currentState = _uiState.value
                if (currentState is AuctionListUiState.Success) {
                    val updatedAuctions = currentState.auctions.map { car ->
                        if (car.id == carId) car.copy(liked = !(car.liked == true))
                        else car
                    }
                    _uiState.value = currentState.copy(auctions = updatedAuctions)
                }
            } else {
                Log.e("AuctionListViewModel", "toggleLike 실패: $carId, ${result.exceptionOrNull()}")
            }
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
                val currentState = _uiState.value
                if (currentState is AuctionListUiState.Success) {
                    // 현재까지 불러온 모든 경매 목록을 Firebase 데이터로 업데이트
                    viewModelScope.launch {
                        val updatedAuctions = auctionRepository.updateAuctionsWithFirebaseData(
                            currentState.auctions
                        )
                        _uiState.value = currentState.copy(auctions = updatedAuctions)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) { /* ... */ }
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