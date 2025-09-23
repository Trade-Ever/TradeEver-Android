package com.trever.android.ui.myPage

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trever.android.data.network.ApiClient
import com.trever.android.data.remote.UserInfo
import com.trever.android.data.repository.AuthRepository
import com.trever.android.data.repository.MyPageRepository
import com.trever.android.data.repository.ProfileRepository
import com.trever.android.data.repository.WalletRepository
import com.trever.android.domain.model.AuctionCar
import com.trever.android.domain.model.RecentlyViewedCar
import com.trever.android.data.remote.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AccountInfo(
    val accountName: String = "내 계좌",
    val balance: Long = 1_234_567,
    val bankName: String? = "트레버 은행",
    val accountNumber: String = "123-456-789012"
)

class MyPageViewModel(
    private val myPageRepository: MyPageRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val walletRepository = WalletRepository(ApiClient.walletApi)
    private val profileRepository = ProfileRepository(ApiClient.profileApi)

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _accountInfo = MutableStateFlow(AccountInfo())
    val accountInfo: StateFlow<AccountInfo> = _accountInfo.asStateFlow()

    private val _recentlyViewedCars = MutableStateFlow<List<RecentlyViewedCar>>(emptyList())
    val recentlyViewedCars: StateFlow<List<RecentlyViewedCar>> = _recentlyViewedCars.asStateFlow()

    private val _likedCars = MutableStateFlow<List<AuctionCar>>(emptyList())
    val likedCars: StateFlow<List<AuctionCar>> = _likedCars.asStateFlow()

    private val _balance = MutableStateFlow<Long?>(null)
    val balance: StateFlow<Long?> = _balance

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        loadRecentlyViewedCars()
        // loadLikedCars() // TODO: 찜하기 기능 구현 후 주석 해제
        loadProfile()
        refreshBalance()
    }

    fun loadRecentlyViewedCars() {
        viewModelScope.launch {
            myPageRepository.getRecentlyViewedCars()
                .onSuccess { cars -> _recentlyViewedCars.value = cars }
                .onFailure { e -> Log.e("MyPageViewModel", "최근 본 차량 로드 실패", e) }
        }
    }

    fun loadLikedCars() {
        // TODO: 찜한 차량 목록 불러오기 API 구현 필요
        Log.d("MyPageViewModel", "loadLikedCars() 호출되었으나, 기능이 아직 구현되지 않았습니다.")
        // 현재는 비어있는 리스트를 반환하도록 처리
        _likedCars.value = emptyList()
    }

    fun loadProfile() {
        viewModelScope.launch {
            profileRepository.getProfile()
                .onSuccess { profile -> _userProfile.value = profile }
                .onFailure { e -> Log.e("MyPageViewModel", "프로필 조회 실패", e) }
        }
    }

    fun updateProfile(userInfo: UserInfo, imageUri: Uri?) {
        viewModelScope.launch {
            profileRepository.updateProfile(userInfo, imageUri)
                .onSuccess { loadProfile() }
                .onFailure { e -> Log.e("MyPageViewModel", "프로필 수정 실패", e) }
        }
    }

    fun deposit(amount: Long) {
        viewModelScope.launch {
            walletRepository.deposit(amount)
                .onSuccess { refreshBalance() }
                .onFailure { e -> Log.e("MyPageViewModel", "충전 실패", e) }
        }
    }

    fun withdraw(amount: Long) {
        viewModelScope.launch {
            walletRepository.withdraw(amount)
                .onSuccess { refreshBalance() }
                .onFailure { e -> Log.e("MyPageViewModel", "출금 실패: ${e.message}", e) }
        }
    }

    fun refreshBalance() {
        viewModelScope.launch {
            walletRepository.getBalance()
                .onSuccess { newBalance -> _balance.value = newBalance }
                .onFailure { e -> Log.e("MyPageViewModel", "잔액 조회 실패: ${e.message}", e) }
        }
    }

    fun onLogoutClicked() {
        Log.d("MyPageViewModel", "로그아웃 버튼 클릭됨")
        // TODO: 로그아웃 로직 구현
    }
}