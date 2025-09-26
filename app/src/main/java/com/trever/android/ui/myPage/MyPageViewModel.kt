package com.trever.android.ui.myPage

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.trever.android.data.auth.TokenStore
import com.trever.android.data.network.ApiClient
import com.trever.android.data.remote.UserInfo
import com.trever.android.data.remote.UserProfile
import com.trever.android.data.repository.AuctionRepository
import com.trever.android.data.repository.AuthRepository
import com.trever.android.data.repository.MyPageRepository
import com.trever.android.data.repository.ProfileRepository
import com.trever.android.data.repository.WalletRepository
import com.trever.android.domain.model.AuctionCar
import com.trever.android.domain.model.RecentlyViewedCar
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class LogoutProcessState {
    object Idle : LogoutProcessState()
    object Loading : LogoutProcessState()
    object CompletedShowDialog : LogoutProcessState()
}

data class AccountInfo(
    val accountName: String = "내 계좌",
    val balance: Long = 1_234_567,
    val bankName: String? = "트레버 은행",
    val accountNumber: String = "123-456-789012"
)

private fun RecentlyViewedCar.toAuctionCar(): AuctionCar {
    val effectiveTitle = if (!manufacturer.isNullOrBlank() && !model.isNullOrBlank()) {
        "$manufacturer $model"
    } else {
        title
    }
    return AuctionCar(
        id = this.id,
        title = effectiveTitle,
        year = this.year,
        mileageKm = this.mileageKm,
        imageUrl = this.imageUrl,
        currentPriceWon = this.priceWon, 
        manufacturer = this.manufacturer,
        model = this.model,
        tags = emptyList(), 
        mainOptions = this.mainOptions ?: emptyList(),
        startAtMillis = 0L, 
        endsAtMillis = 0L,  
        liked = this.isFavorite ?: false,
        auctionId = if (this.isAuction == true) this.id.toLongOrNull() else null,
        transactionType = if (this.isAuction == true) "경매" else "일반"
    )
}

class MyPageViewModel(
    private val myPageRepository: MyPageRepository,
    private val authRepository: AuthRepository,
    private val tokenStore: TokenStore,
    private val auctionRepository: AuctionRepository
) : ViewModel() {

    private val walletRepository = WalletRepository(ApiClient.walletApi)
    private val profileRepository = ProfileRepository(ApiClient.profileApi)

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _accountInfo = MutableStateFlow(AccountInfo())
    val accountInfo: StateFlow<AccountInfo> = _accountInfo.asStateFlow()

    private val _recentlyViewedCars = MutableStateFlow<List<AuctionCar>>(emptyList())
    val recentlyViewedCars: StateFlow<List<AuctionCar>> = _recentlyViewedCars.asStateFlow()

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

    private val database = Firebase.database.getReferenceFromUrl(
        "https://trever-ec541-default-rtdb.asia-southeast1.firebasedatabase.app/auctions"
    )
    private var valueEventListener: ValueEventListener? = null

    init {
        loadRecentlyViewedCars()
        loadLikedCars()
        loadProfile()
        refreshBalance()
        setupFirebaseListener() 
    }

    fun loadRecentlyViewedCars() {
        viewModelScope.launch {
            myPageRepository.getRecentlyViewedCars()
                .onSuccess { recentCarsDto ->
                    val auctionCars = recentCarsDto.map { it.toAuctionCar() }
                    val updatedCars = auctionRepository.updateAuctionsWithFirebaseData(auctionCars)
                    _recentlyViewedCars.value = updatedCars
                }
                .onFailure { e ->
                    Log.e("MyPageViewModel", "최근 본 차량 로드 실패", e)
                }
        }
    }

    fun loadLikedCars() {
        viewModelScope.launch {
            myPageRepository.getLikedCars() 
                .onSuccess { likedAuctionCars ->
                    val updatedCars = auctionRepository.updateAuctionsWithFirebaseData(likedAuctionCars)
                    _likedCars.value = updatedCars
                }
                .onFailure { e ->
                    Log.e("MyPageViewModel", "찜한 차량 로드 실패", e)
                }
        }
    }

    private fun setupFirebaseListener() {
        if (valueEventListener != null) return 

        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                viewModelScope.launch {
                    if (_recentlyViewedCars.value.isNotEmpty()) {
                        val updatedRecentlyViewed = auctionRepository.updateAuctionsWithFirebaseData(_recentlyViewedCars.value)
                        _recentlyViewedCars.update { updatedRecentlyViewed }
                    }
                    if (_likedCars.value.isNotEmpty()) {
                        val updatedLikedCars = auctionRepository.updateAuctionsWithFirebaseData(_likedCars.value)
                        _likedCars.update { updatedLikedCars }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MyPageViewModel", "Firebase listener cancelled", error.toException())
                _message.value = "실시간 데이터 동기화에 실패했습니다: ${error.message}"
            }
        }
        database.addValueEventListener(valueEventListener!!)
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
            _logoutProcessState.value = LogoutProcessState.Loading
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
                _logoutProcessState.value = LogoutProcessState.CompletedShowDialog
            }
        }
    }

    fun onLogoutDialogConfirmed() {
        viewModelScope.launch {
            Log.d("MyPageViewModel", "로그아웃 다이얼로그 확인됨, 로그인 화면으로 이동")
            _logoutProcessState.value = LogoutProcessState.Idle
            _navigateToLogin.emit(Unit)
        }
    }

    override fun onCleared() {
        super.onCleared()
        valueEventListener?.let {
            database.removeEventListener(it)
        }
        valueEventListener = null 
        Log.d("MyPageViewModel", "Firebase listener removed and ViewModel cleared")
    }
}
