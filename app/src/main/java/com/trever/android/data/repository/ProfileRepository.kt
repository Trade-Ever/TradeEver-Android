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
            val response = api.getProfile()
            if (response.success) Result.success(response.data)
            else Result.failure(Exception(response.message))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ 프로필 수정 (UserInfo 보냄)
    suspend fun updateProfile(userInfo: UserInfo, imageUri: Uri?): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val json = gson.toJson(userInfo)
                val userInfoBody = json.toRequestBody("text/plain".toMediaTypeOrNull())

                val imagePart = if (imageUri != null && context != null) {
                    val fileName = "profile_${System.currentTimeMillis()}.jpg"
                    val file = uriToFile(context, imageUri, fileName)
                    val reqFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("profileImage", file.name, reqFile)
                } else null

                val response = api.updateProfile(userInfoBody, imagePart)

                if (response.success) Result.success("프로필 수정 성공")
                else Result.failure(Exception(response.message ?: "프로필 수정 실패"))
            } catch (e: Exception) {
                Log.e("ProfileRepository", "Error updating profile", e)
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
