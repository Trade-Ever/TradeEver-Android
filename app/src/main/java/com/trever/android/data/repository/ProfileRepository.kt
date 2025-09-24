package com.trever.android.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import com.trever.android.data.network.ApiClient
import com.trever.android.data.remote.ProfileApi
import com.trever.android.data.remote.UserInfo
import com.trever.android.data.remote.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class ProfileRepository(
    private val api: ProfileApi = ApiClient.profileApi,
    private val context: Context? = null,
    private val gson: Gson = Gson()
) {

    suspend fun getProfile(): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val response = api.getProfile() // response.data는 UserProfile?
            if (response.success && response.data != null) {
                Result.success(response.data) // response.data가 UserProfile로 스마트 캐스트됨
            } else {
                // API 응답이 성공하지 않았거나 data가 null인 경우
                val errorMessage = response.message ?: "프로필 정보를 가져오지 못했습니다 (data is null: ${response.data == null})"
                Log.e("ProfileRepository", "Get profile failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Get profile exception", e)
            Result.failure(e)
        }
    }

    suspend fun updateProfile(userInfo: UserInfo, imageUri: Uri?): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                Log.d("ProfileRepository", "프로필 업데이트 요청: $userInfo, 이미지 URI: $imageUri")

                val json = gson.toJson(userInfo)
                val userInfoBody = json.toRequestBody("application/json".toMediaTypeOrNull())

                val imagePart = if (imageUri != null && context != null) {
                    val fileName = "profile_${System.currentTimeMillis()}.jpg"
                    val file = uriToFile(context, imageUri, fileName)
                    val reqFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("profileImage", file.name, reqFile)
                } else null

                // api.updateProfile()이 ApiResponse<T>를 반환하고, T가 Unit이거나 무시되는 타입이라고 가정합니다.
                // 만약 ApiResponse.data를 사용해야 한다면, 해당 부분도 nullable 처리가 필요합니다.
                val response = api.updateProfile(userInfoBody, imagePart)

                Log.d("ProfileRepository", "프로필 업데이트 응답: $response")

                if (response.success) {
                    // 성공 메시지를 response.data에서 가져오지 않는다면 이 부분은 괜찮습니다.
                    Result.success("프로필 수정 성공") 
                } else {
                    Log.e("ProfileRepository", "프로필 업데이트 API 실패: ${response.message}")
                    Result.failure(Exception(response.message ?: "프로필 수정 실패"))
                }
            } catch (e: Exception) {
                Log.e("ProfileRepository", "프로필 업데이트 중 예외 발생", e)
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
}
