package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AlfaoneDatabase
import com.example.data.local.AnnouncementEntity
import com.example.data.local.AttendanceLogEntity
import com.example.data.local.EmployeeEntity
import com.example.data.local.HumanCapitalRepository
import com.example.data.local.LeaveRequestEntity
import com.example.data.local.PayslipEntity
import com.example.data.local.TrainingModuleEntity
import com.example.util.Formatters
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AlfaoneUiState(
    val isLoading: Boolean = true,
    val employee: EmployeeEntity? = null,
    val currentTimeMillis: Long = System.currentTimeMillis(),
    val todayAttendance: AttendanceLogEntity? = null,
    val attendanceLogs: List<AttendanceLogEntity> = emptyList(),
    val leaveRequests: List<LeaveRequestEntity> = emptyList(),
    val payslips: List<PayslipEntity> = emptyList(),
    val trainingModules: List<TrainingModuleEntity> = emptyList(),
    val announcements: List<AnnouncementEntity> = emptyList(),
    val isSalaryRevealed: Boolean = false
)

class AlfaoneViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HumanCapitalRepository(
        AlfaoneDatabase.getInstance(application).alfaoneDao()
    )

    private val _isSalaryRevealed = MutableStateFlow(false)
    private val _snackbarMessages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val snackbarMessages = _snackbarMessages.asSharedFlow()

    private val tickerFlow = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(1000L)
        }
    }

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    val uiState: StateFlow<AlfaoneUiState> = combine(
        repository.employee,
        tickerFlow,
        repository.attendanceLogs,
        repository.leaveRequests,
        repository.payslips,
        repository.trainingModules,
        repository.announcements,
        _isSalaryRevealed
    ) { values ->
        val emp = values[0] as EmployeeEntity?
        val now = values[1] as Long
        @Suppress("UNCHECKED_CAST")
        val logs = values[2] as List<AttendanceLogEntity>
        @Suppress("UNCHECKED_CAST")
        val leaves = values[3] as List<LeaveRequestEntity>
        @Suppress("UNCHECKED_CAST")
        val slips = values[4] as List<PayslipEntity>
        @Suppress("UNCHECKED_CAST")
        val trainings = values[5] as List<TrainingModuleEntity>
        @Suppress("UNCHECKED_CAST")
        val news = values[6] as List<AnnouncementEntity>
        val salaryRevealed = values[7] as Boolean

        val todayKey = Formatters.formatDateKey(now)
        val todayLog = logs.firstOrNull { it.dateKey == todayKey }

        AlfaoneUiState(
            isLoading = false,
            employee = emp,
            currentTimeMillis = now,
            todayAttendance = todayLog,
            attendanceLogs = logs,
            leaveRequests = leaves,
            payslips = slips,
            trainingModules = trainings,
            announcements = news,
            isSalaryRevealed = salaryRevealed
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = AlfaoneUiState()
    )

    fun signIn(nik: String, pin: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.signIn(nik, pin)
            result.onFailure {
                onError(it.message ?: "Gagal masuk")
            }
        }
    }

    fun signOut() {
        val nik = uiState.value.employee?.nik ?: return
        viewModelScope.launch {
            _isSalaryRevealed.value = false
            repository.signOut(nik)
        }
    }

    fun clockIn(workMode: String, locationLabel: String) {
        viewModelScope.launch {
            repository.clockIn(workMode, locationLabel)
            _snackbarMessages.tryEmit("Clock In berhasil dicatat pada ${Formatters.formatHourMinute(System.currentTimeMillis())}")
        }
    }

    fun startBreak() {
        viewModelScope.launch {
            repository.startBreak()
            _snackbarMessages.tryEmit("Waktu istirahat dimulai")
        }
    }

    fun endBreak() {
        viewModelScope.launch {
            repository.endBreak()
            _snackbarMessages.tryEmit("Istirahat selesai, selamat melanjutkan kerja")
        }
    }

    fun clockOut(locationLabel: String) {
        viewModelScope.launch {
            repository.clockOut(locationLabel)
            _snackbarMessages.tryEmit("Clock Out berhasil dicatat. Terima kasih atas kerja keras Anda!")
        }
    }

    fun submitAttendanceCorrection(dateKey: String, workMode: String, note: String) {
        viewModelScope.launch {
            repository.submitAttendanceCorrection(dateKey, workMode, note)
            _snackbarMessages.tryEmit("Pengajuan koreksi presensi tersimpan")
        }
    }

    fun submitLeaveRequest(
        category: String,
        title: String,
        startDateLabel: String,
        endDateLabel: String,
        durationLabel: String,
        reason: String,
        attachmentUri: String?
    ) {
        val nik = uiState.value.employee?.nik ?: return
        viewModelScope.launch {
            repository.submitLeaveRequest(
                nik = nik,
                category = category,
                title = title,
                startDateLabel = startDateLabel,
                endDateLabel = endDateLabel,
                durationLabel = durationLabel,
                reason = reason,
                attachmentUri = attachmentUri
            )
            _snackbarMessages.tryEmit("Pengajuan $title berhasil dikirim")
        }
    }

    fun cancelLeaveRequest(id: Int) {
        viewModelScope.launch {
            repository.cancelLeaveRequest(id)
            _snackbarMessages.tryEmit("Pengajuan dibatalkan")
        }
    }

    fun toggleSalaryReveal(pinAttempt: String? = null, onPinInvalid: () -> Unit = {}) {
        if (_isSalaryRevealed.value) {
            _isSalaryRevealed.value = false
            return
        }
        val currentPin = uiState.value.employee?.pin
        if (pinAttempt != null) {
            if (pinAttempt == currentPin) {
                _isSalaryRevealed.value = true
                _snackbarMessages.tryEmit("Slip gaji terbuka untuk sesi ini")
            } else {
                onPinInvalid()
            }
        }
    }

    fun changePin(oldPin: String, newPin: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val nik = uiState.value.employee?.nik ?: return
        viewModelScope.launch {
            val result = repository.changePin(nik, oldPin, newPin)
            result.onSuccess {
                _snackbarMessages.tryEmit("PIN HC berhasil diperbarui")
                onSuccess()
            }.onFailure {
                onError(it.message ?: "Gagal mengubah PIN")
            }
        }
    }

    fun completeTrainingModule(moduleId: String) {
        viewModelScope.launch {
            repository.completeTrainingModule(moduleId)
            _snackbarMessages.tryEmit("Modul E-Learning selesai!")
        }
    }

    fun markAnnouncementRead(id: String) {
        viewModelScope.launch {
            repository.markAnnouncementRead(id)
        }
    }
}
