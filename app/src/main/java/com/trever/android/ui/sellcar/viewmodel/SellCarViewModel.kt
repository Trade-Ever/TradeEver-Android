package com.trever.android.ui.sellcar.viewmodel

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
    val horsepower: String = "" // 마력 추가
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

    // TODO: Implement other ViewModel logic for selling a car
}
