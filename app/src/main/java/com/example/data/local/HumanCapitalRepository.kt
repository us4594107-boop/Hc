package com.example.data.local

import com.example.util.Formatters
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class HumanCapitalRepository(private val dao: AlfaoneDao) {

    val employee: Flow<EmployeeEntity?> = dao.observeEmployee()
    val attendanceLogs: Flow<List<AttendanceLogEntity>> = dao.observeAttendanceLogs()
    val leaveRequests: Flow<List<LeaveRequestEntity>> = dao.observeLeaveRequests()
    val payslips: Flow<List<PayslipEntity>> = dao.observePayslips()
    val trainingModules: Flow<List<TrainingModuleEntity>> = dao.observeTrainingModules()
    val announcements: Flow<List<AnnouncementEntity>> = dao.observeAnnouncements()

    suspend fun ensureSeeded() {
        if (dao.getEmployeeCount() > 0) return

        dao.insertEmployee(
            EmployeeEntity(
                nik = "21040892",
                fullName = "Budi Santoso",
                pin = "123456",
                roleTitle = "Assistant Chief of Store",
                department = "Retail Store Operations",
                storeBranch = "Toko 1042 - Kemang Selatan",
                joinDate = "14 Maret 2021",
                employmentStatus = "PKWTT (Karyawan Tetap)",
                annualLeaveQuota = 12,
                annualLeaveUsed = 3,
                bankName = "Bank BCA",
                maskedAccount = "•••• •••• 4829",
                isSignedIn = false
            )
        )

        val cal = Calendar.getInstance()
        val historyLogs = mutableListOf<AttendanceLogEntity>()
        for (daysAgo in 1..8) {
            val dayCal = (cal.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, -daysAgo)
            }
            val dayOfWeek = dayCal.get(Calendar.DAY_OF_WEEK)
            if (dayOfWeek == Calendar.SUNDAY) continue

            val key = Formatters.formatDateKey(dayCal.timeInMillis)
            val isAfternoon = daysAgo % 3 == 0
            val shiftName = if (isAfternoon) "Shift Siang (S2)" else "Shift Pagi (P1)"
            val shiftWindow = if (isAfternoon) "14:00 - 22:00" else "07:00 - 15:00"
            val inHour = if (isAfternoon) 13 else 6
            val inMin = if (daysAgo == 2) 12 else 54
            val lateInHour = if (daysAgo == 2) 7 else inHour

            val inCal = (dayCal.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, lateInHour)
                set(Calendar.MINUTE, inMin)
                set(Calendar.SECOND, 0)
            }
            val breakStartCal = (dayCal.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, if (isAfternoon) 18 else 12)
                set(Calendar.MINUTE, 0)
            }
            val breakEndCal = (dayCal.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, if (isAfternoon) 18 else 12)
                set(Calendar.MINUTE, 45)
            }
            val outCal = (dayCal.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, if (isAfternoon) 22 else 15)
                set(Calendar.MINUTE, if (daysAgo == 4) 35 else 6)
            }

            historyLogs.add(
                AttendanceLogEntity(
                    dateKey = key,
                    shiftName = shiftName,
                    shiftWindow = shiftWindow,
                    workMode = "Toko / Cabang",
                    clockInMillis = inCal.timeInMillis,
                    breakStartMillis = breakStartCal.timeInMillis,
                    breakEndMillis = breakEndCal.timeInMillis,
                    clockOutMillis = outCal.timeInMillis,
                    locationLabel = "Toko 1042 Kemang Selatan",
                    status = if (daysAgo == 2) "TERLAMBAT" else "TEPAT_WAKTU",
                    note = if (daysAgo == 2) "Kendala lalu lintas pagi" else ""
                )
            )
        }
        dao.insertAttendances(historyLogs)

        dao.insertLeaveRequests(
            listOf(
                LeaveRequestEntity(
                    category = "CUTI",
                    title = "Cuti Tahunan Keluarga",
                    startDateLabel = "24 Nov 2026",
                    endDateLabel = "25 Nov 2026",
                    durationLabel = "2 Hari",
                    reason = "Acara keluarga di Bandung bersama orang tua.",
                    status = "MENUNGGU",
                    approverName = "Rina Kusuma (Area Coordinator)",
                    createdAtMillis = System.currentTimeMillis() - 86_400_000L
                ),
                LeaveRequestEntity(
                    category = "LEMBUR",
                    title = "Stock Opname Bulanan Toko",
                    startDateLabel = "10 Nov 2026",
                    endDateLabel = "10 Nov 2026",
                    durationLabel = "3 Jam",
                    reason = "Pendampingan SO gudang dan rak display promo akhir bulan.",
                    status = "DISETUJUI",
                    approverName = "Rina Kusuma (Area Coordinator)",
                    createdAtMillis = System.currentTimeMillis() - 6 * 86_400_000L
                ),
                LeaveRequestEntity(
                    category = "SAKIT",
                    title = "Izin Sakit Demam",
                    startDateLabel = "29 Okt 2026",
                    endDateLabel = "29 Okt 2026",
                    durationLabel = "1 Hari",
                    reason = "Demam tinggi disertai surat keterangan dokter klinik.",
                    status = "DISETUJUI",
                    approverName = "Rina Kusuma (Area Coordinator)",
                    createdAtMillis = System.currentTimeMillis() - 18 * 86_400_000L
                )
            )
        )

        dao.insertPayslips(
            listOf(
                PayslipEntity(
                    periodLabel = "November 2026",
                    periodOrder = 202611,
                    payDateLabel = "28 Nov 2026",
                    baseSalary = 5450000L,
                    allowance = 950000L,
                    incentive = 625000L,
                    overtimePay = 340000L,
                    bpjsHealth = 54500L,
                    bpjsEmployment = 109000L,
                    taxPph21 = 145000L,
                    cooperative = 150000L,
                    workDays = 22,
                    overtimeHours = 6
                ),
                PayslipEntity(
                    periodLabel = "Oktober 2026",
                    periodOrder = 202610,
                    payDateLabel = "28 Okt 2026",
                    baseSalary = 5450000L,
                    allowance = 950000L,
                    incentive = 580000L,
                    overtimePay = 210000L,
                    bpjsHealth = 54500L,
                    bpjsEmployment = 109000L,
                    taxPph21 = 132000L,
                    cooperative = 150000L,
                    workDays = 23,
                    overtimeHours = 4
                ),
                PayslipEntity(
                    periodLabel = "September 2026",
                    periodOrder = 202609,
                    payDateLabel = "28 Sep 2026",
                    baseSalary = 5450000L,
                    allowance = 900000L,
                    incentive = 710000L,
                    overtimePay = 165000L,
                    bpjsHealth = 54500L,
                    bpjsEmployment = 109000L,
                    taxPph21 = 138000L,
                    cooperative = 150000L,
                    workDays = 22,
                    overtimeHours = 3
                )
            )
        )

        dao.insertTrainingModules(
            listOf(
                TrainingModuleEntity(
                    id = "MOD-01",
                    title = "Standar Layanan Pelanggan Retail 5S",
                    category = "Operasional Toko",
                    durationMinutes = 20,
                    progressPercent = 100,
                    isCompleted = true,
                    description = "Panduan senyum, salam, sapa, serta penanganan antrean kasir saat jam sibuk promo."
                ),
                TrainingModuleEntity(
                    id = "MOD-02",
                    title = "Keselamatan Kerja & Gudang (K3)",
                    category = "Wajib Tahunan",
                    durationMinutes = 25,
                    progressPercent = 60,
                    isCompleted = false,
                    description = "Prosedur penanganan barang berat, jalur evakuasi, dan penggunaan APAR di area toko."
                ),
                TrainingModuleEntity(
                    id = "MOD-03",
                    title = "Manajemen Stok & FEFO Produk Segar",
                    category = "Inventory",
                    durationMinutes = 30,
                    progressPercent = 25,
                    isCompleted = false,
                    description = "Teknik rotasi First Expired First Out dan audit harian rak chiller."
                ),
                TrainingModuleEntity(
                    id = "MOD-04",
                    title = "Etika Digital & Keamanan Data Karyawan",
                    category = "Kepatuhan HC",
                    durationMinutes = 15,
                    progressPercent = 0,
                    isCompleted = false,
                    description = "Menjaga kerahasiaan PIN HC, akun kasir, dan informasi internal perusahaan."
                )
            )
        )

        dao.insertAnnouncements(
            listOf(
                AnnouncementEntity(
                    id = "ANN-03",
                    title = "Jadwal Cuti Bersama & Libur Akhir Tahun",
                    category = "Kebijakan HC",
                    publishedLabel = "Hari Ini • 08:30",
                    summary = "Pengajuan cuti periode Desember dapat diajukan maksimal H-7 melalui Alfaone HC.",
                    body = "Kepada seluruh rekan karyawan toko dan kantor cabang, pengajuan cuti tahunan untuk periode libur akhir tahun sudah dibuka. Pastikan koordinasi jadwal shift bersama Kepala Toko atau Area Coordinator agar operasional tetap optimal.",
                    isRead = false
                ),
                AnnouncementEntity(
                    id = "ANN-02",
                    title = "Program Apresiasi Kehadiran 100% Q4",
                    category = "Reward & Benefit",
                    publishedLabel = "2 Hari Lalu",
                    summary = "Tambahan poin insentif bagi karyawan dengan kehadiran tepat waktu penuh selama Q4.",
                    body = "Karyawan dengan catatan presensi tanpa keterlambatan dan tanpa alpa sepanjang kuartal IV berhak memperoleh bonus poin kinerja yang dicairkan bersama insentif akhir kuartal.",
                    isRead = false
                ),
                AnnouncementEntity(
                    id = "ANN-01",
                    title = "Pembaruan Modul K3 & Layanan 5S Wajib",
                    category = "E-Learning",
                    publishedLabel = "5 Hari Lalu",
                    summary = "Selesaikan modul K3 sebelum tanggal 30 bulan ini di menu Pengembangan Diri.",
                    body = "Seluruh staf operasional toko diharapkan menuntaskan modul Keselamatan Kerja (K3) pada menu E-Learning untuk pemenuhan sertifikasi kompetensi tahunan.",
                    isRead = true
                )
            )
        )
    }

    suspend fun signIn(nik: String, pin: String): Result<EmployeeEntity> {
        ensureSeeded()
        val emp = dao.getEmployeeByNik(nik.trim())
        return if (emp != null && emp.pin == pin.trim()) {
            dao.setSignedIn(emp.nik, true)
            Result.success(emp.copy(isSignedIn = true))
        } else {
            Result.failure(IllegalArgumentException("NIK atau PIN HC tidak sesuai."))
        }
    }

    suspend fun signOut(nik: String) {
        dao.setSignedIn(nik, false)
    }

    suspend fun changePin(nik: String, oldPin: String, newPin: String): Result<Unit> {
        val emp = dao.getEmployeeByNik(nik) ?: return Result.failure(IllegalStateException("Profil tidak ditemukan"))
        if (emp.pin != oldPin) {
            return Result.failure(IllegalArgumentException("PIN HC lama tidak sesuai."))
        }
        if (newPin.length != 6 || !newPin.all { it.isDigit() }) {
            return Result.failure(IllegalArgumentException("PIN HC baru harus terdiri dari 6 angka."))
        }
        dao.updatePin(nik, newPin)
        return Result.success(Unit)
    }

    suspend fun clockIn(workMode: String, locationLabel: String) {
        val todayKey = Formatters.formatDateKey()
        val now = System.currentTimeMillis()
        val existing = dao.getAttendanceByDate(todayKey)
        if (existing != null && existing.clockInMillis != null) return

        val cal = Calendar.getInstance().apply { timeInMillis = now }
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val isLate = hour > 7 || (hour == 7 && minute > 5)

        val log = AttendanceLogEntity(
            dateKey = todayKey,
            shiftName = "Shift Pagi (P1)",
            shiftWindow = "07:00 - 15:00",
            workMode = workMode,
            clockInMillis = now,
            locationLabel = locationLabel,
            status = if (isLate) "TERLAMBAT" else "TEPAT_WAKTU",
            note = if (isLate) "Check-in lewat pukul 07:05" else "Tercatat otomatis di aplikasi"
        )
        dao.insertAttendance(log)
    }

    suspend fun startBreak() {
        val todayKey = Formatters.formatDateKey()
        val existing = dao.getAttendanceByDate(todayKey) ?: return
        if (existing.breakStartMillis == null) {
            dao.updateAttendance(existing.copy(breakStartMillis = System.currentTimeMillis()))
        }
    }

    suspend fun endBreak() {
        val todayKey = Formatters.formatDateKey()
        val existing = dao.getAttendanceByDate(todayKey) ?: return
        if (existing.breakStartMillis != null && existing.breakEndMillis == null) {
            dao.updateAttendance(existing.copy(breakEndMillis = System.currentTimeMillis()))
        }
    }

    suspend fun clockOut(locationLabel: String) {
        val todayKey = Formatters.formatDateKey()
        val existing = dao.getAttendanceByDate(todayKey) ?: return
        if (existing.clockOutMillis == null) {
            val updatedStatus = if (existing.status == "TERLAMBAT") "TERLAMBAT" else "SELESAI"
            dao.updateAttendance(
                existing.copy(
                    clockOutMillis = System.currentTimeMillis(),
                    locationLabel = locationLabel,
                    status = updatedStatus
                )
            )
        }
    }

    suspend fun submitAttendanceCorrection(
        dateKey: String,
        workMode: String,
        note: String
    ) {
        val existing = dao.getAttendanceByDate(dateKey)
        val now = System.currentTimeMillis()
        if (existing != null) {
            dao.updateAttendance(
                existing.copy(
                    workMode = workMode,
                    status = "KOREKSI",
                    note = note
                )
            )
        } else {
            dao.insertAttendance(
                AttendanceLogEntity(
                    dateKey = dateKey,
                    shiftName = "Shift Pagi (P1)",
                    shiftWindow = "07:00 - 15:00",
                    workMode = workMode,
                    clockInMillis = now - 8 * 3600_000L,
                    clockOutMillis = now,
                    locationLabel = "$workMode (Koreksi)",
                    status = "KOREKSI",
                    note = note
                )
            )
        }
    }

    suspend fun submitLeaveRequest(
        nik: String,
        category: String,
        title: String,
        startDateLabel: String,
        endDateLabel: String,
        durationLabel: String,
        reason: String,
        attachmentUri: String?
    ) {
        dao.insertLeaveRequest(
            LeaveRequestEntity(
                category = category,
                title = title,
                startDateLabel = startDateLabel,
                endDateLabel = endDateLabel,
                durationLabel = durationLabel,
                reason = reason,
                attachmentUri = attachmentUri,
                status = "MENUNGGU",
                approverName = "Rina Kusuma (Area Coordinator)",
                createdAtMillis = System.currentTimeMillis()
            )
        )
        if (category == "CUTI") {
            dao.incrementLeaveUsed(nik, 1)
        }
    }

    suspend fun cancelLeaveRequest(id: Int) {
        dao.deleteLeaveRequestById(id)
    }

    suspend fun completeTrainingModule(moduleId: String) {
        dao.updateTrainingProgress(moduleId, 100, true)
    }

    suspend fun markAnnouncementRead(id: String) {
        dao.markAnnouncementRead(id)
    }
}
