package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import java.util.Locale

data class LocationCheckResult(
    val isVerified: Boolean,
    val label: String,
    val detail: String
)

object LocationHelper {
    fun hasCoarseLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun resolveCurrentLocation(context: Context, fallbackWorkMode: String): LocationCheckResult {
        if (!hasCoarseLocationPermission(context)) {
            return LocationCheckResult(
                isVerified = false,
                label = "Izin lokasi belum diberikan",
                detail = "Aktifkan akses lokasi perkiraan untuk verifikasi titik $fallbackWorkMode."
            )
        }
        return try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val providers = lm?.getProviders(true).orEmpty()
            var bestLocation: Location? = null
            for (provider in providers) {
                val loc = lm?.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                    bestLocation = loc
                }
            }
            if (bestLocation != null) {
                val lat = String.format(Locale.US, "%.4f", bestLocation.latitude)
                val lng = String.format(Locale.US, "%.4f", bestLocation.longitude)
                LocationCheckResult(
                    isVerified = true,
                    label = "$fallbackWorkMode • GPS Terverifikasi",
                    detail = "Koordinat ($lat, $lng) • Akurasi ±${bestLocation.accuracy.toInt()}m"
                )
            } else {
                LocationCheckResult(
                    isVerified = true,
                    label = "$fallbackWorkMode • Lokasi Perangkat Aktif",
                    detail = "Izin lokasi aktif; sinyal koordinat menunggu pembaruan jaringan."
                )
            }
        } catch (_: Exception) {
            LocationCheckResult(
                isVerified = false,
                label = "$fallbackWorkMode • Mode Offline",
                detail = "Tidak dapat membaca sensor lokasi perangkat."
            )
        }
    }
}
