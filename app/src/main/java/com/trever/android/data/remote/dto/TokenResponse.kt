package com.trever.android.data.remote.dto // 또는 dto 패키지 바로 아래

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TokenResponse(
    // 백엔드에서 오는 JSON 키 이름과 변수명이 다를 경우 @SerialName 사용
    @SerialName("accessToken") // 예시: JSON 키가 "accessToken" 일 경우
    val accessToken: String,

    @SerialName("refreshToken") // 예시: JSON 키가 "refreshToken" 일 경우
    val refreshToken: String,

    // 필요하다면 다른 필드 추가 (예: 만료 시간 등)
    // val expiresIn: Long? = null
)