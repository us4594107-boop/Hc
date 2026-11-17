package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.AlfaoneUiState
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusPill
import com.example.ui.components.attendanceStatusStyle
import com.example.ui.theme.AlfaMonoTextStyles
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.util.Formatters

@Composable
fun AttendanceScreen(
    uiState: AlfaoneUiState,
    onSubmitCorrection: (dateKey: String, workMode: String, note: String) -> Unit
) {
    val filters = listOf("Semua", "Tepat Waktu", "Terlambat", "Koreksi")
    var selectedFilter by remember { mutableStateOf("Semua") }
    var showCorrectionDialog by remember { mutableStateOf(false) }

    val filteredLogs = remember(uiState.attendanceLogs, selectedFilter) {
        when (selectedFilter) {
            "Tepat Waktu" -> uiState.attendanceLogs.filter { it.status == "TEPAT_WAKTU" || it.status == "SELESAI" }
            "Terlambat" -> uiState.attendanceLogs.filter { it.status == "TERLAMBAT" }
            "Koreksi" -> uiState.attendanceLogs.filter { it.status == "KOREKSI" }
            else -> uiState.attendanceLogs
        }
    }

    val onTimeCount = uiState.attendanceLogs.count { it.status == "TEPAT_WAKTU" || it.status == "SELESAI" }
    val lateCount = uiState.attendanceLogs.count { it.status == "TERLAMBAT" }
    val totalCount = uiState.attendanceLogs.size

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Presensi & Jadwal Shift",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "Riwayat kehadiran tercatat secara real-time",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(
                        onClick = { showCorrectionDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("correction_button")
                    ) {
                        Icon(Icons.Filled.EditCalendar, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.btn_correction))
                    }
                }
            }

            // Monthly Summary Metrics
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AttendanceStatTile("Total Hadir", "$totalCount Hari", MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                    AttendanceStatTile("Tepat Waktu", "$onTimeCount Hari", StatusSuccess, Modifier.weight(1f))
                    AttendanceStatTile("Terlambat", "$lateCount Hari", StatusWarning, Modifier.weight(1f))
                }
            }

            // Weekly Shift Roster Strip
            item {
                SectionHeader(title = "Jadwal Shift Minggu Ini")
                Spacer(modifier = Modifier.height(8.dp))
                val storeBranch = uiState.employee?.storeBranch ?: "Toko / Cabang"
                val roster = remember(storeBranch) {
                    com.example.util.ShiftScheduleProvider.getUpcomingShifts(storeBranch, daysCount = 7)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    roster.forEach { shift ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (shift.isDayOff) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                width = if (shift.isToday) 1.5.dp else 1.dp,
                                color = if (shift.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier.width(86.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(shift.dayLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = shift.shiftCode,
                                    style = AlfaMonoTextStyles.figureMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (shift.isDayOff) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = shift.dateLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filters.forEach { filter ->
                        FilterChip(
                            selected = filter == selectedFilter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter) }
                        )
                    }
                }
            }

            if (filteredLogs.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Outlined.EventBusy,
                        title = "Belum ada riwayat presensi",
                        subtitle = "Catatan presensi pada filter ini akan muncul setelah Anda melakukan Clock In."
                    )
                }
            } else {
                items(filteredLogs, key = { "${it.id}_${it.dateKey}" }) { log ->
                    val (statusLabel, statusColor, statusBg) = attendanceStatusStyle(log.status)
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = Formatters.formatShortDateFromKey(log.dateKey),
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = "${log.shiftName} • ${log.locationLabel}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                StatusPill(label = statusLabel, color = statusColor, backgroundColor = statusBg)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Masuk", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(Formatters.formatHourMinute(log.clockInMillis), style = AlfaMonoTextStyles.figureMedium)
                                }
                                Column {
                                    Text("Istirahat", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(Formatters.formatHourMinute(log.breakStartMillis), style = AlfaMonoTextStyles.figureMedium)
                                }
                                Column {
                                    Text("Pulang", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(Formatters.formatHourMinute(log.clockOutMillis), style = AlfaMonoTextStyles.figureMedium)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Durasi", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = Formatters.formatWorkDuration(
                                            log.clockInMillis,
                                            log.clockOutMillis,
                                            log.breakStartMillis,
                                            log.breakEndMillis
                                        ),
                                        style = AlfaMonoTextStyles.figureMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }

                            if (log.note.isNotBlank()) {
                                Text(
                                    text = "Catatan: ${log.note}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCorrectionDialog) {
        AttendanceCorrectionDialog(
            onDismiss = { showCorrectionDialog = false },
            onSubmit = { dateKey, mode, note ->
                onSubmitCorrection(dateKey, mode, note)
                showCorrectionDialog = false
            }
        )
    }
}

@Composable
private fun AttendanceStatTile(
    label: String,
    value: String,
    accent: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = AlfaMonoTextStyles.figureMedium.copy(fontWeight = FontWeight.Bold), color = accent)
        }
    }
}

@Composable
private fun AttendanceCorrectionDialog(
    onDismiss: () -> Unit,
    onSubmit: (dateKey: String, workMode: String, note: String) -> Unit
) {
    var dateKey by remember { mutableStateOf(Formatters.formatDateKey()) }
    var workMode by remember { mutableStateOf("Toko / Cabang") }
    var note by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajukan Koreksi Presensi") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = dateKey,
                    onValueChange = { dateKey = it },
                    label = { Text("Tanggal (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Toko / Cabang", "Gudang / DC").forEach { mode ->
                        FilterChip(
                            selected = workMode == mode,
                            onClick = { workMode = mode },
                            label = { Text(mode, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = {
                        note = it
                        errorMsg = null
                    },
                    label = { Text("Alasan Koreksi") },
                    placeholder = { Text("Contoh: Lupa tap Clock Out karena jaringan toko") },
                    minLines = 2,
                    isError = errorMsg != null,
                    supportingText = errorMsg?.let { m -> { Text(m) } },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (note.isBlank()) {
                        errorMsg = "Mohon isi alasan koreksi presensi"
                    } else {
                        onSubmit(dateKey.trim(), workMode, note.trim())
                    }
                },
                modifier = Modifier.testTag("submit_correction_button")
            ) {
                Text("Simpan Koreksi")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
