package com.trever.android.ui.myPage

import android.net.Uri 
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trever.android.data.network.ApiClient
import com.trever.android.data.remote.UserInfo
import com.trever.android.data.remote.UserProfile
import com.trever.android.data.repository.ProfileRepository
import com.trever.android.data.repository.WalletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

//// UserProfile 데이터 클래스는 이전과 동일하게 유지
//data class UserProfile(
//    val nickname: String = "닉네임",
//    val email: String = "nick@example.com",
//    val profileImageUrl: String? = null,
//    val phoneNumber: String? = null,
//    val address: String? = null,
//    val birthday: String? = null
//)
//
//// AccountInfo 데이터 클래스 수정
//data class AccountInfo(
//    val balance: Long = 10000L, // Long 타입으로 변경, 기본값 10,000원
//    val accountNumber: String = "1002-044-******", // 실제 계좌번호 (또는 마스킹된 형태)
//    val bankName: String? = "우리은행",      // 은행 이름
//    val accountName: String? = "내 계좌"    // 계좌 별칭 (예: 주거래 통장)
//) {
//    // UI 표시용 포맷된 잔액 문자열 생성 함수
//    fun getFormattedBalance(): String {
//        return "${NumberFormat.getNumberInstance(Locale.KOREA).format(balance)}원"
//    }
//}

class MyPageViewModel : ViewModel() {

    private val walletRepository = WalletRepository(ApiClient.walletApi)
    private val profileRepository = ProfileRepository(ApiClient.profileApi)

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    private val _balance = MutableStateFlow<Long?>(null)
    val balance: StateFlow<Long?> = _balance

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        // 예시: 앱 시작 시 또는 ViewModel 생성 시 사용자 프로필 및 계좌 정보 로드
        loadProfile() // 프로필 정보 로드 함수 호출 (예시)
        refreshBalance()
//        loadAccountInfo() // 계좌 정보 로드 함수 호출 (예시)
    }

    // 프로필 불러오기
    fun loadProfile() {
        viewModelScope.launch {
            profileRepository.getProfile()
                .onSuccess { profile ->
                    _userProfile.value = profile
                    Log.d("MyPageViewModel", "프로필 불러오기 성공: $profile")
                }
                .onFailure { e ->
                    Log.e("MyPageViewModel", "프로필 조회 실패", e)
                }
        }
    }

    // 프로필 수정
    fun updateProfile(userInfo: UserInfo, imageUri: Uri?) {
        viewModelScope.launch {
            profileRepository.updateProfile(userInfo, imageUri)
                .onSuccess {
                    Log.d("MyPageViewModel", "프로필 수정 성공")
                    loadProfile() // 수정 후 다시 불러오기
                }
                .onFailure { e ->
                    Log.e("MyPageViewModel", "프로필 수정 실패", e)
                }
        }
    }

    // 충전 함수 (바텀시트의 최종 '충전하기' 버튼에서 호출될 함수)
    fun deposit(amount: Long) {
        viewModelScope.launch {
            walletRepository.deposit(amount)
                .onSuccess {
                    // 충전 성공하면 → 최신 잔액 다시 조회
                    refreshBalance()
                    Log.d("MyPageViewModel", "충전 성공: $amount")
                }
                .onFailure { e ->
                    Log.e("MyPageViewModel", "충전 실패", e)
                }
        }
    }

    // 출금 함수 (바텀시트의 최종 '출금하기' 버튼에서 호출될 함수)
    fun withdraw(amount: Long) {
        viewModelScope.launch {
            walletRepository.withdraw(amount)
                .onSuccess {
                    refreshBalance()
                }
                .onFailure { e ->
                    Log.e("MyPageViewModel", "출금 실패: ${e.message}")
                }
        }
    }


    // 잔액 새로고침
    fun refreshBalance() {
        viewModelScope.launch {
            walletRepository.getBalance()
                .onSuccess { newBalance ->
                    Log.d("MyPageViewModel", "서버에서 받은 잔액 = $newBalance")
                    _balance.value = newBalance
                }
                .onFailure { e ->
                    Log.e("MyPageViewModel", "잔액 조회 실패: ${e.message}")
                }
        }
    }


    // 바텀시트를 여는 기존 함수들은 Screen에서 바텀시트 상태를 직접 제어하므로 ViewModel에서는 단순 로그만 남기거나 비워둘 수 있음
    fun onChargeClicked() {
        Log.d("MyPageViewModel", "Charge button clicked. Bottom sheet should be shown by the Screen.")
    }

    fun onWithdrawClicked() {
        Log.d("MyPageViewModel", "Withdraw button clicked. Bottom sheet should be shown by the Screen.")
    }

    fun onLogoutClicked() {
        Log.d("MyPageViewModel", "로그아웃 버튼 클릭됨")
        // TODO: 로그아웃 로직 연결 (데이터 초기화, 화면 전환 등)
        // 예: _userProfile.value = UserProfile() 
        //     _accountInfo.value = AccountInfo()
    }
    
//    // 예시: 사용자 프로필 로드 함수 (실제 구현 필요)
//    private fun loadUserProfile() {
//        viewModelScope.launch {
//            // TODO: SharedPreferences, Room, 서버 API 등에서 프로필 정보 로드
//            _userProfile.value = UserProfile(
//                nickname = "TreverUser",
//                email = "trever@example.com",
//                profileImageUrl = null,
//                phoneNumber = "010-1234-5678",
//                address = "서울시 강남구 테헤란로 123",
//                birthday = "19901225"
//            )
//        }
//    }

//    // 예시: 계좌 정보 로드 함수 (실제 구현 필요)
//    private fun loadAccountInfo() {
//        viewModelScope.launch {
//            // TODO: SharedPreferences, Room, 서버 API 등에서 계좌 정보 로드
//            _accountInfo.value = AccountInfo(
//                balance = 50000L, // 초기 잔액 50,000원
//                accountNumber = "1002-123-456789",
//                bankName = "우리은행",
//                accountName = "내 계좌"
//            )
//        }
//    }
}
