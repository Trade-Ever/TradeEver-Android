package com.trever.android.data.repository

import android.util.Log
import com.trever.android.data.remote.MyPageApi
import com.trever.android.data.remote.RecentlyViewedCarDto
import com.trever.android.data.remote.toAuctionCar
import com.trever.android.domain.model.AuctionCar
import com.trever.android.domain.model.RecentlyViewedCar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 마이페이지 관련 데이터를 처리하는 저장소입니다.
 */
class MyPageRepository(private val myPageApi: MyPageApi) {

    /**
     * "최근 본 차량" 목록을 서버에서 가져옵니다.
     * 서버 응답(DTO)을 UI 모델로 변환하여 반환합니다.
     */
    suspend fun getRecentlyViewedCars(): Result<List<RecentlyViewedCar>> = withContext(Dispatchers.IO) {
        try {
            val response = myPageApi.getRecentlyViewedCars()
            if (response.success) {
                val domainModels = response.data.vehicles.map { it.toDomainModel() } // <--- 수정됨
                Result.success(domainModels)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Log.e("MyPageRepository", "최근 본 차량 로드 실패", e)
            Result.failure(e)
        }
    }

    /**
     * "찜한 차량" 목록을 서버에서 가져옵니다.
     */
    suspend fun getLikedCars(): Result<List<AuctionCar>> = withContext(Dispatchers.IO) {
        try {
            Log.d("MyPageRepository", "찜 목록 조회 API 호출 시작")
            val response = myPageApi.getLikedCars()
            Log.d("MyPageRepository", "찜 목록 조회 응답: $response")

            if (response.success) {
                val domainModels = response.data.map { it.toAuctionCar() }
                Result.success(domainModels)
            } else {
                Log.e("MyPageRepository", "찜 목록 API 조회 실패: ${response.message}")
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Log.e("MyPageRepository", "찜 목록 조회 중 예외 발생", e)
            Result.failure(e)
        }
    }
}

/**
 * API 응답 모델인 [RecentlyViewedCarDto]를
 * UI에서 사용하는 도메인 모델인 [RecentlyViewedCar]로 변환합니다.
 */
private fun RecentlyViewedCarDto.toDomainModel(): RecentlyViewedCar {
    Log.d("MyPageRepository", "Mapping DTO: id=${this.id}, carName=${this.carName}, mainOptions DTO=${this.mainOptions}")
    val recentlyViewedCar = RecentlyViewedCar(
        id = this.id.toString(),
        title = this.carName ?: "제목 없음",
        year = this.year_value ?: 0,
        mileageKm = this.mileage ?: 0,
        imageUrl = this.representativePhotoUrl,
        priceWon = this.price ?: 0L,
        isAuction = this.isAuction == "Y",
        manufacturer = this.manufacturer,
        model = this.model,
        mainOptions = this.mainOptions, // DTO의 mainOptions를 그대로 전달
        isFavorite = this.isFavorite
    )
    Log.d("MyPageRepository", "Created Domain: id=${recentlyViewedCar.id}, mainOptions Domain=${recentlyViewedCar.mainOptions}")
    return recentlyViewedCar
}
