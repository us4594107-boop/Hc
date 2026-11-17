package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AlfaoneDao {
    // Employee
    @Query("SELECT * FROM employees LIMIT 1")
    fun observeEmployee(): Flow<EmployeeEntity?>

    @Query("SELECT * FROM employees WHERE nik = :nik LIMIT 1")
    suspend fun getEmployeeByNik(nik: String): EmployeeEntity?

    @Query("SELECT COUNT(*) FROM employees")
    suspend fun getEmployeeCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: EmployeeEntity)

    @Update
    suspend fun updateEmployee(employee: EmployeeEntity)

    @Query("UPDATE employees SET isSignedIn = :signedIn WHERE nik = :nik")
    suspend fun setSignedIn(nik: String, signedIn: Boolean)

    @Query("UPDATE employees SET pin = :newPin WHERE nik = :nik")
    suspend fun updatePin(nik: String, newPin: String)

    @Query("UPDATE employees SET annualLeaveUsed = annualLeaveUsed + :delta WHERE nik = :nik")
    suspend fun incrementLeaveUsed(nik: String, delta: Int)

    // Attendance
    @Query("SELECT * FROM attendance_logs ORDER BY dateKey DESC, id DESC")
    fun observeAttendanceLogs(): Flow<List<AttendanceLogEntity>>

    @Query("SELECT * FROM attendance_logs WHERE dateKey = :dateKey LIMIT 1")
    suspend fun getAttendanceByDate(dateKey: String): AttendanceLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(log: AttendanceLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendances(logs: List<AttendanceLogEntity>)

    @Update
    suspend fun updateAttendance(log: AttendanceLogEntity)

    // Leave & Overtime Requests
    @Query("SELECT * FROM leave_requests ORDER BY createdAtMillis DESC, id DESC")
    fun observeLeaveRequests(): Flow<List<LeaveRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaveRequest(request: LeaveRequestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaveRequests(requests: List<LeaveRequestEntity>)

    @Query("DELETE FROM leave_requests WHERE id = :id")
    suspend fun deleteLeaveRequestById(id: Int)

    // Payslips
    @Query("SELECT * FROM payslips ORDER BY periodOrder DESC")
    fun observePayslips(): Flow<List<PayslipEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayslips(payslips: List<PayslipEntity>)

    // Training
    @Query("SELECT * FROM training_modules ORDER BY isCompleted ASC, id ASC")
    fun observeTrainingModules(): Flow<List<TrainingModuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrainingModules(modules: List<TrainingModuleEntity>)

    @Query("UPDATE training_modules SET progressPercent = :progress, isCompleted = :completed WHERE id = :id")
    suspend fun updateTrainingProgress(id: String, progress: Int, completed: Boolean)

    // Announcements
    @Query("SELECT * FROM announcements ORDER BY isRead ASC, id DESC")
    fun observeAnnouncements(): Flow<List<AnnouncementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncements(announcements: List<AnnouncementEntity>)

    @Query("UPDATE announcements SET isRead = 1 WHERE id = :id")
    suspend fun markAnnouncementRead(id: String)
}
