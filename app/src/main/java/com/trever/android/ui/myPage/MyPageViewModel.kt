package com.trever.android.ui.myPage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
// 간단한 Log import 추가

// 임시 데이터 클래스 (실제 프로젝트에서는 domain 모델 사용 고려)
data class UserProfile(
    val nickname: String = "닉네임",
    val email: String = "nick@example.com",
    val profileImageUrl: String? = null // 실제 이미지 URL 또는 로컬 리소스 ID
)

data class AccountInfo(
    val balance: String = "10,000원",
    val accountNumber: String = "내 계좌" // 또는 "123-456-7890 예금주"
)

class MyPageViewModel : ViewModel() {

    private val _userProfile = MutableStateFlow(UserProfile()) // 기본값 또는 로드
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _accountInfo = MutableStateFlow(AccountInfo())
    val accountInfo: StateFlow<AccountInfo> = _accountInfo.asStateFlow()

    // TODO: 사용자 정보, 계좌 정보 로드 로직 추가
    init {
        // 예시: viewModelScope.launch { loadUserProfile() }
    }

    fun onChargeClicked() {
        // TODO: 충전 로직 연결
        Log.d("MyPageViewModel", "충전 버튼 클릭됨")
    }

    fun onWithdrawClicked() {
        // TODO: 출금 로직 연결
        Log.d("MyPageViewModel", "출금 버튼 클릭됨")
    }

    fun onLogoutClicked() {
        // TODO: 로그아웃 로직 연결
        Log.d("MyPageViewModel", "로그아웃 버튼 클릭됨")
        // 예: viewModelScope.launch { authRepository.logout() }
        //      tokenStore.clear() 등
    }
}