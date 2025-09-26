package com.trever.android.ui.utils

import java.text.NumberFormat
import java.util.Locale

fun formatMileage(mileageKm: Int): String {
    return if (mileageKm >= 10000) {
        val man = mileageKm / 10000
        val remainder = (mileageKm % 10000) / 1000
        if (remainder > 0) {
            "$man.${remainder}만km"
        } else {
            "${man}만km"
        }
    } else {
        "${String.format("%,d", mileageKm)}km"
    }
}

fun formatKoreanWon(amount: Long): String {
    val 억 = amount / 100_000_000
    val 만 = (amount % 100_000_000) / 10_000

    return buildString {
        if (억 > 0) append("${NumberFormat.getNumberInstance(Locale.KOREA).format(억)}억 ")
        if (만 > 0) append("${NumberFormat.getNumberInstance(Locale.KOREA).format(만)}만원")
        if (억 == 0L && 만 == 0L) append("0원")
    }.trim()
}