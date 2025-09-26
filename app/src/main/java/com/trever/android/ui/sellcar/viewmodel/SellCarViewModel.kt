package com.trever.android.ui.sellcar.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trever.android.R
import com.trever.android.data.network.ApiClient
import com.trever.android.data.repository.VehicleRepository
import com.trever.android.domain.model.AuctionCar
import com.trever.android.domain.model.CarRegistrationRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID
import java.util.concurrent.TimeUnit

data class SellCarUiState(
    val currentStep: Int = 1,
    val plateNumber: String = "",
    val isPlateNumberChecking: Boolean = false, // 번호판 중복 확인 중 상태
    val plateNumberExists: Boolean? = null, // 중복 확인 결과 (true: 중복, false: 사용 가능, null: 확인 전)
    val selectedManufacturer: String = "",
    val selectedModel: String = "",
    val selectedModelName: String = "",
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
    val manufacturerDataMap: Map<String, List<String>> = emptyMap(),
    val isLoadingManufacturers: Boolean = false,
    val carNameList: List<String> = emptyList(),
    val isLoadingCarNames: Boolean = false,
    val modelNameList: List<String> = emptyList(),
    val isLoadingModelNames: Boolean = false,
    val yearList: List<Int> = emptyList(),
    val isLoadingYears: Boolean = false
)

class SellCarViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(SellCarUiState())
    val uiState: StateFlow<SellCarUiState> = _uiState.asStateFlow()
    private val repository = VehicleRepository(ApiClient.vehicleApi, application.applicationContext)

    private val _registeredCars = MutableStateFlow<List<AuctionCar>>(emptyList())
    val registeredCars: StateFlow<List<AuctionCar>> = _registeredCars.asStateFlow()

    init {
        loadManufacturers()
    }

    fun checkPlateNumberDuplication(onResult: (isDuplicate: Boolean) -> Unit) {
        val plateNumber = _uiState.value.plateNumber
        if (plateNumber.isBlank()) return

        _uiState.update { it.copy(isPlateNumberChecking = true) }
        viewModelScope.launch {
            repository.checkCarNumber(plateNumber)
                .onSuccess { isDuplicate ->
                    _uiState.update { it.copy(isPlateNumberChecking = false, plateNumberExists = isDuplicate) }
                    onResult(isDuplicate)
                }
                .onFailure {
                    _uiState.update { it.copy(isPlateNumberChecking = false, plateNumberExists = null) }
                    onResult(false) // API 실패 시 중복이 아닌 것으로 간주하여 일단 플로우는 진행
                    Log.e("SellCarViewModel", "Failed to check plate number duplication", it)
                }
        }
    }

    fun resetPlateNumberCheck() {
        _uiState.update { it.copy(plateNumberExists = null) }
    }

    // --- 상태 업데이트 함수들 ---
    fun updateCurrentStep(step: Int) {
        _uiState.update { it.copy(currentStep = step) }
    }

    fun updatePlateNumber(plateNumber: String) {
        _uiState.update { it.copy(plateNumber = plateNumber) }
    }

    fun updateSelectedManufacturer(category: String, manufacturer: String) {
        _uiState.update {
            it.copy(
                selectedManufacturer = manufacturer,
                selectedModel = "",
                selectedModelName = "",
                carNameList = emptyList(),
                modelNameList = emptyList(),
                yearList = emptyList()
            )
        }
        if (manufacturer.isNotEmpty()) {
            loadCarNames(category, manufacturer)
        }
    }

    fun updateSelectedModel(model: String) {
        _uiState.update {
            it.copy(
                selectedModel = model,
                selectedModelName = "",
                modelNameList = emptyList(),
                yearList = emptyList()
            )
        }
        if (model.isNotEmpty()) {
            val state = _uiState.value
            val category = findCategoryForManufacturer(state.selectedManufacturer)
            if (category != null) {
                loadModelNames(category, state.selectedManufacturer, model)
            }
        }
    }

    fun updateSelectedModelName(modelName: String) {
        _uiState.update { it.copy(selectedModelName = modelName, yearList = emptyList()) }
        if (modelName.isNotEmpty()) {
            val state = _uiState.value
            val category = findCategoryForManufacturer(state.selectedManufacturer)
            if (category != null) {
                loadYears(category, state.selectedManufacturer, state.selectedModel, modelName)
            }
        }
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
            imageUrl = currentState.imageUris.firstOrNull()?.toString() ?: "drawable://${R.drawable.sell_entry_banner_placeholder}",
            year = currentState.selectedYear,
            mileageKm = currentState.mileage.filter { it.isDigit() }.toIntOrNull() ?: 0,
            currentPriceWon = currentState.price.filter { it.isDigit() }.toLongOrNull() ?: 0L,
            endsAtMillis = currentState.transactionEndDateMillis ?: (System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7)),
            startAtMillis = currentState.transactionStartDateMillis ?: System.currentTimeMillis(),
            liked = false,
            manufacturer = currentState.selectedManufacturer.ifEmpty { null },
            model = currentState.selectedModel.ifEmpty { null },
            transactionType = currentState.transactionType.ifEmpty { null },
            mainOptions = currentState.selectedOptions
        )
        _registeredCars.update { currentList -> currentList + newCar }
        resetUiStateForNewRegistration()
    }

    private fun resetUiStateForNewRegistration() {
        _uiState.value = SellCarUiState()
        loadManufacturers()
    }

    // --- 데이터 로드 함수들 ---

    private fun loadManufacturers() {
        if (_uiState.value.isLoadingManufacturers) return
        _uiState.update { it.copy(isLoadingManufacturers = true) }
        viewModelScope.launch {
            val result = repository.getManufacturersDataForSelection()
            _uiState.update { currentState ->
                result.fold(
                    onSuccess = { dataMap ->
                        currentState.copy(manufacturerDataMap = dataMap, isLoadingManufacturers = false)
                    },
                    onFailure = { exception ->
                        Log.e("SellCarViewModel", "Failed to load manufacturers", exception)
                        currentState.copy(isLoadingManufacturers = false)
                    }
                )
            }
        }
    }

    private fun loadCarNames(category: String, manufacturer: String) {
        if (manufacturer.isBlank() || _uiState.value.isLoadingCarNames) return
        _uiState.update { it.copy(isLoadingCarNames = true) }
        viewModelScope.launch {
            val result = repository.getCarNameList(category, manufacturer)
            _uiState.update { currentState ->
                result.fold(
                    onSuccess = { data ->
                        currentState.copy(carNameList = data, isLoadingCarNames = false)
                    },
                    onFailure = { exception ->
                        Log.e("SellCarViewModel", "Failed to load car names for $manufacturer", exception)
                        currentState.copy(isLoadingCarNames = false, carNameList = emptyList())
                    }
                )
            }
        }
    }

    private fun loadModelNames(category: String, manufacturer: String, carName: String) {
        if (carName.isBlank() || _uiState.value.isLoadingModelNames) return
        _uiState.update { it.copy(isLoadingModelNames = true) }
        viewModelScope.launch {
            val result = repository.getModelNameList(category, manufacturer, carName)
            _uiState.update { currentState ->
                result.fold(
                    onSuccess = { data ->
                        currentState.copy(modelNameList = data, isLoadingModelNames = false)
                    },
                    onFailure = { exception ->
                        Log.e("SellCarViewModel", "Failed to load model names for $carName", exception)
                        currentState.copy(isLoadingModelNames = false, modelNameList = emptyList())
                    }
                )
            }
        }
    }

    private fun loadYears(category: String, manufacturer: String, carName: String, modelName: String) {
        if (modelName.isBlank() || _uiState.value.isLoadingYears) return
        _uiState.update { it.copy(isLoadingYears = true) }
        viewModelScope.launch {
            val result = repository.getYearList(category, manufacturer, carName, modelName)
            _uiState.update { currentState ->
                result.fold(
                    onSuccess = { data ->
                        currentState.copy(yearList = data.sortedDescending(), isLoadingYears = false)
                    },
                    onFailure = { exception ->
                        Log.e("SellCarViewModel", "Failed to load years for $modelName", exception)
                        currentState.copy(isLoadingYears = false, yearList = emptyList())
                    }
                )
            }
        }
    }

    private fun findCategoryForManufacturer(manufacturer: String): String? {
        return _uiState.value.manufacturerDataMap.entries.find { it.value.contains(manufacturer) }?.key
    }

    // --- 차량 등록 함수 ---

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun registerCar(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        if (_isLoading.value) return

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                val isAuction = currentState.transactionType == "경매"

                val request = CarRegistrationRequest(
                    carNumber = currentState.plateNumber,
                    carName = currentState.selectedModel, // 차명 (예: 쏘나타)
                    description = currentState.description,
                    manufacturer = currentState.selectedManufacturer,
                    model = currentState.selectedModelName, // 상세 모델명 (예: DN8)
                    year_value = currentState.selectedYear,
                    mileage = currentState.mileage.toIntOrNull() ?: 0,
                    fuelType = currentState.fuelType,
                    transmission = currentState.transmissionType,
                    accidentHistory = currentState.hasAccidentHistory ?: false,
                    accidentDescription = currentState.accidentDetails,
                    engineCc = currentState.displacement.toIntOrNull() ?: 0,
                    horsepower = currentState.horsepower.toIntOrNull() ?: 0,
                    color = currentState.color.ifEmpty { "정보 없음" },
                    additionalInfo = "",
                    isAuction = isAuction,
                    price = if (!isAuction) (currentState.price.toIntOrNull() ?: 0) * 10000 else null,
                    startPrice = if (isAuction) (currentState.price.toIntOrNull() ?: 0) * 10000 else null,
                    startAt = if (isAuction) convertMillisToDateString(currentState.transactionStartDateMillis) else null,
                    endAt = if (isAuction) convertMillisToDateString(currentState.transactionEndDateMillis) else null,
                    locationAddress = "서울특별시 강남구 테헤란로 152", // TODO: 실제 주소 입력 UI 필요
                    photoOrders = currentState.imageUris.indices.toList(),
                    vehicleType = convertToVehicleType(currentState.selectedCarType),
                    options = currentState.selectedOptions
                )

                repository.registerVehicle(request, currentState.imageUris)
                onSuccess()
            } catch (e: Exception) {
                Log.e("SellCarViewModel", "차량 등록 실패", e)
                onError(e.message ?: "차량 등록 중 오류가 발생했습니다")
            } finally {
                _isLoading.value = false
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

    private fun convertToVehicleType(carType: String): String {
        return when (carType) {
            "대형" -> "LARGE"
            "중형" -> "MID_SIZE"
            "준중형" -> "SEMI_MID_SIZE"
            "소형" -> "COMPACT"
            "경차" -> "MINI"
            "SUV" -> "SUV"
            "스포츠" -> "SPORTS"
            "승합차" -> "VAN"
            else -> carType.uppercase().replace(" ", "_")
        }
    }
}
