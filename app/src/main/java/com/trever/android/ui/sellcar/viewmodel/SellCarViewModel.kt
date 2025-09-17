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
    val selectedCarType: String = "" // 차종 추가
)

open class SellCarViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SellCarUiState())
    open val uiState: StateFlow<SellCarUiState> = _uiState.asStateFlow()

    open fun updateCarNumber(number: String) {
        _uiState.update { currentState ->
            currentState.copy(carNumber = number)
        }
    }

    open fun updateCurrentStep(step: Int) {
        _uiState.update { currentState ->
            currentState.copy(currentStep = step)
        }
    }

    open fun updateSelectedModel(model: String) {
        _uiState.update { currentState ->
            currentState.copy(selectedModel = model)
        }
    }

    open fun updateSelectedYear(year: Int) {
        _uiState.update { currentState ->
            currentState.copy(selectedYear = year)
        }
    }

    open fun updateMileage(mileage: String) {
        _uiState.update { currentState ->
            currentState.copy(mileage = mileage)
        }
    }

    open fun updateSelectedCarType(carType: String) {
        _uiState.update { currentState ->
            currentState.copy(selectedCarType = carType)
        }
    }

    // TODO: Implement other ViewModel logic for selling a car
}
