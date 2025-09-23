package com.trever.android.ui.buy


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trever.android.data.remote.BuyApplyData
import com.trever.android.data.remote.SelectBuyerResponse
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

    private val _buyerList = MutableStateFlow<List<BuyApplyData>>(emptyList())
    val buyerList: StateFlow<List<BuyApplyData>> = _buyerList

    fun loadBuyRequests(vehicleId: String) {
        viewModelScope.launch {
            repository.getBuyRequests(vehicleId)
                .onSuccess { _buyerList.value = it }
                .onFailure { _buyerList.value = emptyList() }
        }
    }

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

    fun applyBuy(vehicleId: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.applyBuy(vehicleId) // repository에 구현 필요
                if (response.success) {
                    onResult(true, response.message)
                } else {
                    onResult(false, response.message)
                }
            } catch (e: Exception) {
                onResult(false, e.message ?: "신청 실패")
            }
        }
    }

    // BuyDetailViewModel.kt
    fun selectBuyer(
        vehicleId: String,
        buyerId: Long,
        onResult: (Boolean, String, SelectBuyerResponse?) -> Unit
    ) {
        viewModelScope.launch {
            repository.selectBuyer(vehicleId, buyerId)
                .onSuccess { response ->
                    onResult(true, "구매자 선택 완료", response)
                }
                .onFailure { error ->
                    onResult(false, error.message ?: "구매자 선택 실패", null)
                }
        }
    }
}

sealed class BuyDetailUiState {
    object Loading : BuyDetailUiState()
    data class Success(val vehicle: VehicleDetail) : BuyDetailUiState()
    data class Error(val message: String) : BuyDetailUiState()
}