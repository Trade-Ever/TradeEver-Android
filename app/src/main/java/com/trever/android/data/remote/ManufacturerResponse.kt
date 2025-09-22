package com.trever.android.data.remote


import kotlinx.serialization.Serializable

// 최상위 응답 구조 (ApiResponse는 이미 VehicleApi.kt에 정의된 것을 재사용)

@Serializable
data class ManufacturerData(
    val category: String, // "국산", "수입"
    val manufacturers: List<ManufacturerDetail>
)

@Serializable
data class ManufacturerDetail(
    val manufacturer: String, // "기아", "BMW" 등
    val count: Int
)

@Serializable
data class CarNameDetail(
    val carName: String,
    val count: Int
)