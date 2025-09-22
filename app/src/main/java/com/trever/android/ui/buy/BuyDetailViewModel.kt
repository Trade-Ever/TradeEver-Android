package com.trever.android.ui.buy

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trever.android.data.repository.VehicleRepository
import com.trever.android.domain.model.VehicleDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BuyDetailViewModel(
    private val repository: VehicleRepository = VehicleRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<BuyDetailUiState>(BuyDetailUiState.Loading)
    val uiState: StateFlow<BuyDetailUiState> = _uiState

    fun loadVehicleDetail(vehicleId: String) {
        _uiState.value = BuyDetailUiState.Loading

        viewModelScope.launch {
            repository.getVehicleDetail(vehicleId)
                .onSuccess { vehicleDetail ->
                    _uiState.value = BuyDetailUiState.Success(vehicleDetail)
                }
                .onFailure { error ->
                    Log.e("BuyDetailViewModel", "일반 매물 상세 정보 로드 실패", error)
                    _uiState.value = BuyDetailUiState.Error(error.message ?: "알 수 없는 오류")
                }
        }
    }
}

sealed class BuyDetailUiState {
    object Loading : BuyDetailUiState()
    data class Success(val vehicle: VehicleDetail) : BuyDetailUiState()
    data class Error(val message: String) : BuyDetailUiState()
}