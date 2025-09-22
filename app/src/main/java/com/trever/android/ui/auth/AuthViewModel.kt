package com.trever.android.ui.auth

import android.content.Intent
import android.util.Log
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

    fun getGoogleSignInIntent(): Intent {
        return authRepository.getGoogleSignInIntent()
    }

    fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            viewModelScope.launch {
                _loginState.value = LoginState.Loading
                account.idToken?.let {
                    authRepository.handleGoogleSignInResult(it)
                        .onSuccess { _loginState.value = LoginState.Success }
                        .onFailure { _loginState.value = LoginState.Error("로그인 실패22: ${it.message}") }
                }
            }
        } catch (e: ApiException) {
            Log.e("AuthViewModel", "Google sign in failed", e)
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