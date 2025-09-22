package com.trever.android.data.remote

import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.Part

interface ProfileApi {

    // 내 프로필 조회 (원래 정보)
    @GET("api/v1/users/me")
    suspend fun getProfile(): ApiResponse<UserProfile>

    // 프로필 수정
    @Multipart
    @PATCH("api/v1/users/profile")
    suspend fun updateProfile(
        @Part("userInfo") userInfo: RequestBody,
        @Part profileImage: MultipartBody.Part? = null
    ): ApiBaseResponse<Unit>
    }

// 프로필 수정 요청 DTO
@Serializable
data class UserInfo(
    val name: String? = null,
    val phone: String? = null,
    val locationCity: String? = null,
    val birthDate: String? = null
)

// 서버에서 내려줄 프로필 응답 DTO
@Serializable
data class UserProfile(
    val userId: Long? = null,
    val email: String? = null,
    val name: String? = null,
    val phone: String? = null,
    val locationCity: String? = null,
    val birthDate: String? = null,
    val profileImageUrl: String? = null,
    val balance: Long? = null
)