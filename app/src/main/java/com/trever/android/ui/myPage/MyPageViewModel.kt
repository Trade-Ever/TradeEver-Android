package com.trever.android.ui.myPage

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trever.android.data.repository.AuthRepository
import com.trever.android.data.repository.MyPageRepository
import com.trever.android.domain.model.RecentlyViewedCar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 화면(Screen)에서 필요로 하는 데이터 모델들
data class UserProfile(
    val nickname: String = "트레버",
    val email: String = "trever@example.com",
    val phoneNumber: String? = null,
    val address: String? = null,
    val birthday: String? = null,
    val profileImageUrl: String? = null
)

data class AccountInfo(
    val accountName: String = "내 계좌",
    val balance: Long = 1_234_567,
    val bankName: String? = "트레버 은행",
    val accountNumber: String = "123-456-789012"
)

class MyPageViewModel(
    private val myPageRepository: MyPageRepository,
    private val authRepository: AuthRepository // 유저 정보 등을 위해 AuthRepository도 사용
) : ViewModel() {

    // --- StateFlow 정의 --- //

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _accountInfo = MutableStateFlow(AccountInfo())
    val accountInfo: StateFlow<AccountInfo> = _accountInfo.asStateFlow()

    private val _recentlyViewedCars = MutableStateFlow<List<RecentlyViewedCar>>(emptyList())
    val recentlyViewedCars: StateFlow<List<RecentlyViewedCar>> = _recentlyViewedCars.asStateFlow()

    init {
        // ViewModel이 생성될 때 초기 데이터를 로드합니다.
        loadRecentlyViewedCars()
        // TODO: loadUserProfile(authRepository), loadAccountInfo() 등 초기화 로직 추가 필요
    }

    // --- 화면(Screen)에서 호출하는 함수들 --- //

    fun loadRecentlyViewedCars() {
        viewModelScope.launch {
            myPageRepository.getRecentlyViewedCars() // MyPageRepository 사용
                .onSuccess { cars ->
                    _recentlyViewedCars.value = cars
                }
                .onFailure { e ->
                    Log.e("MyPageViewModel", "최근 본 차량 로드 실패", e)
                }
        }
    }

    fun updateUserProfile(name: String, phone: String?, address: String?, birthday: String?, imageUri: String?) {
        viewModelScope.launch {
            // TODO: 실제 프로필 업데이트 API 호출 로직 구현 (authRepository 사용)
            _userProfile.update { currentState ->
                currentState.copy(
                    nickname = name,
                    phoneNumber = phone,
                    address = address,
                    birthday = birthday,
                    profileImageUrl = imageUri ?: currentState.profileImageUrl
                )
            }
            Log.d("MyPageViewModel", "Profile Updated: $name, $phone, $address, $birthday, $imageUri")
        }
    }

    fun charge(amount: Long) {
        viewModelScope.launch {
            // TODO: 실제 충전 API 호출 로직 구현
            _accountInfo.update { it.copy(balance = it.balance + amount) }
            Log.d("MyPageViewModel", "Charge: $amount")
        }
    }

    fun withdraw(amount: Long) {
        viewModelScope.launch {
            // TODO: 실제 출금 API 호출 로직 구현
            _accountInfo.update { it.copy(balance = it.balance - amount) }
            Log.d("MyPageViewModel", "Withdraw: $amount")
        }
    }

    fun onLogoutClicked() {
        viewModelScope.launch {
            // TODO: 실제 로그아웃 로직 구현 (authRepository 사용)
            Log.d("MyPageViewModel", "Logout clicked")
        }
    }

    // 찜한 차량 기능은 아직 구현되지 않았으므로 임시로 남겨둡니다.
    fun loadLikedCars() {
        viewModelScope.launch {
            Log.d("MyPageViewModel", "Liked cars loaded (dummy)")
        }
    }
}
