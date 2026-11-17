package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employees")
data class EmployeeEntity(
    @PrimaryKey val nik: String,
    val fullName: String,
    val pin: String,
    val roleTitle: String,
    val department: String,
    val storeBranch: String,
    val joinDate: String,
    val employmentStatus: String,
    val annualLeaveQuota: Int,
    val annualLeaveUsed: Int,
    val bankName: String,
    val maskedAccount: String,
    val isSignedIn: Boolean = false
)

@Entity(tableName = "attendance_logs")
data class AttendanceLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateKey: String,
    val shiftName: String,
    val shiftWindow: String,
    val workMode: String,
    val clockInMillis: Long? = null,
    val breakStartMillis: Long? = null,
    val breakEndMillis: Long? = null,
    val clockOutMillis: Long? = null,
    val locationLabel: String,
    val status: String, // TEPAT_WAKTU, TERLAMBAT, SELESAI, KOREKSI
    val note: String = ""
)

@Entity(tableName = "leave_requests")
data class LeaveRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String, // CUTI, SAKIT, IZIN, LEMBUR
    val title: String,
    val startDateLabel: String,
    val endDateLabel: String,
    val durationLabel: String,
    val reason: String,
    val attachmentUri: String? = null,
    val status: String, // MENUNGGU, DISETUJUI, DITOLAK
    val approverName: String,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "payslips")
data class PayslipEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val periodLabel: String,
    val periodOrder: Int,
    val payDateLabel: String,
    val baseSalary: Long,
    val allowance: Long,
    val incentive: Long,
    val overtimePay: Long,
    val bpjsHealth: Long,
    val bpjsEmployment: Long,
    val taxPph21: Long,
    val cooperative: Long,
    val workDays: Int,
    val overtimeHours: Int
) {
    val totalEarnings: Long get() = baseSalary + allowance + incentive + overtimePay
    val totalDeductions: Long get() = bpjsHealth + bpjsEmployment + taxPph21 + cooperative
    val takeHomePay: Long get() = totalEarnings - totalDeductions
}

@Entity(tableName = "training_modules")
data class TrainingModuleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val durationMinutes: Int,
    val progressPercent: Int,
    val isCompleted: Boolean,
    val description: String
)

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val publishedLabel: String,
    val summary: String,
    val body: String,
    val isRead: Boolean = false
)
