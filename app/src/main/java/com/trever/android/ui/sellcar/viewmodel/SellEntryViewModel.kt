package com.trever.android.ui.sellcar.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.trever.android.data.network.ApiClient
import com.trever.android.data.remote.VehicleSummaryDto // 기존 DTO 유지
import com.trever.android.data.remote.toAuctionCar // toAuctionCar 확장 함수 사용
import com.trever.android.data.repository.AuctionRepository
import com.trever.android.data.repository.VehicleRepository
import com.trever.android.domain.model.AuctionCar // AuctionCar 모델 사용
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SellEntryUiState(
    val myVehicles: List<AuctionCar> = emptyList(), // 타입을 AuctionCar로 변경
    val isLoading: Boolean = false,
    val error: String? = null
)

class SellEntryViewModel(application: Application) : AndroidViewModel(application) {

    private val vehicleRepository = VehicleRepository(ApiClient.vehicleApi, application.applicationContext)
    // AuctionRepository 추가
    private val auctionRepository = AuctionRepository(ApiClient.vehicleApi)

    private val _uiState = MutableStateFlow(SellEntryUiState())
    val uiState: StateFlow<SellEntryUiState> = _uiState.asStateFlow()

    // Firebase Database 참조
    private val database = Firebase.database.getReferenceFromUrl(
        "https://trever-ec541-default-rtdb.asia-southeast1.firebasedatabase.app/auctions"
    )
    private var valueEventListener: ValueEventListener? = null

    init {
        fetchMyVehicles()
        setupFirebaseListener() // ViewModel 초기화 시 리스너 설정
    }

    fun fetchMyVehicles(isRefresh: Boolean = true) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isLoading = true) }
            }
            vehicleRepository.getMyVehicles()
                .onSuccess { data ->
                    // VehicleSummaryDto를 AuctionCar로 변환
                    val auctionCars = data.vehicles.map { it.toAuctionCar() }
                    // Firebase 데이터와 병합
                    val updatedCars = auctionRepository.updateAuctionsWithFirebaseData(auctionCars)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            myVehicles = updatedCars,
                            error = null
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

    private fun setupFirebaseListener() {
        if (valueEventListener != null) return // 이미 리스너가 설정되어 있으면 중복 설정 방지

        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentUiState = _uiState.value
                // 현재 차량 목록(AuctionCar)을 가져와 Firebase 데이터로 업데이트
                viewModelScope.launch {
                    val updatedCars = auctionRepository.updateAuctionsWithFirebaseData(currentUiState.myVehicles)
                    _uiState.update {
                        it.copy(myVehicles = updatedCars)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SellEntryViewModel", "Firebase listener cancelled", error.toException())
                // 필요시 오류 처리 UI 업데이트
                 _uiState.update {
                    it.copy(error = "실시간 데이터 동기화에 실패했습니다: ${error.message}")
                }
            }
        }
        database.addValueEventListener(valueEventListener!!)
    }

    // ViewModel이 파괴될 때 Firebase 리스너 제거
    override fun onCleared() {
        super.onCleared()
        valueEventListener?.let {
            database.removeEventListener(it)
        }
    }
}
