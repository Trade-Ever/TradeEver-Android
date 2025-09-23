package com.trever.android.data.repository

import android.util.Log
import com.trever.android.data.network.ApiClient
import com.trever.android.data.remote.WalletApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WalletRepository(
    private val api: WalletApi = ApiClient.walletApi
) {

    suspend fun deposit(amount: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d("WalletRepository", "충전 요청: $amount")
            val response = api.deposit(amount)
            Log.d("WalletRepository", "충전 응답: $response")

            if (response.success) {
                Result.success(Unit)
            } else {
                Log.e("WalletRepository", "충전 API 실패: ${response.message}")
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Log.e("WalletRepository", "충전 중 예외 발생", e)
            Result.failure(e)
        }
    }

    suspend fun withdraw(amount: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d("WalletRepository", "출금 요청: $amount")
            val response = api.withdraw(amount)
            Log.d("WalletRepository", "출금 응답: $response")

            if (response.success) {
                Result.success(Unit)
            } else {
                Log.e("WalletRepository", "출금 API 실패: ${response.message}")
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Log.e("WalletRepository", "출금 중 예외 발생", e)
            Result.failure(e)
        }
    }

    suspend fun getBalance(): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val response = api.getBalance()
            if (response.success) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}