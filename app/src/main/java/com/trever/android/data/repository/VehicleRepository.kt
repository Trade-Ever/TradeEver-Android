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

        val file = File(context!!.cacheDir, "자동차매매계약서_$contractId.pdf")

        body.byteStream().use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return file
    }

    suspend fun openRenderer(pdfFile: File): PdfRenderer = withContext(Dispatchers.IO) {
        val pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
        PdfRenderer(pfd)
    }

    suspend fun renderPage(renderer: PdfRenderer, index: Int, width: Int): Bitmap =
        withContext(Dispatchers.IO) {
            renderer.openPage(index).use { page ->
                val ratio = page.height.toFloat() / page.width.toFloat()
                val bmp = Bitmap.createBitmap(width, (width * ratio).toInt(), Bitmap.Config.ARGB_8888)
                page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bmp
            }
        }

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
            val response = api.checkCarNumber(carNumber) // response.data is CarNumberCheckDto?
            if (response.success && response.data != null) {
                Result.success(response.data.exists)
            } else {
                val errorMessage = response.message ?: "차량 번호 확인 실패 (data is null: ${response.data == null})"
                Log.e("VehicleRepository", "Check car number failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("VehicleRepository", "Check car number exception", e)
            Result.failure(e)
        }
    }

    suspend fun toggleLike(vehicleId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = api.toggleFavorite(vehicleId) // response.data is Boolean?
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                val errorMessage = response.message ?: "찜하기 처리 실패 (data is null: ${response.data == null})"
                Log.e("VehicleRepository", "Toggle like failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("VehicleRepository", "Toggle like exception", e)
            Result.failure(e)
        }
    }

    suspend fun getMyVehicles(page: Int = 0, size: Int = 10): Result<MyVehiclesResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getMyVehicles(page, size) // response.data is MyVehiclesResponse?
                if (response.success && response.data != null) {
                    Result.success(response.data)
                } else {
                    val errorMessage = response.message ?: "내 차량 정보 가져오기 실패 (data is null: ${response.data == null})"
                    Log.e("VehicleRepository", "Get my vehicles failed: $errorMessage")
                    Result.failure(Exception(errorMessage))
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
            val response = api.listVehicles(page, size, isAuction = true) // response.data is VehicleListResponse?
            if (response.success && response.data != null) {
                val auctionCars = response.data.vehicles.map { it.toAuctionCar() }
                Result.success(auctionCars)
            } else {
                val errorMessage = response.message ?: "경매 차량 목록 가져오기 실패 (data is null: ${response.data == null})"
                Log.e("VehicleRepository", "Get auctions failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("VehicleRepository", "Get auctions exception", e)
            Result.failure(e)
        }
    }

    suspend fun getVehicles(
        page: Int = 0,
        size: Int = 10,
    ): Result<List<VehicleSummary>> = withContext(Dispatchers.IO) {
        try {
            val response = api.listVehicles(page, size, isAuction = false) // response.data is VehicleListResponse?
            if (response.success && response.data != null) {
                val vehicles = response.data.vehicles.map { it.toVehicleSummary() }
                Result.success(vehicles)
            } else {
                val errorMessage = response.message ?: "차량 목록 가져오기 실패 (data is null: ${response.data == null})"
                Log.e("VehicleRepository", "Get vehicles failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("VehicleRepository", "Error fetching vehicles", e)
            Result.failure(e)
        }
    }

    suspend fun getVehicleDetail(id: String): Result<VehicleDetail> = withContext(Dispatchers.IO) {
        try {
            val response = api.getVehicleDetail(id) // response.data is VehicleDetailDto?
            if (response.success && response.data != null) {
                val vehicleDetail = response.data.toVehicleDetail()
                Result.success(vehicleDetail)
            } else {
                val errorMessage = response.message ?: "차량 상세 정보 가져오기 실패 (data is null: ${response.data == null})"
                Log.e("VehicleRepository", "Get vehicle detail failed: $errorMessage")
                Result.failure(Exception(errorMessage))
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

            val response = api.registerVehicle(requestBody, photoParts) // response.data could be String? or other type

            if (response.success) {
                // Assuming response.data (if not null) might contain an ID or a specific success detail.
                // If response.data is always Unit or not relevant on success, this can be simplified.
                Result.success("차량 등록 성공: ${response.data ?: "성공 (추가 정보 없음)"}")
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
                    val errorMsgBuilder = StringBuilder()
                    if (!domesticResponse.success || domesticResponse.data == null) {
                        errorMsgBuilder.append("국산 제조사 목록 실패: ${domesticResponse.message ?: "데이터 없음"}. ")
                    }
                    if (!importedResponse.success || importedResponse.data == null) {
                        errorMsgBuilder.append("수입 제조사 목록 실패: ${importedResponse.message ?: "데이터 없음"}")
                    }
                    Result.failure(Exception(errorMsgBuilder.toString().ifEmpty { "제조사 목록을 불러오는 데 실패했습니다." }))
                }
            }
        } catch (e: Exception) {
            Log.e("VehicleRepository", "Error fetching manufacturers for selection", e)
            Result.failure(e)
        }
    }

    suspend fun getCarNameList(category: String, manufacturer: String): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getCarNames(category, manufacturer) // response.data is List<String>?
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                val errorMessage = response.message ?: "Failed to load car names for $manufacturer (data is null: ${response.data == null})"
                Log.e("VehicleRepository", "Get car names failed for $manufacturer: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("VehicleRepository", "Error fetching car names for $manufacturer", e)
            Result.failure(e)
        }
    }

    suspend fun getModelNameList(category: String, manufacturer: String, carName: String): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getModelNames(category, manufacturer, carName) // response.data is List<String>?
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                val errorMessage = response.message ?: "Failed to load model names for $carName (data is null: ${response.data == null})"
                Log.e("VehicleRepository", "Get model names failed for $carName: $errorMessage")
                Result.failure(Exception(errorMessage))
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
            val response = api.getYears(category, manufacturer, carName, modelName) // response.data is List<Int>?
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                val errorMessage = response.message ?: "Failed to load years for $modelName (data is null: ${response.data == null})"
                Log.e("VehicleRepository", "Get years failed for $modelName: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("VehicleRepository", "Error fetching years for $modelName", e)
            Result.failure(e)
        }
    }

    suspend fun selectBuyer(vehicleId: String, buyerId: Long): Result<SelectBuyerResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.selectBuyer(vehicleId, buyerId) // response.data is SelectBuyerResponse?
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                val errorMessage = response.message ?: "구매자 선택 실패 (data is null: ${response.data == null})"
                Log.e("VehicleRepository", "Select buyer failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("VehicleRepository", "Select buyer exception", e)
            Result.failure(e)
        }
    }

    suspend fun getBuyRequests(vehicleId: String): Result<List<BuyApplyData>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getBuyRequests(vehicleId) // response.data is List<BuyApplyData>?
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                val errorMessage = response.message ?: "구매 신청 목록 가져오기 실패 (data is null: ${response.data == null})"
                Log.e("VehicleRepository", "Get buy requests failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("VehicleRepository", "Get buy requests exception", e)
            Result.failure(e)
        }
    }

    // 이 함수는 ApiResponse를 직접 반환하므로, 호출하는 쪽에서 data의 null 가능성을 처리해야 합니다.
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
