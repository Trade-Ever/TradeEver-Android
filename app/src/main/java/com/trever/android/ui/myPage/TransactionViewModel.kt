package com.trever.android.ui.myPage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trever.android.data.repository.TransactionRepository
import com.trever.android.domain.model.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class TransactionType {
    SALES, PURCHASES
}

class TransactionViewModel(private val transactionRepository: TransactionRepository) : ViewModel() {

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadTransactions(type: TransactionType) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = when (type) {
                TransactionType.SALES -> transactionRepository.getSalesHistory()
                TransactionType.PURCHASES -> transactionRepository.getPurchaseHistory()
            }

            result
                .onSuccess { data ->
                    _transactions.value = data
                    Log.d("TransactionViewModel", "${type.name} 내역 로드 성공: ${data.size}개")
                }
                .onFailure { e ->
                    _transactions.value = emptyList()
                    Log.e("TransactionViewModel", "${type.name} 내역 로드 실패", e)
                }
            _isLoading.value = false
        }
    }
}
