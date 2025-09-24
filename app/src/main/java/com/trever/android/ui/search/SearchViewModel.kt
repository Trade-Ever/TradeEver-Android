package com.trever.android.ui.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.trever.android.data.network.ApiClient
import com.trever.android.data.remote.CarModel
import com.trever.android.data.remote.CarName
import com.trever.android.data.remote.ManufacturerCategory
import com.trever.android.data.remote.SearchApi
import com.trever.android.data.remote.VehicleSearchRequest
import com.trever.android.data.remote.VehicleSearchResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.text.toInt

class SearchViewModel(

) : ViewModel() {

    val searchText = MutableStateFlow("")

    val yearRange = MutableStateFlow<ClosedFloatingPointRange<Float>?>(null)
    val distanceRange = MutableStateFlow<ClosedFloatingPointRange<Float>?>(null)
    val priceRange = MutableStateFlow<ClosedFloatingPointRange<Float>?>(null)
    val selectedType = MutableStateFlow<String?>(null)
    val selectedManufacturer = MutableStateFlow<String?>(null)
    val selectedCarName = MutableStateFlow<String?>(null)
    val selectedCarModel = MutableStateFlow<String?>(null)
    private val api: SearchApi = ApiClient.searchApi
    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: StateFlow<List<String>> = _recentSearches

    private val _manufacturerCategories = MutableStateFlow<List<ManufacturerCategory>>(emptyList())
    val manufacturerCategories: StateFlow<List<ManufacturerCategory>> = _manufacturerCategories

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        viewModelScope.launch {
            yearRange.collectLatest { checkAndTriggerSearch() }
        }
        viewModelScope.launch {
            distanceRange.collectLatest { checkAndTriggerSearch() }
        }
        viewModelScope.launch {
            priceRange.collectLatest { checkAndTriggerSearch() }
        }
        viewModelScope.launch {
            selectedType.collectLatest { checkAndTriggerSearch() }
        }
    }

    private fun checkAndTriggerSearch() {
        // 모든 값이 null이 아니고, 실제로 유효할 때만 검색 실행
        if (
            yearRange.value != null ||
            distanceRange.value != null ||
            priceRange.value != null ||
            selectedType.value != null
        ) {
            triggerSearchIfReady()
        }
    }

    fun deleteRecentSearch(keyword: String) {
        viewModelScope.launch {
            val ok = api.deleteRecentSearch(keyword)
            if (ok.success) fetchRecentSearches() // 성공 시 목록 갱신
        }
    }

    private val carTypeMapReverse = mapOf(
        "대형" to "LARGE",
        "중형" to "MID_SIZE",
        "준중형" to "SEMI_MID_SIZE",
        "소형" to "SMALL",
        "스포츠" to "SPORTS",
        "SUV" to "SUV",
        "승합차" to "VAN",
        "경차" to "COMPACT"
    )


    private fun triggerSearchIfReady() {
        val request = VehicleSearchRequest(

            keyword = searchText.value.trim().takeIf { it.isNotEmpty() }, // 만약 StateFlow로 관리 중이라면
            manufacturer = selectedManufacturer.value?.takeIf { it.isNotEmpty() },
            carName = selectedCarName.value?.takeIf { it.isNotEmpty() },
            carModel = selectedCarModel.value?.takeIf { it.isNotEmpty() },
            yearStart = yearRange.value?.start?.toInt(),
            yearEnd = yearRange.value?.endInclusive?.toInt(),
            mileageStart = distanceRange.value?.start?.toInt(),
            mileageEnd = distanceRange.value?.endInclusive?.toInt(),
            priceStart = priceRange.value?.start?.toInt()?.times(1000000),
            priceEnd = priceRange.value?.endInclusive?.toInt()?.times(1000000),
            vehicleType = selectedType.value?.let { carTypeMapReverse[it] },
            page = 0,
            size = 10
        )
        Log.d("SearchViewModel", "request: $request")
        searchVehicles(request)
    }



    private val _searchResult = MutableStateFlow<VehicleSearchResponse?>(null)
    val searchResult: StateFlow<VehicleSearchResponse?> = _searchResult

    fun searchVehicles(request: VehicleSearchRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.searchVehicles(request) // response.data is VehicleSearchResponse?
                Log.d("SearchViewModel", "searchVehicles: $response")
                if (response.success) {
                    _searchResult.value = response.data // response.data is nullable, _searchResult is nullable - OK
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private val _carModels = MutableStateFlow<List<CarModel>>(emptyList())
    val carModels: StateFlow<List<CarModel>> = _carModels

    fun fetchCarModels(manufacturer: String, carName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.getCarModels(manufacturer, carName) // response.data is List<CarModel>?
                if (response.success) {
                    _carModels.value = response.data ?: emptyList()
                } else {
                    Log.e("SearchViewModel", "Failed to fetch car models: ${response.message}")
                    _carModels.value = emptyList() // API 실패 시 빈 리스트로 처리
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private val _carNames = MutableStateFlow<List<CarName>>(emptyList())
    val carNames: StateFlow<List<CarName>> = _carNames

    fun fetchCarNames(manufacturer: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.getCarNames(manufacturer) // response.data is List<CarName>?
                if (response.success) {
                    _carNames.value = response.data ?: emptyList()
                } else {
                    Log.e("SearchViewModel", "Failed to fetch car names: ${response.message}")
                    _carNames.value = emptyList() // API 실패 시 빈 리스트로 처리
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchRecentSearches() {
        viewModelScope.launch {
            val response = api.getRecentSearches() // response.data is List<String>?
            if (response.success) {
                _recentSearches.value = response.data ?: emptyList()
            } else {
                Log.e("SearchViewModel", "Failed to fetch recent searches: ${response.message}")
                _recentSearches.value = emptyList() // API 실패 시 빈 리스트로 처리
            }
        }
    }

    fun fetchManufacturers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.getManufacturers() // response.data is List<ManufacturerCategory>?
                if (response.success) {
                    _manufacturerCategories.value = response.data ?: emptyList()
                } else {
                    Log.e("SearchViewModel", "Failed to fetch manufacturers: ${response.message}")
                    _manufacturerCategories.value = emptyList() // API 실패 시 빈 리스트로 처리
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
}
