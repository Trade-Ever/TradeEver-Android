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

    fun applyLikeResult(result: LikeResult) {
        val state = _uiState.value
        if (state is BuyListUiState.Success) {
            val carIdLong = result.carId.toLongOrNull()
            val updated = state.vehicles.map { v ->
                if (v.id == carIdLong) {
                    v.copy(
                        liked = result.liked,
                        favoriteCount = result.favoriteCount  // Summary에 필드가 있으면 반영
                    )
                } else v
            }
            _uiState.value = state.copy(vehicles = updated)
        }
    }

    fun toggleLike(carId: String) {
        Log.d("BuyListViewModel", "toggleLike 호출됨: $carId")
        viewModelScope.launch {
            val result = repository.toggleLike(carId)
            if (result.isSuccess) {
                Log.d("BuyListViewModel", "toggleLike 성공: $carId")
                val currentState = _uiState.value
                if (currentState is BuyListUiState.Success) {
                    val carIdLong = carId.toLongOrNull()
                    val updatedVehicles = currentState.vehicles.map { vehicle ->
                        if (vehicle.id == carIdLong) vehicle.copy(liked = !(vehicle.liked == true))
                        else vehicle
                    }
                    _uiState.value = currentState.copy(vehicles = updatedVehicles)
                }
            } else {
                Log.e("BuyListViewModel", "toggleLike 실패: $carId, ${result.exceptionOrNull()}")
            }
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

data class LikeResult(
    val carId: String,
    val liked: Boolean,
    val favoriteCount: Int
)