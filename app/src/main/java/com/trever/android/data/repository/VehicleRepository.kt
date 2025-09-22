package com.trever.android.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import com.trever.android.data.network.ApiClient
import com.trever.android.data.network.ApiClient.vehicleApi
import com.trever.android.data.remote.*
import com.trever.android.domain.model.AuctionCar
import com.trever.android.domain.model.CarRegistrationRequest
import com.trever.android.domain.model.VehicleDetail

import com.trever.android.domain.model.VehicleSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class VehicleRepository(
    private val api: VehicleApi = ApiClient.vehicleApi,
    private val context: Context? = null,
    private val gson: Gson = Gson()

) {
    suspend fun getAuctions(
        page: Int = 0,
        size: Int = 10,
    ): Result<List<AuctionCar>> = withContext(Dispatchers.IO) {
        try {
            val response = api.listVehicles(page, size, isAuction = true)
            if (response.success) {
                val auctionCars = response.data.vehicles.map { it.toAuctionCar() }
                Result.success(auctionCars)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVehicles(
        page: Int = 0,
        size: Int = 10,
    ): Result<List<VehicleSummary>> = withContext(Dispatchers.IO) {
        try {
            val response = api.listVehicles(page, size, isAuction = false)
            if (response.success) {
                val vehicles = response.data.vehicles.map { it.toVehicleSummary() }
                Result.success(vehicles)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Log.e("VehicleRepository", "Error fetching vehicles", e)
            Result.failure(e)
        }
    }



    suspend fun getVehicleDetail(id: String): Result<VehicleDetail> = withContext(Dispatchers.IO) {
        try {
            val response = api.getVehicleDetail(id)
            if (response.success) {
                // VehicleDetailResponse를 도메인 모델인 VehicleDetail로 변환
                val vehicleDetail = response.data.toVehicleDetail()
                Result.success(vehicleDetail)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Log.e("VehicleRepository", "Error fetching vehicle detail", e)
            Result.failure(e)
        }
    }

//    suspend fun registerVehicle(request: CarRegistrationRequest, photoUris: List<Uri>): Result<String> = withContext(Dispatchers.IO) {
//        try {
//            // request JSON 변환 (최신 API 사용)
//            val requestJson = gson.toJson(request)
//            val requestBody = requestJson.toRequestBody("application/json".toMediaTypeOrNull())
//
//            // 파일 대신 문자열을 photos 파라미터로 전송
//            val photoPart = MultipartBody.Part.createFormData("photos", "string")
//
//
//            // API 호출 (단일 문자열로 테스트)
//            val response = api.registerVehicle(requestBody, listOf(photoPart))
//
//            if (response.success) {
//                Result.success("차량 등록 성공: ${response.data}")
//            } else {
//                Result.failure(Exception(response.message ?: "등록 실패"))
//            }
//        } catch (e: Exception) {
//            Log.e("VehicleRepository", "Error registering vehicle", e)
//            Result.failure(e)
//        }
//    }

    suspend fun registerVehicle(request: CarRegistrationRequest, photoUris: List<Uri>): Result<String> = withContext(Dispatchers.IO) {
        try {
            // request JSON 변환
            val requestJson = gson.toJson(request)
            val requestBody = requestJson.toRequestBody("application/json".toMediaTypeOrNull())

            // URI 목록이 비어있지 않고 컨텍스트가 있는 경우에만 실제 파일 전송
            val photoParts = if (photoUris.isNotEmpty() && context != null) {
                photoUris.mapIndexed { index, uri ->
                    // URI를 파일로 변환
                    val fileName = "image_${System.currentTimeMillis()}_$index.jpg"
                    val file = uriToFile(context, uri, fileName)

                    // MultipartBody.Part 생성
                    val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("photos", file.name, requestFile)
                }
            } else {
                // Context가 없거나 사진이 없는 경우 빈 리스트 반환
                emptyList()
            }

            // API 호출
            val response = api.registerVehicle(requestBody, photoParts)

            if (response.success) {
                Result.success("차량 등록 성공: ${response.data}")
            } else {
                Result.failure(Exception(response.message ?: "등록 실패"))
            }
        } catch (e: Exception) {
            Log.e("VehicleRepository", "Error registering vehicle", e)
            Result.failure(e)
        }
    }

    private fun uriToFile(context: Context, uri: Uri, fileName: String): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File(context.cacheDir, fileName)

        inputStream?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }

        return tempFile
    }



    // 필요한 경우 다른 메서드 구현
}