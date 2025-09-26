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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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
                val startAtMillis = parseFirebaseDateToMillis(firebaseAuction.startAt)
                val endsAtMillis = parseFirebaseDateToMillis(firebaseAuction.endAt)
                Log.d("AuctionRepository", "auctionId=${auctionCar.auctionId}, startAt=${firebaseAuction.startAt}, startAtMillis=$startAtMillis, endAt=${firebaseAuction.endAt}, endsAtMillis=$endsAtMillis")
                // Firebase 데이터로 업데이트
                auctionCar.copy(
                    currentPriceWon = firebaseAuction.currentBidPrice.takeIf { it > 0 }
                        ?: firebaseAuction.startPrice,
                    // firebaseAuction.endAt이 이제 Long 타입이므로 직접 사용
                    endsAtMillis = parseFirebaseDateToMillis(firebaseAuction.endAt),
                    startAtMillis = parseFirebaseDateToMillis(firebaseAuction.startAt)

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


    public fun parseFirebaseDateToMillis(dateStr: String?): Long {
        if (dateStr.isNullOrBlank()) return 0L
        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm"
        )
        for (pattern in patterns) {
            try {
                val sdf = java.text.SimpleDateFormat(pattern, java.util.Locale.getDefault())
                sdf.timeZone = java.util.TimeZone.getTimeZone("Asia/Seoul")
                return sdf.parse(dateStr)?.time ?: throw IllegalArgumentException("Null date")
            } catch (e: java.text.ParseException) {
                // 다음 패턴 시도
            }
        }
        throw java.text.ParseException("Unparseable date: \"$dateStr\"", 0)
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


    suspend fun placeBid(auctionId: Int, bidPrice: Long): Flow<Result<BidData>> = flow {
        try {
            val request = BidRequest(auctionId, bidPrice)
            val response = auctionApi.placeBid(request)
            if (response.success) {
                response.data?.let {
                    emit(Result.success(it))
                } ?: emit(Result.failure(Exception("입찰 데이터가 null입니다")))
            } else {
                emit(Result.failure(Exception(response.message)))
            }
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val message = try {
                val json = Json.parseToJsonElement(errorBody ?: "")
                json.jsonObject["message"]?.jsonPrimitive?.content ?: "알 수 없는 오류"
            } catch (_: Exception) {
                errorBody ?: e.message()
            }
            emit(Result.failure(Exception(message)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun getFirebaseAuctionsByIds(ids: List<String>): Map<String, FirebaseAuction> {
        val all = getFirebaseAuctions()
        return all.filterKeys { it in ids }
    }


}