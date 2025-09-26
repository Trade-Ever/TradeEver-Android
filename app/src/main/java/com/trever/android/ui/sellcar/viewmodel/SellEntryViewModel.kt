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
    val myVehicles: List<AuctionCar> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class SellEntryViewModel(application: Application) : AndroidViewModel(application) {

    private val vehicleRepository = VehicleRepository(ApiClient.vehicleApi, application.applicationContext)
    private val auctionRepository = AuctionRepository(ApiClient.vehicleApi)

    private val _uiState = MutableStateFlow(SellEntryUiState())
    val uiState: StateFlow<SellEntryUiState> = _uiState.asStateFlow()

    private val database = Firebase.database.getReferenceFromUrl(
        "https://trever-ec541-default-rtdb.asia-southeast1.firebasedatabase.app/auctions"
    )
    private var valueEventListener: ValueEventListener? = null
    private var isInitialApiFetchDone = false // 첫 API 호출 완료 여부 플래그

    init {
        Log.d("SellEntryVM_LifeCycle", "ViewModel init called")
        fetchMyVehicles() // 초기 데이터 로드 시작
    }

    fun fetchMyVehicles(isRefresh: Boolean = true) {
        Log.d("SellEntryVM_Fetch", "fetchMyVehicles called. isRefresh: $isRefresh, isInitialApiFetchDone: $isInitialApiFetchDone")
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isLoading = true) }
            }
            vehicleRepository.getMyVehicles()
                .onSuccess { data ->
                    Log.d("SellEntryVM_Fetch", "API onSuccess. data.vehicles.size: ${data.vehicles.size}")
                    val auctionCars = data.vehicles.map { it.toAuctionCar() }
                    Log.d("SellEntryVM_Fetch", "Mapped auctionCars.size: ${auctionCars.size}")

                    val updatedCars = auctionRepository.updateAuctionsWithFirebaseData(auctionCars)
                    Log.d("SellEntryVM_Fetch", "After Firebase merge. updatedCars.size: ${updatedCars.size}")

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            myVehicles = updatedCars,
                            error = null
                        )
                    }
                    Log.d("SellEntryVM_Fetch", "UI state updated with myVehicles.size: ${updatedCars.size}, isLoading: false")

                    // 첫 성공적인 API 호출 후 Firebase 리스너 설정
                    if (!isInitialApiFetchDone) {
                        setupFirebaseListener()
                        isInitialApiFetchDone = true
                    }
                }
                .onFailure { exception ->
                    Log.e("SellEntryVM_Fetch", "API onFailure: ${exception.message}", exception)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "차량 목록을 불러오는 데 실패했습니다."
                        )
                    }
                    // API 실패 시에도 리스너는 설정할 수 있으나, 초기 데이터가 없을 수 있음을 유의
                    // if (!isInitialApiFetchDone) {
                    //     setupFirebaseListener()
                    //     isInitialApiFetchDone = true
                    // }
                }
        }
    }

    private fun setupFirebaseListener() {
        // 이미 리스너가 설정되어 있으면 중복 설정 방지
        if (valueEventListener != null) {
            Log.d("SellEntryVM_Firebase", "Firebase listener already set up. Skipping.")
            return
        }
        Log.d("SellEntryVM_Firebase", "Setting up Firebase listener.")

        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentCars = _uiState.value.myVehicles
                val isLoading = _uiState.value.isLoading
                Log.d("SellEntryVM_Firebase", "onDataChange triggered. Current cars: ${currentCars.size}, isLoading: $isLoading")

                // 현재 로딩 중이 아닐 때만 Firebase 데이터 병합 실행
                if (!isLoading) {
                    viewModelScope.launch {
                        Log.d("SellEntryVM_Firebase", "Merging Firebase data with current cars list (count: ${currentCars.size})")
                        val updatedCars = auctionRepository.updateAuctionsWithFirebaseData(currentCars)
                        _uiState.update {
                            it.copy(myVehicles = updatedCars) // isLoading 상태는 변경하지 않음
                        }
                        Log.d("SellEntryVM_Firebase", "Firebase merge complete. New list count: ${updatedCars.size}")
                    }
                } else {
                    Log.d("SellEntryVM_Firebase", "Skipping Firebase merge because isLoading is true.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SellEntryViewModel", "Firebase listener cancelled", error.toException())
                _uiState.update {
                    it.copy(error = "실시간 데이터 동기화에 실패했습니다: ${error.message}")
                }
            }
        }
        database.addValueEventListener(valueEventListener!!)
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("SellEntryVM_LifeCycle", "ViewModel onCleared. Removing Firebase listener.")
        valueEventListener?.let {
            database.removeEventListener(it)
            valueEventListener = null // 리스너 참조 제거
        }
    }
}
