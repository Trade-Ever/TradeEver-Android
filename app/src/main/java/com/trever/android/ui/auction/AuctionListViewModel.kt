package com.trever.android.ui.auction

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trever.android.data.network.ApiClient
import com.trever.android.data.repository.VehicleRepository
import com.trever.android.domain.model.AuctionCar
import com.trever.android.domain.model.VehicleSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuctionListViewModel : ViewModel() {
    private val repository = VehicleRepository(ApiClient.vehicleApi)

    private val _uiState = MutableStateFlow<AuctionListUiState>(AuctionListUiState.Loading)
    val uiState: StateFlow<AuctionListUiState> = _uiState

    init {
        loadAuctions()
    }

    fun loadAuctions(page: Int = 0) {
        _uiState.value = AuctionListUiState.Loading
        viewModelScope.launch {
            repository.getAuctions(page)
                .onSuccess { auctions ->
                    _uiState.value = AuctionListUiState.Success(
                        auctions = auctions,
                        currentPage = page,
                        hasMorePages = auctions.isNotEmpty() // 정확한 페이징 정보는 응답에서 추출 필요
                    )
                }
                .onFailure { error ->
                    Log.e("AuctionListViewModel", "Error loading auctions", error)
                    _uiState.value = AuctionListUiState.Error(error.message ?: "알 수 없는 오류")
                }
        }
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