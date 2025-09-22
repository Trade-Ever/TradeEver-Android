package com.trever.android.data.repository

import com.trever.android.data.network.ApiClient
import com.trever.android.data.remote.WalletApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WalletRepository(
    private val api: WalletApi = ApiClient.walletApi
) {
    // 지갑 충전
    suspend fun deposit(amount: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val res = api.deposit(amount)
            if (res.success) Result.success(Unit)
            else Result.failure(Exception(res.message))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 지갑 출금
    suspend fun withdraw(amount: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val res = api.withdraw(amount)
            if (res.success) Result.success(Unit)
            else Result.failure(Exception(res.message))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 지갑 잔액 조회
    suspend fun getBalance(): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val res = api.getBalance()
            if (res.success) Result.success(res.data)
            else Result.failure(Exception(res.message))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}