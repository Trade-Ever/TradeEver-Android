package com.trever.android.data.network.service // 실제 패키지 경로에 맞게 수정

import com.trever.android.data.network.dto.SellCarRequest
import com.trever.android.data.network.dto.SellCarResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface CarApiService {

    /**
     * 차량 판매 정보를 서버에 등록합니다.
     *
     * @param sellCarRequest 등록할 차량 정보 DTO
     * @return 서버 응답 DTO를 포함하는 Response 객체
     */
    @POST("api/vehicles") // TODO: 실제 API 엔드포인트로 반드시 변경하세요! 예: "listings" 또는 "vehicles/sell" 등
    suspend fun registerCar(@Body sellCarRequest: SellCarRequest): Response<SellCarResponse>

    // 필요하다면 여기에 다른 API 함수들을 추가할 수 있습니다.
    // 예:
    // @GET("api/v1/cars/{carId}")
    // suspend fun getCarDetails(@Path("carId") carId: String): Response<CarDetailResponse>
}
