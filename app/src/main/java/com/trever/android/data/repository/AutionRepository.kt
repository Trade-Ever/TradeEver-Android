package com.trever.android.data.repository

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.trever.android.data.network.ApiClient
import com.trever.android.data.remote.AuctionApi
import com.trever.android.data.remote.BidData
import com.trever.android.data.remote.BidRequest
import com.trever.android.data.remote.VehicleApi
import com.trever.android.domain.model.AuctionCar
import com.trever.android.domain.model.BidResponse
import com.trever.android.domain.model.FirebaseAuction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.compareTo
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AuctionRepository(
    private val vehicleApi: VehicleApi,
    private val firebaseDatabase: FirebaseDatabase = Firebase.database,
    private val auctionApi: AuctionApi = ApiClient.auctionApi
) {
    private val dbRef = firebaseDatabase.getReferenceFromUrl(
        "https://trever-ec541-default-rtdb.asia-southeast1.firebasedatabase.app/auctions"
    )

    // 기존 경매 목록에 Firebase 정보 적용하기
    suspend fun updateAuctionsWithFirebaseData(auctions: List<AuctionCar>): List<AuctionCar> {
        val firebaseAuctions = getFirebaseAuctions()

        return auctions.map { auctionCar ->
            val firebaseAuction = firebaseAuctions[auctionCar.auctionId.toString()]
            if (firebaseAuction != null) {
                // Firebase 데이터로 업데이트
                auctionCar.copy(
                    currentPriceWon = firebaseAuction.currentBidPrice.takeIf { it > 0 }
                        ?: firebaseAuction.startPrice,
                    endsAtMillis = parseFirebaseDateToMillis(firebaseAuction.endAt)
                )
            } else {
                auctionCar
            }
        }
    }

    private suspend fun getFirebaseAuctions(): Map<String, FirebaseAuction> = suspendCoroutine { continuation ->
            dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val auctionMap = mutableMapOf<String, FirebaseAuction>()

                for (childSnapshot in snapshot.children) {
                    val key = childSnapshot.key
                    if (!key.isNullOrEmpty() && key.toLongOrNull() != null) {
                        val auction = childSnapshot.getValue(FirebaseAuction::class.java)
                        if (auction != null) {
                            auctionMap[key] = auction
                        }
                    }
                }

                continuation.resume(auctionMap)
            }

            override fun onCancelled(error: DatabaseError) {
                continuation.resumeWithException(Exception(error.message))
            }
        })
    }

    private fun parseFirebaseDateToMillis(dateString: String): Long {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = format.parse(dateString.substringBefore('.'))
            date?.time ?: System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)
        } catch (e: Exception) {
            // 현재 시간 + 1일을 기본값으로 설정
            System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)
        }
    }

    suspend fun getAuctionById(auctionId: String): FirebaseAuction? = suspendCoroutine { continuation ->
        dbRef.child(auctionId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val auction = snapshot.getValue(FirebaseAuction::class.java)
                continuation.resume(auction)
            }

            override fun onCancelled(error: DatabaseError) {
                continuation.resumeWithException(Exception(error.message))
            }
        })
    }

    suspend fun placeBid(auctionId: Int, bidPrice: Long, bidderId: Int): Flow<Result<BidData>> = flow {
        try {
            val request = BidRequest(auctionId, bidPrice, bidderId)
            val response = auctionApi.placeBid(request)

            if (response.success) {
                response.data?.let {
                    emit(Result.success(it))
                } ?: emit(Result.failure(Exception("입찰 데이터가 null입니다")))
            } else {
                emit(Result.failure(Exception(response.message)))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun getBidHistory(auctionId: String): List<BidResponse> = suspendCoroutine { continuation ->
        val bidsRef = firebaseDatabase.getReferenceFromUrl(
            "https://trever-ec541-default-rtdb.asia-southeast1.firebasedatabase.app/bids/$auctionId"
        )

        bidsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val bidList = mutableListOf<BidResponse>()

                // Firebase에서 가져온 배열 데이터 순회
                snapshot.children.forEach { childSnapshot ->
                    // null이 아닌 항목만 처리
                    childSnapshot.getValue(BidResponse::class.java)?.let { bid ->
                        bidList.add(bid)
                    }
                }

                // 입찰 ID 기준 내림차순 정렬 (최신순)
                bidList.sortByDescending { it.id }

                Log.d("AuctionRepository", "입찰 내역 ${bidList.size}개 로드됨")
                continuation.resume(bidList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AuctionRepository", "입찰 내역 로드 실패: ${error.message}")
                continuation.resume(emptyList())
            }
        })
    }


}