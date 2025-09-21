package com.trever.android.ui.sellcar.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel // ViewModel 임포트 확인
import com.trever.android.R // For placeholder drawable
import com.trever.android.domain.model.AuctionCar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Calendar
import java.util.UUID
import java.util.concurrent.TimeUnit

// SellCarUiState 데이터 클래스는 변경 없음 (이전 정의 사용)
data class SellCarUiState(
    val currentStep: Int = 1,
    val plateNumber: String = "",
    val selectedManufacturer: String = "",
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

// @HiltViewModel 제거
class SellCarViewModel : ViewModel() { // @Inject constructor() 제거
    private val _uiState = MutableStateFlow(SellCarUiState())
    val uiState: StateFlow<SellCarUiState> = _uiState.asStateFlow()

    private val _registeredCars = MutableStateFlow<List<AuctionCar>>(emptyList())
    val registeredCars: StateFlow<List<AuctionCar>> = _registeredCars.asStateFlow()

    fun updateCurrentStep(step: Int) {
        _uiState.update { it.copy(currentStep = step) }
    }

    fun updatePlateNumber(plateNumber: String) {
        _uiState.update { it.copy(plateNumber = plateNumber) }
    }

    fun updateSelectedManufacturer(manufacturer: String) {
        _uiState.update { it.copy(selectedManufacturer = manufacturer) }
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

    fun completeRegistrationAndAddCar() {
        val currentState = _uiState.value
        val newCar = AuctionCar(
            id = UUID.randomUUID().toString(), 
            title = "${currentState.selectedManufacturer} ${currentState.selectedModel}".trim().ifEmpty { currentState.plateNumber.ifEmpty{"차량 정보 없음"} },
            imageUrl = currentState.imageUris.firstOrNull()?.toString() ?: "drawable://" + R.drawable.sell_entry_banner_placeholder,
            year = currentState.selectedYear,
            mileageKm = currentState.mileage.filter { it.isDigit() }.toIntOrNull() ?: 0,
            currentPriceWon = currentState.price.filter { it.isDigit() }.toLongOrNull() ?: 0L,
            endsAtMillis = currentState.transactionEndDateMillis ?: (System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7)),
            liked = false, 
            manufacturer = currentState.selectedManufacturer.ifEmpty { null },
            model = currentState.selectedModel.ifEmpty { null },
            transactionType = currentState.transactionType.ifEmpty { null },
            mainOptions = currentState.selectedOptions // AuctionCar 모델의 mainOptions는 기본값으로 emptyList()를 가짐
            // AuctionCar 모델의 tags는 기본값으로 emptyList()를 가짐
        )
        _registeredCars.update { currentList -> currentList + newCar }
        resetUiStateForNewRegistration() 
    }

    private fun resetUiStateForNewRegistration() {
        _uiState.value = SellCarUiState() 
    }
}
