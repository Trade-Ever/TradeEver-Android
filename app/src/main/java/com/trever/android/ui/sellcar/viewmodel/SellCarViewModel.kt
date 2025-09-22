package com.trever.android.ui.sellcar.viewmodel


import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel // ViewModel 임포트 확인
import com.trever.android.R // For placeholder drawable
import com.trever.android.domain.model.AuctionCar
import android.util.Log
import androidx.lifecycle.AndroidViewModel

import androidx.lifecycle.viewModelScope
import com.trever.android.data.network.ApiClient
import com.trever.android.data.repository.VehicleRepository
import com.trever.android.domain.model.CarRegistrationRequest

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.text.get
import kotlin.toString

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
//class SellCarViewModel : ViewModel() { // @Inject constructor() 제거
class SellCarViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(SellCarUiState())
    val uiState: StateFlow<SellCarUiState> = _uiState.asStateFlow()
    // Repository에 application context 전달
    private val repository = VehicleRepository(ApiClient.vehicleApi, application.applicationContext)


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

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun registerCar(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        if (_isLoading.value) return // 이미 요청 중이면 무시

        _isLoading.value = true // 로딩 상태 시작
        viewModelScope.launch {
            try {
                // uiState에서 데이터 가져오기
                val isAuction = uiState.value.transactionType == "경매"
                val request = CarRegistrationRequest(
                    carNumber = uiState.value.plateNumber,
                    carName = "${uiState.value.plateNumber} ${uiState.value.selectedModel}",
                    description = uiState.value.description,
                    manufacturer = uiState.value.plateNumber,
                    model = uiState.value.selectedModel,
                    year_value = uiState.value.selectedYear,
                    mileage = uiState.value.mileage.toInt(),
                    fuelType = uiState.value.fuelType,
                    transmission = uiState.value.transmissionType,
                    accidentHistory = uiState.value.hasAccidentHistory ?: false,
                    accidentDescription = uiState.value.accidentDetails,
                    engineCc = uiState.value.displacement.toInt(),
                    horsepower = uiState.value.horsepower.toInt(),
                    color = "화이트", // 색상 데이터가 없어 기본값 설정
                    additionalInfo = "",
                    isAuction = isAuction,
                    price = if (!isAuction) uiState.value.price.toInt() * 10000  else null,
                    startPrice = if (isAuction) uiState.value.price.toInt() * 10000  else null,
                    startAt = if (isAuction) convertMillisToDateString(uiState.value.transactionStartDateMillis) else null,
                    endAt = if (isAuction) convertMillisToDateString(uiState.value.transactionEndDateMillis) else null,
                    locationAddress = "서울특별시 강남구 테헤란로 152", // 위치 데이터 기본값
                    photoOrders = listOf(0, 1, 2, 3, 4), // 사진 순서
                    vehicleType = convertToVehicleType(uiState.value.selectedCarType),
                    options = uiState.value.selectedOptions
                )

                // API 호출
                repository.registerVehicle(request, uiState.value.imageUris)
                onSuccess()
            } catch (e: Exception) {
                Log.e("SellCarViewModel", "차량 등록 실패", e)
                onError(e.message ?: "차량 등록 중 오류가 발생했습니다")
            } finally {
                _isLoading.value = false // 로딩 상태 종료
            }
        }
    }

    private fun convertMillisToDateString(millis: Long?): String? {
        if (millis == null) return null
        val calendar = Calendar.getInstance().apply { timeInMillis = millis }
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
    }

    // 차종을 API 요구 형식으로 변환
    private fun convertToVehicleType(carType: String): String {
        return when(carType) {
            "대형" -> "LARGE"
            "중형" -> "MID_SIZE"
            "준중형" -> "SEMI_MID_SIZE"
            "소형" -> "COMPACT"
            "경차" -> "MINI"
            "SUV" -> "SUV"
            "스포츠" -> "SPORTS"
            "승합차" -> "VAN"
            else -> "OTHER"
        }
    }
}
