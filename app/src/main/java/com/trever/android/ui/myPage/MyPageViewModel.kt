package com.trever.android.ui.myPage

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trever.android.data.auth.TokenStore
import com.trever.android.data.network.ApiClient
import com.trever.android.data.remote.UserInfo
import com.trever.android.data.repository.AuthRepository
import com.trever.android.data.repository.MyPageRepository
import com.trever.android.data.repository.ProfileRepository
import com.trever.android.data.repository.WalletRepository
import com.trever.android.domain.model.AuctionCar
import com.trever.android.domain.model.RecentlyViewedCar
import com.trever.android.data.remote.UserProfile
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// 로그아웃 절차 상태 정의
sealed class LogoutProcessState {
    object Idle : LogoutProcessState() // 초기 또는 완료된 상태
    object Loading : LogoutProcessState() // 로그아웃 진행 중 (UI 피드백을 위해 선택적으로 사용 가능)
    object CompletedShowDialog : LogoutProcessState() // 로그아웃 완료, 다이얼로그 표시 요청
}

data class AccountInfo(
    val accountName: String = "내 계좌",
    val balance: Long = 1_234_567,
    val bankName: String? = "트레버 은행",
    val accountNumber: String = "123-456-789012"
)

class MyPageViewModel(
    private val myPageRepository: MyPageRepository,
    private val authRepository: AuthRepository,
    private val tokenStore: TokenStore
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

    private val _logoutProcessState = MutableStateFlow<LogoutProcessState>(LogoutProcessState.Idle)
    val logoutProcessState: StateFlow<LogoutProcessState> = _logoutProcessState.asStateFlow()

    private val _navigateToLogin = MutableSharedFlow<Unit>()
    val navigateToLogin = _navigateToLogin.asSharedFlow()

    init {
        loadRecentlyViewedCars()
        loadLikedCars()
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
        viewModelScope.launch {
            myPageRepository.getLikedCars()
                .onSuccess { cars -> _likedCars.value = cars }
                .onFailure { e -> Log.e("MyPageViewModel", "찜한 차량 로드 실패", e) }
        }
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
        viewModelScope.launch {
            _logoutProcessState.value = LogoutProcessState.Loading // 로딩 상태 (선택적)
            Log.d("MyPageViewModel", "로그아웃 로직 시작")
            try {
                val logoutResult = authRepository.logout()
                if (logoutResult.isSuccess) {
                    Log.d("MyPageViewModel", "API 로그아웃 성공")
                } else {
                    Log.w("MyPageViewModel", "API 로그아웃 실패: ${logoutResult.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.e("MyPageViewModel", "API 로그아웃 중 예외 발생", e)
            } finally {
                Log.d("MyPageViewModel", "로컬 토큰 삭제 시도")
                tokenStore.clear()
                Log.d("MyPageViewModel", "로그아웃 완료, 다이얼로그 표시 요청")
                _logoutProcessState.value = LogoutProcessState.CompletedShowDialog // 다이얼로그 표시 상태로 변경
            }
        }
    }

    fun onLogoutDialogConfirmed() {
        viewModelScope.launch {
            Log.d("MyPageViewModel", "로그아웃 다이얼로그 확인됨, 로그인 화면으로 이동")
            _logoutProcessState.value = LogoutProcessState.Idle // 상태 초기화
            _navigateToLogin.emit(Unit) // 화면 이동 이벤트 발생
        }
    }
}