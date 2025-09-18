package com.trever.android.ui.sellcar.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Calendar

data class SellCarUiState(
    val currentStep: Int = 1,
    val plateNumber: String = "", // 번호판 프로퍼티 추가
    val selectedModel: String = "",
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val selectedCarType: String = "",
    val mileage: String = "",
    val fuelType: String = "",
    val transmissionType: String = "",
    val displacement: String = "",
    val horsepower: String = "",
    val imageUris: List<Uri> = emptyList(),
    val color: String = "",
    val selectedOptions: List<String> = emptyList(),
    val description: String = "",
    val hasAccidentHistory: Boolean? = null,
    val accidentDetails: String = "",
    val transactionType: String = "",
    val price: String = "",
    val transactionStartDateMillis: Long? = null,
    val transactionEndDateMillis: Long? = null,
)

class SellCarViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SellCarUiState())
    val uiState: StateFlow<SellCarUiState> = _uiState.asStateFlow()

    fun updateCurrentStep(step: Int) {
        _uiState.update { it.copy(currentStep = step) }
    }

    // 번호판 업데이트 함수 추가
    fun updatePlateNumber(plateNumber: String) {
        _uiState.update { it.copy(plateNumber = plateNumber) }
    }

    fun updateSelectedModel(model: String) {
        _uiState.update { it.copy(selectedModel = model) }
    }

    fun updateSelectedYear(year: Int) {
        _uiState.update { it.copy(selectedYear = year) }
    }

    fun updateSelectedCarType(carType: String) {
        _uiState.update { it.copy(selectedCarType = carType) }
    }

    fun updateMileage(mileage: String) {
        _uiState.update { it.copy(mileage = mileage) }
    }

    fun updateFuelType(fuelType: String) {
        _uiState.update { it.copy(fuelType = fuelType) }
    }

    fun updateTransmissionType(transmissionType: String) {
        _uiState.update { it.copy(transmissionType = transmissionType) }
    }

    fun updateDisplacement(displacement: String) {
        _uiState.update { it.copy(displacement = displacement) }
    }

    fun updateHorsepower(horsepower: String) {
        _uiState.update { it.copy(horsepower = horsepower) }
    }

    fun addImageUris(uris: List<Uri>) {
        _uiState.update { it.copy(imageUris = it.imageUris + uris) }
    }

    fun removeImageUri(uri: Uri) {
        _uiState.update { it.copy(imageUris = it.imageUris.filter { it != uri }) }
    }

    fun updateColor(color: String) {
        _uiState.update { it.copy(color = color) }
    }

    fun updateSelectedOptions(options: List<String>) {
        _uiState.update { it.copy(selectedOptions = options) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun updateHasAccidentHistory(hasAccident: Boolean?) {
        _uiState.update { it.copy(hasAccidentHistory = hasAccident) }
    }

    fun updateAccidentDetails(details: String) {
        _uiState.update { it.copy(accidentDetails = details) }
    }

    fun updateTransactionType(type: String) {
        _uiState.update { it.copy(transactionType = type) }
    }

    fun updatePrice(price: String) {
        _uiState.update { it.copy(price = price) }
    }

    fun updateTransactionDateRange(startDateMillis: Long?, endDateMillis: Long?) {
        _uiState.update {
            it.copy(
                transactionStartDateMillis = startDateMillis,
                transactionEndDateMillis = endDateMillis
            )
        }
    }
}
