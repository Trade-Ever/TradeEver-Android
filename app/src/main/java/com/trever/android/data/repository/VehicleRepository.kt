package com.trever.android.data.repository

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Log
import com.google.gson.Gson
import com.trever.android.data.network.ApiClient
import com.trever.android.data.remote.*
import com.trever.android.domain.model.AuctionCar
import com.trever.android.domain.model.CarRegistrationRequest
import com.trever.android.domain.model.VehicleDetail
import com.trever.android.domain.model.VehicleSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream

class VehicleRepository(
    private val api: VehicleApi = ApiClient.vehicleApi,
    private val context: Context? = null,
    private val gson: Gson = Gson()
) {

    suspend fun downloadToCache(contractId: Long): File {
        val response = api.getContractPdf(contractId)
        if (response == null || response.body() == null) {
            throw IllegalStateException("계약서 PDF 응답이 없습니다.")
        }
        val body = response.body()!!
        val file = File(context!!.cacheDir, "contract_$contractId.pdf")
        body.byteStream().use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return file
    }

    // PdfRenderer 열기
    suspend fun openRenderer(pdfFile: File): PdfRenderer = withContext(Dispatchers.IO) {
        val pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
        PdfRenderer(pfd)
    }

    // 특정 페이지를 비트맵으로 렌더링
    suspend fun renderPage(renderer: PdfRenderer, index: Int, width: Int): Bitmap =
        withContext(Dispatchers.IO) {
            renderer.openPage(index).use { page ->
                // 종횡비대로 높이 계산
                val ratio = page.height.toFloat() / page.width.toFloat()
                val bmp = Bitmap.createBitmap(width, (width * ratio).toInt(), Bitmap.Config.ARGB_8888)
                page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bmp
            }
        }


    // Downloads/ 에 저장 (스코프드 스토리지)
    suspend fun saveToDownloads(pdfFile: File, displayName: String = pdfFile.name): Uri =
        withContext(Dispatchers.IO) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, displayName)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
            }

            val resolver = context!!.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                ?: error("Downloads 항목 생성 실패")

            resolver.openOutputStream(uri)?.use { out ->
                pdfFile.inputStream().use { it.copyTo(out) }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
            uri
        }

    suspend fun checkCarNumber(carNumber: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = api.checkCarNumber(carNumber)
            if (response.success) {
                Result.success(response.data.exists)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleLike(vehicleId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = api.toggleFavorite(vehicleId)
            if (response.success) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMyVehicles(page: Int = 0, size: Int = 10): Result<MyVehiclesResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getMyVehicles(page, size)
                if (response.success) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Log.e("VehicleRepository", "Error fetching my vehicles", e)
                Result.failure(e)
            }
        }
    }

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

    suspend fun registerVehicle(request: CarRegistrationRequest, photoUris: List<Uri>): Result<String> = withContext(Dispatchers.IO) {
        try {
            val requestJson = gson.toJson(request)
            val requestBody = requestJson.toRequestBody("application/json".toMediaTypeOrNull())

            val photoParts = if (photoUris.isNotEmpty() && context != null) {
                photoUris.mapIndexed { index, uri ->
                    val fileName = "image_${System.currentTimeMillis()}_$index.jpg"
                    val file = uriToFile(context, uri, fileName)
                    val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("photos", file.name, requestFile)
                }
            } else {
                emptyList()
            }

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

    suspend fun getManufacturersDataForSelection(): Result<Map<String, List<String>>> = withContext(Dispatchers.IO) {
        try {
            coroutineScope { 
                val domesticManufacturersDeferred = async { api.getManufacturersByCategory("국산") }
                val importedManufacturersDeferred = async { api.getManufacturersByCategory("수입") }

                val domesticResponse = domesticManufacturersDeferred.await()
                val importedResponse = importedManufacturersDeferred.await()

                if (domesticResponse.success && domesticResponse.data != null && importedResponse.success && importedResponse.data != null) {
                    val map = mapOf(
                        "국산" to domesticResponse.data,
                        "수입" to importedResponse.data
                    )
                    Result.success(map)
                } else {
                    val errorMessage = if (!domesticResponse.success) domesticResponse.message else importedResponse.message
                    Result.failure(Exception(errorMessage ?: "제조사 목록을 불러오는 데 실패했습니다."))
                }
            }
        } catch (e: Exception) {
            Log.e("VehicleRepository", "Error fetching manufacturers for selection", e)
            Result.failure(e)
        }
    }

    suspend fun getCarNameList(category: String, manufacturer: String): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getCarNames(category, manufacturer)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Failed to load car names for $manufacturer"))
            }
        } catch (e: Exception) {
            Log.e("VehicleRepository", "Error fetching car names for $manufacturer", e)
            Result.failure(e)
        }
    }

    suspend fun getModelNameList(category: String, manufacturer: String, carName: String): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getModelNames(category, manufacturer, carName)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Failed to load model names for $carName"))
            }
        } catch (e: Exception) {
            Log.e("VehicleRepository", "Error fetching model names for $carName", e)
            Result.failure(e)
        }
    }

    suspend fun getYearList(
        category: String,
        manufacturer: String,
        carName: String,
        modelName: String
    ): Result<List<Int>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getYears(category, manufacturer, carName, modelName)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Failed to load years for $modelName"))
            }
        } catch (e: Exception) {
            Log.e("VehicleRepository", "Error fetching years for $modelName", e)
            Result.failure(e)
        }
    }

    suspend fun selectBuyer(vehicleId: String, buyerId: Long): Result<SelectBuyerResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.selectBuyer(vehicleId, buyerId)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBuyRequests(vehicleId: String): Result<List<BuyApplyData>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getBuyRequests(vehicleId)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun applyBuy(vehicleId: String): ApiResponse<BuyApplyData> {
        return api.applyBuy(vehicleId)
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


}