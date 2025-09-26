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
import com.trever.android.data.remote.VehicleSummaryDto
import com.trever.android.data.remote.toAuctionCar
import com.trever.android.data.repository.AuctionRepository
import com.trever.android.data.repository.VehicleRepository
import com.trever.android.domain.model.AuctionCar
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
    private var isInitialApiFetchDone = false

    init {
        Log.d("SellEntryVM_LifeCycle", "ViewModel init called")
        fetchMyVehicles()
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
                }
        }
    }

    private fun setupFirebaseListener() {
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

                if (!isLoading) {
                    viewModelScope.launch {
                        Log.d("SellEntryVM_Firebase", "Merging Firebase data with current cars list (count: ${currentCars.size})")
                        val updatedCars = auctionRepository.updateAuctionsWithFirebaseData(currentCars)
                        _uiState.update {
                            it.copy(myVehicles = updatedCars)
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
            valueEventListener = null
        }
    }
}
