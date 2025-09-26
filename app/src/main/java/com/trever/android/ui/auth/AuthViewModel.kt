package com.trever.android.ui.auth

import android.content.Intent
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.trever.android.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState = _loginState.asStateFlow()

    private val _profileComplete = MutableStateFlow<Boolean?>(null)
    val profileComplete = _profileComplete.asStateFlow()

    // 추가 정보 입력값 상태
    var name by mutableStateOf("")
    var phone by mutableStateOf("")
    var birth by mutableStateOf("")
    var region by mutableStateOf("")

    fun saveProfile(
        name: String,
        phone: String,
        birth: String,
        region: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            authRepository.completeProfile(
                name = name,
                phone = phone,
                locationCity = region,
                birthDate = birth
            ).onSuccess {
                // 성공 시
                onComplete()
            }.onFailure {
                Log.e("AuthViewModel", "프로필 저장 실패: ${it.message}")
            }
        }
    }

    fun setProfileComplete(value: Boolean) {
        _profileComplete.value = value
    }

    fun getGoogleSignInIntent(): Intent {
        return authRepository.getGoogleSignInIntent()
    }

//    fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
//        try {
//            val account = task.getResult(ApiException::class.java)
//            viewModelScope.launch {
//                _loginState.value = LoginState.Loading
//                account.idToken?.let {
//                    authRepository.handleGoogleSignInResult(it)
//                        .onSuccess { _loginState.value = LoginState.Success }
//                        .onFailure { _loginState.value = LoginState.Error("로그인 실패22: ${it.message}") }
//                }
//            }
//        } catch (e: ApiException) {
//            Log.e("AuthViewModel", "Google sign in failed", e)
//            _loginState.value = LoginState.Error("구글 로그인 실패: ${e.statusCode}")
//        }
//    }
// 로그인 결과 처리에서 profileComplete 값 저장
fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
    try {
        val account = task.getResult(ApiException::class.java)
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            account.idToken?.let {
                authRepository.handleGoogleSignInResult(it)
                    .onSuccess { response ->
                        _profileComplete.value = response
                        _loginState.value = LoginState.Success
                    }
                    .onFailure { _loginState.value = LoginState.Error("로그인 실패22: ${it.message}") }
            }
        }
    } catch (e: ApiException) {
        _loginState.value = LoginState.Error("구글 로그인 실패: ${e.statusCode}")
    }
}



    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        object Success : LoginState()
        data class Error(val message: String) : LoginState()
    }
}