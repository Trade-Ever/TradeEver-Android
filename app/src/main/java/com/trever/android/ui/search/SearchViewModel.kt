package com.trever.android.ui.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

import com.trever.android.data.network.ApiClient
import com.trever.android.data.remote.CarModel
import com.trever.android.data.remote.CarName
import com.trever.android.data.remote.ManufacturerCategory
import com.trever.android.data.remote.SearchApi
import com.trever.android.data.remote.VehicleSearchRequest
import com.trever.android.data.remote.VehicleSearchResponse
import com.trever.android.data.remote.toSearchCarItem
import com.trever.android.data.repository.AuctionRepository
import com.trever.android.domain.model.SearchCarItem

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.compareTo
import kotlin.inc
import kotlin.text.get
import kotlin.text.toInt
import kotlin.toString

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

    private val auctionRepository = AuctionRepository(ApiClient.vehicleApi)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _searchCarItems = MutableStateFlow<List<SearchCarItem>>(emptyList())
    val searchCarItems: StateFlow<List<SearchCarItem>> = _searchCarItems

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

            triggerSearchIfReady()

    }
    private var lastSearchRequest: VehicleSearchRequest? = null

    private var currentPage = 0
    private var isLastPage = false
    private var isLoadingPage = false
    val hasMorePages: Boolean
        get() = !isLastPage

//    fun updateSearchResultWithFirebase() {
//        viewModelScope.launch {
//            val result = _searchResult.value ?: return@launch
//            // 1. 경매 차량만 추출
//            val auctionItems = result.vehicles.filterIsInstance<SearchCarItem.Auction>()
//            val auctionIds = auctionItems.map { it.auctionId.toString() }
//            // 2. Firebase에서 경매 정보 받아오기
//            val firebaseAuctions = auctionRepository.getFirebaseAuctionsByIds(auctionIds)
//            // 3. 전체 차량 리스트를 순회하며 경매 차량만 Firebase 정보로 갱신
//            val updatedList = result.vehicles.map { item ->
//                if (item is SearchCarItem.Auction) {
//                    val fb = firebaseAuctions[item.auctionId.toString()]
//                    if (fb != null) {
//                        item.copy(
//                            currentPriceWon = fb.currentBidPrice.takeIf { it > 0 } ?: fb.startPrice,
//                            endsAtMillis = auctionRepository.parseFirebaseDateToMillis(fb.endAt)
//                            // 필요한 필드만 갱신, 나머지는 서버 데이터 유지
//                        )
//                    } else item
//                } else item // 일반 차량은 서버 데이터 그대로
//            }.filterIsInstance<SearchCarItem>()
//
//            _searchCarItems.value = updatedList
//        }
//    }

    fun setupFirebaseListenerForSearchResults() {
        Firebase.database.getReferenceFromUrl(
            "https://trever-ec541-default-rtdb.asia-southeast1.firebasedatabase.app/auctions"
        ).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // 현재 검색 결과에 대해 Firebase 메타 다시 주입
                updateSearchResultWithFirebase()
            }
            override fun onCancelled(error: DatabaseError) { /* no-op */ }
        })
    }

    fun updateSearchResultWithFirebase() {
        viewModelScope.launch {
            val result = _searchResult.value ?: return@launch

            // 1) 서버 DTO -> 화면 모델 변환
            val serverItems: List<SearchCarItem> =
                result.vehicles.map { it.toSearchCarItem() }

            // 2) 경매만 골라 Firebase 조회
            val auctionItems = serverItems.filterIsInstance<SearchCarItem.Auction>()
            val auctionIds = auctionItems.map { it.auctionId.toString() }
            val firebaseAuctions = auctionRepository.getFirebaseAuctionsByIds(auctionIds)

            // 3) Firebase 메타(현재가/마감시간) 주입
            val updatedList = serverItems.map { item ->
                if (item is SearchCarItem.Auction) {
                    val fb = firebaseAuctions[item.auctionId.toString()]
                    if (fb != null) {
                        item.copy(
                            currentPriceWon = fb.currentBidPrice.takeIf { it > 0 } ?: fb.startPrice,
                            endsAtMillis = auctionRepository.parseFirebaseDateToMillis(fb.endAt)
                        )
                    } else item
                } else item
            }

            // 4) 화면에 바인딩되는 단일 소스
            _searchCarItems.value = updatedList
        }
    }

