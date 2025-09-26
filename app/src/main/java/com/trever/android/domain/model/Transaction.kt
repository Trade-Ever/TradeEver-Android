package com.trever.android.domain.model

data class Transaction(
    val transactionId: Long,
    val vehicleId: Long,
    val vehicleName: String,
    val buyerName: String?,
    val sellerName: String?,
    val finalPrice: Long,
    val status: String,
    val createdAt: String,
    val contractPdfUrl: String?,
    val contractId: Long
)