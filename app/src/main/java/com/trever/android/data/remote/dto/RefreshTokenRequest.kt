package com.trever.android.data.remote.dto // 또는 dto 패키지 바로 아래

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)