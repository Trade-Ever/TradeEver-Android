package com.trever.android.ui.myPage

import android.net.Uri // Uri를 직접 사용하진 않지만, profileImageUrl이 Uri 문자열일 수 있음을 시사
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update // .update 확장 함수 임포트
import kotlinx.coroutines.launch

data class UserProfile(
    val nickname: String = "닉네임",
    val email: String = "nick@example.com",
    val profileImageUrl: String? = null, // 실제 이미지 URL 또는 로컬 리소스 ID
    val phoneNumber: String? = null,    // 전화번호 추가
    val address: String? = null,        // 주소 추가
    val birthday: String? = null         // 생일 추가 (YYYYMMDD 형식 또는 다른 형식)
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

    init {
        // 예시: 앱 시작 시 또는 ViewModel 생성 시 사용자 프로필 로드
        // loadUserProfile() 
    }

    // 프로필 업데이트 함수
    fun updateUserProfile(newName: String, newPhoneNumber: String?, newAddress: String?, newBirthday: String?, newProfileImageUrl: String?) {
        viewModelScope.launch {
            _userProfile.update {
                it.copy(
                    nickname = newName,
                    phoneNumber = newPhoneNumber,
                    address = newAddress,
                    birthday = newBirthday,
                    profileImageUrl = newProfileImageUrl
                )
            }
            // TODO: 변경된 프로필 정보를 SharedPreferences, Room, 서버 API 등을 통해 영구 저장하는 로직 추가
            Log.d("MyPageViewModel", "UserProfile updated: ${_userProfile.value}")
        }
    }

    // 예시: 사용자 프로필 로드 함수 (실제 구현 필요)
    private fun loadUserProfile() {
        viewModelScope.launch {
            // TODO: SharedPreferences, Room, 서버 API 등에서 프로필 정보 로드
            // 예시: val loadedProfile = repository.getUserProfile()
            // _userProfile.value = loadedProfile
            // 초기 더미 데이터로 설정 (실제로는 로드 로직 후)
            _userProfile.value = UserProfile(
                nickname = "TreverUser",
                email = "trever@example.com",
                profileImageUrl = null, // 실제 이미지 URL 로드
                phoneNumber = "010-0000-0000",
                address = "서울시 강남구 테헤란로",
                birthday = "19901225"
            )
        }
    }

    fun onChargeClicked() {
        Log.d("MyPageViewModel", "충전 버튼 클릭됨")
        // TODO: 충전 로직 연결
    }

    fun onWithdrawClicked() {
        Log.d("MyPageViewModel", "출금 버튼 클릭됨")
        // TODO: 출금 로직 연결
    }

    fun onLogoutClicked() {
        Log.d("MyPageViewModel", "로그아웃 버튼 클릭됨")
        // TODO: 로그아웃 로직 연결 (데이터 초기화 등)
        // 예: viewModelScope.launch { authRepository.logout() }
        //      tokenStore.clear() 등
        //      _userProfile.value = UserProfile() // 기본값으로 리셋
    }
}
