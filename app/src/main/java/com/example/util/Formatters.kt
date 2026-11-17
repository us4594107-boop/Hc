package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
    private val indonesiaLocale = Locale("id", "ID")
    private val currencyFormat = NumberFormat.getCurrencyInstance(indonesiaLocale).apply {
        maximumFractionDigits = 0
    }

    fun formatIdr(amount: Long): String {
        return currencyFormat.format(amount).replace("Rp", "Rp ")
    }

    fun formatClockTime(millis: Long): String {
        val sdf = SimpleDateFormat("HH:mm:ss", indonesiaLocale)
        return sdf.format(Date(millis))
    }

    fun formatHourMinute(millis: Long?): String {
        if (millis == null) return "--:--"
        val sdf = SimpleDateFormat("HH:mm", indonesiaLocale)
        return sdf.format(Date(millis))
    }

    fun formatFullDate(millis: Long): String {
        val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", indonesiaLocale)
        return sdf.format(Date(millis))
    }

    fun formatDateKey(millis: Long = System.currentTimeMillis()): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date(millis))
    }

    fun formatShortDateFromKey(dateKey: String): String {
        return try {
            val input = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val output = SimpleDateFormat("EEE, dd MMM yyyy", indonesiaLocale)
            val date = input.parse(dateKey)
            if (date != null) output.format(date) else dateKey
        } catch (_: Exception) {
            dateKey
        }
    }

    fun formatWorkDuration(clockInMillis: Long?, clockOutMillis: Long?, breakStart: Long?, breakEnd: Long?): String {
        if (clockInMillis == null) return "0j 00m"
        val end = clockOutMillis ?: System.currentTimeMillis()
        var total = (end - clockInMillis).coerceAtLeast(0L)
        if (breakStart != null) {
            val bEnd = breakEnd ?: end
            val breakDuration = (bEnd - breakStart).coerceAtLeast(0L)
            total = (total - breakDuration).coerceAtLeast(0L)
        }
        val hours = total / (1000 * 60 * 60)
        val minutes = (total / (1000 * 60)) % 60
        return "${hours}j ${minutes.toString().padStart(2, '0')}m"
    }
}
