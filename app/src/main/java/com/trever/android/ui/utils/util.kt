package com.trever.android.ui.utils

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