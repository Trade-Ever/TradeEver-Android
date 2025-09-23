package com.trever.android.ui.buy

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trever.android.data.network.ApiClient
import com.trever.android.data.repository.VehicleRepository
import com.trever.android.domain.model.VehicleSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.compareTo

class BuyListViewModel : ViewModel() {
    private val repository = VehicleRepository(ApiClient.vehicleApi)

    private val _uiState = MutableStateFlow<BuyListUiState>(BuyListUiState.Loading)
    val uiState: StateFlow<BuyListUiState> = _uiState

    private var currentPage = 0
    private var isLastPage = false
    private var isLoading = false

    init {
        loadVehicles()
    }

    fun loadVehicles(page: Int = 0, isRefresh: Boolean = true) {
        if (isLoading) return
        isLoading = true

        if (isRefresh) {
            _uiState.value = BuyListUiState.Loading
            currentPage = 0
            isLastPage = false
        }

        viewModelScope.launch {
            repository.getVehicles(currentPage)
                .onSuccess { vehicles ->
                    val uniqueVehicles = vehicles.distinctBy { it.id }
                    val currentState = _uiState.value
                    val newState = if (currentState is BuyListUiState.Success && !isRefresh) {
                        currentState.copy(
                            vehicles = (currentState.vehicles + uniqueVehicles).distinctBy { it.id },
                            currentPage = currentPage,
                            hasMorePages = uniqueVehicles.isNotEmpty() && uniqueVehicles.size == 10
                        )
                    } else {
                        BuyListUiState.Success(
                            vehicles = uniqueVehicles,
                            currentPage = currentPage,
                            hasMorePages = uniqueVehicles.isNotEmpty() && uniqueVehicles.size == 10
                        )
                    }
                    _uiState.value = newState
                    isLastPage = uniqueVehicles.isEmpty() || uniqueVehicles.size < 10
                }
                .onFailure { error ->
                    Log.e("BuyListViewModel", "Error loading vehicles", error)
                    _uiState.value = BuyListUiState.Error(error.message ?: "알 수 없는 오류")
                }

            isLoading = false
        }
    }

    fun loadNextPage() {
        if (!isLoading && !isLastPage) {
            currentPage++
            loadVehicles(currentPage, false)
        }
    }
}

sealed class BuyListUiState {
    object Loading : BuyListUiState()
    data class Success(
        val vehicles: List<VehicleSummary>,
        val currentPage: Int,
        val hasMorePages: Boolean
    ) : BuyListUiState()
    data class Error(val message: String) : BuyListUiState()
}