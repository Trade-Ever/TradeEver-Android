package com.trever.android.ui.sellcar.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trever.android.data.network.ApiClient
import com.trever.android.data.remote.VehicleSummaryDto
import com.trever.android.data.repository.VehicleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SellEntryUiState(
    val myVehicles: List<VehicleSummaryDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class SellEntryViewModel(application: Application) : AndroidViewModel(application) {

    private val vehicleRepository = VehicleRepository(ApiClient.vehicleApi, application.applicationContext)

    private val _uiState = MutableStateFlow(SellEntryUiState())
    val uiState: StateFlow<SellEntryUiState> = _uiState.asStateFlow()

    init {
        fetchMyVehicles()
    }

    fun fetchMyVehicles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            vehicleRepository.getMyVehicles()
                .onSuccess { data ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            myVehicles = data.vehicles
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "차량 목록을 불러오는 데 실패했습니다."
                        )
                    }
                }
        }
    }
}
