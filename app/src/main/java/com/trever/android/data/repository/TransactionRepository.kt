package com.trever.android.data.repository

import android.util.Log
import com.trever.android.data.remote.TransactionApi
import com.trever.android.data.remote.TransactionDto
import com.trever.android.domain.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 거래 내역 관련 데이터를 처리하는 저장소입니다.
 */
class TransactionRepository(private val transactionApi: TransactionApi) {

    /**
     * "판매 내역" 목록을 서버에서 가져옵니다.
     */
    suspend fun getSalesHistory(): Result<List<Transaction>> = withContext(Dispatchers.IO) {
        try {
            Log.d("TransactionRepository", "판매 내역 로드를 시작합니다.")
            val response = transactionApi.getSalesHistory() // response.data is List<TransactionDto>?
            Log.d("TransactionRepository", "판매 내역 서버 응답: $response")

            if (response.success && response.data != null) {
                val domainModels = response.data.map { it.toDomainModel() } // response.data가 스마트 캐스트됨
                Log.d("TransactionRepository", "판매 내역 파싱 성공: ${domainModels.size}개")
                Result.success(domainModels)
            } else {
                val errorMessage = response.message ?: "판매 내역을 가져오지 못했습니다 (data is null: ${response.data == null})"
                Log.e("TransactionRepository", "판매 내역 API 실패: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("TransactionRepository", "판매 내역 로드 중 예외 발생", e)
            Result.failure(e)
        }
    }

    /**
     * "구매 내역" 목록을 서버에서 가져옵니다.
     */
    suspend fun getPurchaseHistory(): Result<List<Transaction>> = withContext(Dispatchers.IO) {
        try {
            Log.d("TransactionRepository", "구매 내역 로드를 시작합니다.")
            val response = transactionApi.getPurchaseHistory() // response.data is List<TransactionDto>?
            Log.d("TransactionRepository", "구매 내역 서버 응답: $response")

            if (response.success && response.data != null) {
                val domainModels = response.data.map { it.toDomainModel() } // response.data가 스마트 캐스트됨
                Log.d("TransactionRepository", "구매 내역 파싱 성공: ${domainModels.size}개")
                Result.success(domainModels)
            } else {
                val errorMessage = response.message ?: "구매 내역을 가져오지 못했습니다 (data is null: ${response.data == null})"
                Log.e("TransactionRepository", "구매 내역 API 실패: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            // 원래 로그 메시지 유지, 스택 트레이스도 함께 출력
            Log.e("TransactionRepository", "구매 내역 로드 실패", e) 
            Result.failure(e)
        }
    }
}

/**
 * API 응답 모델인 [TransactionDto]를
 * UI에서 사용하는 도메인 모델인 [Transaction]으로 변환합니다.
 */
private fun TransactionDto.toDomainModel(): Transaction {
    return Transaction(
        transactionId = this.transactionId,
        vehicleId = this.vehicleId,
        vehicleName = this.vehicleName,
        buyerName = this.buyerName,      // <--- DTO의 buyerName을 전달
        sellerName = this.sellerName,    // <--- DTO의 sellerName을 전달
        finalPrice = this.finalPrice,
        status = this.status,
        createdAt = this.createdAt,
        contractPdfUrl = this.contractPdfUrl,
        contractId = this.contractId // <--- DTO의 contractId를 전달
    )
}
