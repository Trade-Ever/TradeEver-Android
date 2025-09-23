package com.trever.android.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

interface ProfileApi {

    @POST("api/v1/users/me/complete")
    suspend fun completeProfile(@Body request: ProfileCompleteRequest): ProfileCompleteResponse
}

@Serializable
data class ProfileCompleteRequest(
    val name: String,
    val phone: String,
    val locationCity: String,
    val birthDate: String
)

@Serializable
data class ProfileCompleteResponse(
    val status: Int,
    val success: Boolean,
    val message: String
)