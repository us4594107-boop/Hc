package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class UpcomingShift(
    val id: String,
    val dayLabel: String,
    val dateLabel: String,
    val shiftCode: String,
    val shiftTitle: String,
    val timeWindow: String,
    val location: String,
    val isToday: Boolean,
    val isDayOff: Boolean
)

object ShiftScheduleProvider {
    private val idLocale = Locale("id", "ID")

    fun getUpcomingShifts(storeBranch: String, daysCount: Int = 7): List<UpcomingShift> {
        val dayFmt = SimpleDateFormat("EEE", idLocale)
        val dateFmt = SimpleDateFormat("dd MMM", idLocale)
        val baseCal = Calendar.getInstance()

        return (0 until daysCount).map { offset ->
            val cal = (baseCal.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, offset)
            }
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            val isOff = dayOfWeek == Calendar.SUNDAY
            val isAfternoon = dayOfWeek == Calendar.WEDNESDAY || dayOfWeek == Calendar.SATURDAY

            val (code, title, window) = when {
                isOff -> Triple("OFF", "Libur Mingguan", "Jadwal Libur")
                isAfternoon -> Triple("S2", "Shift Siang", "14:00 - 22:00")
                else -> Triple("P1", "Shift Pagi", "07:00 - 15:00")
            }

            val dayLabel = when (offset) {
                0 -> "Hari Ini"
                1 -> "Besok"
                else -> dayFmt.format(cal.time)
            }

            UpcomingShift(
                id = "shift_$offset",
                dayLabel = dayLabel,
                dateLabel = dateFmt.format(cal.time),
                shiftCode = code,
                shiftTitle = title,
                timeWindow = window,
                location = if (isOff) "Tidak bertugas" else storeBranch,
                isToday = offset == 0,
                isDayOff = isOff
            )
        }
    }
}
