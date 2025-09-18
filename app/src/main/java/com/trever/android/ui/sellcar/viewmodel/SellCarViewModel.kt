package com.trever.android.ui.sellcar.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Calendar

// Define the UI state data class
data class SellCarUiState(
    val carNumber: String = "",
    val currentStep: Int = 1, // Default to the first step
    val selectedModel: String = "", // 차량 모델 추가
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR), // 선택된 연도 추가, 기본값은 현재 연도
    val mileage: String = "", // 주행거리 추가
    val selectedCarType: String = "", // 차종 추가
    val hasAccidentHistory: Boolean? = null, // 사고 이력 추가 (null: 선택 안함, true: 있음, false: 없음)
    val fuelType: String = "", // 연료 타입 추가
    val transmissionType: String = "", // 변속기 타입 추가
    val displacement: String = "", // 배기량 추가
    val horsepower: String = "", // 마력 추가
    val imageUris: List<Uri> = emptyList(), // 이미지 URI 리스트 추가
    val color: String = "", // 색상 추가
    val selectedOptions: List<String> = emptyList(), // 선택 옵션 리스트 추가
    val description: String = "", // 상세 설명 추가
    val accidentDetails: String = "", // 사고 상세 내용 추가
    val transactionType: String = "", // 거래 방식 추가
    val transactionDate: Long? = null, // 거래 날짜 추가 (Timestamp)
    val price: String = "" // 가격 추가
)

open class SellCarViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SellCarUiState())
    open val uiState: StateFlow<SellCarUiState> = _uiState.asStateFlow()

    open fun updateCarNumber(number: String) {
        _uiState.update { it.copy(carNumber = number) }
    }

    open fun updateCurrentStep(step: Int) {
        _uiState.update { it.copy(currentStep = step) }
    }

    open fun updateSelectedModel(model: String) {
        _uiState.update { it.copy(selectedModel = model) }
    }

    open fun updateSelectedYear(year: Int) {
        _uiState.update { it.copy(selectedYear = year) }
    }

    open fun updateMileage(mileage: String) {
        _uiState.update { it.copy(mileage = mileage) }
    }

    open fun updateSelectedCarType(carType: String) {
        _uiState.update { it.copy(selectedCarType = carType) }
    }

    open fun updateHasAccidentHistory(hasHistory: Boolean) {
        _uiState.update { it.copy(hasAccidentHistory = hasHistory) }
    }

    open fun updateFuelType(fuel: String) {
        _uiState.update { it.copy(fuelType = fuel) }
    }

    open fun updateTransmissionType(transmission: String) {
        _uiState.update { it.copy(transmissionType = transmission) }
    }

    open fun updateDisplacement(cc: String) {
        _uiState.update { it.copy(displacement = cc) }
    }

    open fun updateHorsepower(hp: String) {
        _uiState.update { it.copy(horsepower = hp) }
    }

    open fun addImageUris(uris: List<Uri>) {
        _uiState.update { currentState ->
            val currentUris = currentState.imageUris.toMutableList()
            currentUris.addAll(uris)
            currentState.copy(imageUris = currentUris.take(5)) // 최대 5장 제한
        }
    }

    open fun removeImageUri(uri: Uri) {
        _uiState.update { currentState ->
            val currentUris = currentState.imageUris.toMutableList()
            currentUris.remove(uri)
            currentState.copy(imageUris = currentUris)
        }
    }

    open fun updateColor(color: String) {
        _uiState.update { it.copy(color = color) }
    }

    open fun updateSelectedOptions(options: List<String>) {
        _uiState.update { it.copy(selectedOptions = options) }
    }

    open fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    open fun updateAccidentDetails(details: String) {
        _uiState.update { it.copy(accidentDetails = details) }
    }

    open fun updateTransactionType(type: String) {
        _uiState.update { it.copy(transactionType = type) }
    }

    open fun updateTransactionDate(date: Long?) {
        _uiState.update { it.copy(transactionDate = date) }
    }

    open fun updatePrice(price: String) {
        _uiState.update { it.copy(price = price) }
    }

    // TODO: Implement other ViewModel logic for selling a car
}
