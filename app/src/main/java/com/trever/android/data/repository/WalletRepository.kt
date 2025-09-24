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
                Result.success(Unit) // response.data를 사용하지 않으므로 문제 없음
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
                Result.success(Unit) // response.data를 사용하지 않으므로 문제 없음
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
            Log.d("WalletRepository", "잔액 조회 요청")
            val response = api.getBalance() // response.data is Long?
            Log.d("WalletRepository", "잔액 조회 응답: $response")

            if (response.success && response.data != null) {
                Result.success(response.data) // response.data가 Long으로 스마트 캐스트됨
            } else {
                val errorMessage = response.message ?: "잔액 정보를 가져오지 못했습니다 (data is null: ${response.data == null})"
                Log.e("WalletRepository", "잔액 조회 API 실패: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("WalletRepository", "잔액 조회 중 예외 발생", e)
            Result.failure(e)
        }
    }
}