//    fun updateSearchResultWithFirebase() {
//        viewModelScope.launch {
//            val result = _searchResult.value ?: return@launch
//            val auctionItems = result.vehicles.filterIsInstance<SearchCarItem.Auction>()
//            val auctionIds = auctionItems.map { it.auctionId.toString() }
//            val firebaseAuctions = auctionRepository.getFirebaseAuctionsByIds(auctionIds)
//            val updatedList = result.vehicles.map { item ->
//                if (item is SearchCarItem.Auction) {
//                    val fb = firebaseAuctions[item.auctionId.toString()]
//                    if (fb != null) {
//                        item.copy(
//                            currentPriceWon = fb.currentBidPrice.takeIf { it > 0 } ?: fb.startPrice,
//                            endsAtMillis = auctionRepository.parseFirebaseDateToMillis(fb.endAt)
//                        )
//                    } else item
//                } else item
//            }.filterIsInstance<SearchCarItem>()
//
//            _searchCarItems.value = updatedList
//        }
//    }

//    fun updateSearchResultWithFirebase() {
//        viewModelScope.launch {
//            val result = _searchResult.value ?: return@launch
//            val auctionItems = result.vehicles.filterIsInstance<SearchCarItem.Auction>()
//            val auctionIds = auctionItems.map { it.auctionId.toString() }
//            val firebaseAuctions = auctionRepository.getFirebaseAuctionsByIds(auctionIds)
//            val updatedList = result.vehicles.map { item ->
//                if (item is SearchCarItem.Auction) {
//                    val fb = firebaseAuctions[item.auctionId.toString()]
//                    if (fb != null) {
//                        item.copy(
//                            currentPrice = fb.currentBidPrice.takeIf { it > 0 } ?: fb.startPrice,
//                            endsAtMillis = /* 파싱 함수로 변환 */,
//                            // 기타 필요한 필드
//                        )
//                    } else item
//                } else item
//            }
//            _searchResult.value = result.copy(vehicles = updatedList)
//        }
//    }



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
        lastSearchRequest = request
        currentPage = 0
        isLastPage = false
        searchVehicles(request)
    }

    fun loadNextPage() {
        if (isLoadingPage || isLastPage) return
        if ((_searchResult.value?.vehicles?.isEmpty() == true)) return
        val baseRequest = lastSearchRequest ?: return
        val nextRequest = baseRequest.copy(
            page = currentPage + 1,
            keyword = searchText.value.trim().takeIf { it.isNotEmpty() }
        )

        Log.d("SearchViewModel","loadNextPage: currentPage=$currentPage, baseRequest=$baseRequest")
        isLoadingPage = true
        viewModelScope.launch {
            val response = api.searchVehicles(nextRequest)
            if (response.success) {
                val prevList = _searchResult.value?.vehicles ?: emptyList()
                val newList = prevList + (response.data?.vehicles ?: emptyList())
                _searchResult.value = response.data?.copy(vehicles = newList)
                currentPage++
                // 마지막 페이지 조건: 받아온 차량이 0개이거나, 이번에 받아온 차량이 10개 미만이거나, 이미 받은 차량 수와 서버 totalCount가 같을 때
                val pageSize = 10
                val receivedCount = response.data?.vehicles?.size ?: 0
                val totalLoaded = newList.size
                val totalCount = response.data?.totalCount ?: -1
                isLastPage = receivedCount == 0 || receivedCount < pageSize || (totalCount > 0 && totalLoaded >= totalCount)
                updateSearchResultWithFirebase()
                Log.d("SearchViewModel", "loadNextPage: response=${response.data}, vehicles=${response.data?.vehicles?.size}, isLastPage=$isLastPage, totalLoaded=$totalLoaded, totalCount=$totalCount")
            }
            isLoadingPage = false
        }
    }



    private val _searchResult = MutableStateFlow<VehicleSearchResponse?>(null)
    val searchResult: StateFlow<VehicleSearchResponse?> = _searchResult

    fun searchVehicles(request: VehicleSearchRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d("SearchViewModel", "searchVehicles 요청: $request") // 요청 파라미터 로그
                val response = api.searchVehicles(request)
                Log.d("SearchViewModel", "searchVehicles 결과: $response")
                if (response.success) {
                    _searchResult.value = response.data

                    // ✅ 페이징 상태 초기화 + 마지막 페이지 판정
                    currentPage = 0
                    val pageSize = request.size
                    val receivedCount = response.data?.vehicles?.size ?: 0
                    val totalCount = response.data?.totalCount ?: -1
                    isLastPage =
                        receivedCount == 0 ||            // 0건이면 더 없음
                                receivedCount < pageSize ||       // 페이지 사이즈보다 적게 오면 마지막
                                (totalCount > 0 && receivedCount >= totalCount)

                    updateSearchResultWithFirebase()
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
                val response = api.getCarModels(manufacturer, carName)
                if (response.success) {
                    _carModels.value = response.data
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
                val response = api.getCarNames(manufacturer)
                if (response.success) {
                    _carNames.value = response.data
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchRecentSearches() {
        viewModelScope.launch {
            val response = api.getRecentSearches()
            if (response.success) {
                _recentSearches.value = response.data
            }
        }
    }

    fun fetchManufacturers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.getManufacturers()
                if (response.success) {
                    _manufacturerCategories.value = response.data
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
}

