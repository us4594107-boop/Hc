package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.AnnouncementEntity
import com.example.ui.AlfaoneUiState
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusPill
import com.example.ui.components.attendanceStatusStyle
import com.example.ui.theme.AlfaCrimson
import com.example.ui.theme.AlfaGold
import com.example.ui.theme.AlfaMonoTextStyles
import com.example.ui.theme.AlfaNavy
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusSuccessBg
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.StatusWarningBg
import com.example.util.Formatters
import com.example.util.LocationCheckResult
import com.example.util.LocationHelper
import com.example.util.ShiftScheduleProvider
import com.example.util.UpcomingShift

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    uiState: AlfaoneUiState,
    onClockIn: (workMode: String, locationLabel: String) -> Unit,
    onStartBreak: () -> Unit,
    onEndBreak: () -> Unit,
    onClockOut: (locationLabel: String) -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToRequests: (defaultCategory: String?) -> Unit,
    onNavigateToPayroll: () -> Unit,
    onNavigateToTraining: () -> Unit,
    onMarkAnnouncementRead: (String) -> Unit
) {
    val context = LocalContext.current
    val employee = uiState.employee ?: return
    val todayLog = uiState.todayAttendance
    val workModes = listOf("Toko / Cabang", "Gudang / DC", "Kantor Pusat")
    var selectedWorkMode by remember { mutableStateOf(workModes.first()) }
    var locationResult by remember(selectedWorkMode) {
        mutableStateOf(LocationHelper.resolveCurrentLocation(context, selectedWorkMode))
    }
    var selectedAnnouncement by remember { mutableStateOf<AnnouncementEntity?>(null) }

    val upcomingShifts = remember(employee.storeBranch) {
        ShiftScheduleProvider.getUpcomingShifts(employee.storeBranch, daysCount = 6)
    }
    val pendingRequests = remember(uiState.leaveRequests) {
        uiState.leaveRequests.filter { it.status == "MENUNGGU" }
    }
    val unreadAnnouncementsCount = remember(uiState.announcements) {
        uiState.announcements.count { !it.isRead }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        locationResult = if (granted) {
            LocationHelper.resolveCurrentLocation(context, selectedWorkMode)
        } else {
            LocationCheckResult(
                isVerified = false,
                label = "$selectedWorkMode • Tanpa GPS",
                detail = "Izin lokasi ditolak; presensi tetap dapat dicatat secara lokal."
            )
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Top User Greeting & Notification Bell with M3 Badge
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_employee_avatar),
                            contentDescription = stringResource(R.string.cd_employee_avatar),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                        )
                        Column {
                            Text(
                                text = "Halo, ${employee.fullName}",
                                style = MaterialTheme.typography.titleLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "NIK ${employee.nik} • ${employee.storeBranch}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    val totalAlerts = unreadAnnouncementsCount + pendingRequests.size
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .size(48.dp)
                            .clickable {
                                uiState.announcements.firstOrNull()?.let {
                                    selectedAnnouncement = it
                                    onMarkAnnouncementRead(it.id)
                                }
                            }
                            .testTag("announcements_bell_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            BadgedBox(
                                badge = {
                                    if (totalAlerts > 0) {
                                        Badge {
                                            Text("$totalAlerts")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.NotificationsActive,
                                    contentDescription = "Notifikasi HR",
                                    tint = if (totalAlerts > 0) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 2. Attendance Status Summary & Live Clock-In Hero ElevatedCard
            item {
                val isClockedIn = todayLog?.clockInMillis != null
                val isOnBreak = todayLog?.breakStartMillis != null && todayLog.breakEndMillis == null
                val isClockedOut = todayLog?.clockOutMillis != null

                val statusBadgeText = when {
                    isClockedOut -> "Shift Selesai"
                    isOnBreak -> "Sedang Istirahat"
                    isClockedIn -> "Sedang Bekerja"
                    else -> "Belum Clock In"
                }

                val cardContainer by animateColorAsState(
                    targetValue = when {
                        isClockedOut -> AlfaNavy
                        isClockedIn -> AlfaCrimson
                        else -> AlfaNavy
                    },
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "dashboard_hero_color"
                )

                val todayShift = upcomingShifts.firstOrNull()
                val onTimeCount = uiState.attendanceLogs.count {
                    it.status == "TEPAT_WAKTU" || it.status == "SELESAI"
                }
                val lateCount = uiState.attendanceLogs.count { it.status == "TERLAMBAT" }

                ElevatedCard(
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("attendance_summary_card")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(cardContainer, AlfaNavy.copy(alpha = 0.94f))
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(
                                        text = Formatters.formatFullDate(uiState.currentTimeMillis),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                    Text(
                                        text = Formatters.formatClockTime(uiState.currentTimeMillis),
                                        style = AlfaMonoTextStyles.clockHero,
                                        color = Color.White
                                    )
                                }
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        color = if (isClockedOut || isClockedIn) {
                                            StatusSuccessBg
                                        } else {
                                            StatusWarningBg
                                        },
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = statusBadgeText,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isClockedOut || isClockedIn) StatusSuccess else StatusWarning,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                    Surface(
                                        color = Color.White.copy(alpha = 0.16f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            text = "${todayShift?.shiftTitle ?: "Shift Pagi"} (${todayShift?.shiftCode ?: "P1"}) • ${todayShift?.timeWindow ?: "07:00 - 15:00"}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AlfaGold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            // Attendance Monthly Summary Strip inside Hero
                            Surface(
                                color = Color.White.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    PunchTimeStat("Hadir", "${uiState.attendanceLogs.size} Hari")
                                    PunchTimeStat("Tepat Waktu", "$onTimeCount Hari")
                                    PunchTimeStat("Terlambat", "$lateCount Hari")
                                    PunchTimeStat(
                                        "Jam Hari Ini",
                                        Formatters.formatWorkDuration(
                                            todayLog?.clockInMillis,
                                            todayLog?.clockOutMillis,
                                            todayLog?.breakStartMillis,
                                            todayLog?.breakEndMillis
                                        )
                                    )
                                }
                            }

                            // Work Mode Selector (before Clock In)
                            if (!isClockedIn) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    workModes.forEach { mode ->
                                        FilterChip(
                                            selected = mode == selectedWorkMode,
                                            onClick = { selectedWorkMode = mode },
                                            label = {
                                                Text(
                                                    text = mode,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    maxLines = 1
                                                )
                                            }
                                        )
                                    }
                                }
                            }

                            // Location Status Pill
                            Surface(
                                color = Color.White.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (!LocationHelper.hasCoarseLocationPermission(context)) {
                                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                                        } else {
                                            locationResult = LocationHelper.resolveCurrentLocation(context, selectedWorkMode)
                                        }
                                    }
                                    .testTag("verify_location_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = if (locationResult.isVerified) Icons.Filled.LocationOn else Icons.Outlined.MyLocation,
                                        contentDescription = null,
                                        tint = if (locationResult.isVerified) AlfaGold else Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = locationResult.label,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White
                                        )
                                        Text(
                                            text = locationResult.detail,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                    Text(
                                        text = if (LocationHelper.hasCoarseLocationPermission(context)) "Segarkan" else "Izinkan",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = AlfaGold
                                    )
                                }
                            }

                            // Today's punch timestamps
                            if (todayLog != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    PunchTimeStat("Masuk", Formatters.formatHourMinute(todayLog.clockInMillis))
                                    PunchTimeStat("Istirahat", Formatters.formatHourMinute(todayLog.breakStartMillis))
                                    PunchTimeStat("Selesai Ist.", Formatters.formatHourMinute(todayLog.breakEndMillis))
                                    PunchTimeStat("Pulang", Formatters.formatHourMinute(todayLog.clockOutMillis))
                                }
                            }

                            // Primary Clock Actions
                            when {
                                !isClockedIn -> {
                                    Button(
                                        onClick = {
                                            val locLabel = if (locationResult.isVerified) {
                                                locationResult.label
                                            } else {
                                                "$selectedWorkMode • Perangkat Lokal"
                                            }
                                            onClockIn(selectedWorkMode, locLabel)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.White,
                                            contentColor = AlfaCrimson
                                        ),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                            .testTag("clock_in_button")
                                    ) {
                                        Icon(Icons.Filled.Login, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(R.string.btn_clock_in),
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }

                                !isClockedOut -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        if (todayLog.breakEndMillis == null) {
                                            OutlinedButton(
                                                onClick = {
                                                    if (isOnBreak) onEndBreak() else onStartBreak()
                                                },
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = Color.White
                                                ),
                                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                                                shape = RoundedCornerShape(14.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(48.dp)
                                                    .testTag(if (isOnBreak) "end_break_button" else "start_break_button")
                                            ) {
                                                Icon(
                                                    Icons.Filled.Coffee,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (isOnBreak) {
                                                        stringResource(R.string.btn_end_break)
                                                    } else {
                                                        stringResource(R.string.btn_start_break)
                                                    },
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                        Button(
                                            onClick = {
                                                val locLabel = if (locationResult.isVerified) {
                                                    locationResult.label
                                                } else {
                                                    "$selectedWorkMode • Perangkat Lokal"
                                                }
                                                onClockOut(locLabel)
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color.White,
                                                contentColor = AlfaNavy
                                            ),
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                                .testTag("clock_out_button")
                                        ) {
                                            Icon(
                                                Icons.Filled.Logout,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(stringResource(R.string.btn_clock_out))
                                        }
                                    }
                                }

                                else -> {
                                    val (label, _, _) = attendanceStatusStyle(todayLog.status)
                                    Surface(
                                        color = StatusSuccessBg,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Filled.AccessTimeFilled,
                                                contentDescription = null,
                                                tint = StatusSuccess,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Shift Hari Ini Selesai ($label)",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = StatusSuccess
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Pending Request Notifications Section (Material 3 OutlinedCard + Badge + ListItem)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Notifikasi Pengajuan",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (pendingRequests.isNotEmpty()) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ) {
                                Text("${pendingRequests.size}")
                            }
                        }
                    }
                    TextButton(
                        onClick = { onNavigateToRequests(null) },
                        modifier = Modifier.testTag("view_all_requests_button")
                    ) {
                        Text("Lihat Semua")
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedCard(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(
                        1.dp,
                        if (pendingRequests.isNotEmpty()) {
                            StatusWarning.copy(alpha = 0.55f)
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pending_requests_card")
                ) {
                    if (pendingRequests.isEmpty()) {
                        ListItem(
                            leadingContent = {
                                Surface(
                                    shape = CircleShape,
                                    color = StatusSuccessBg,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = null,
                                            tint = StatusSuccess
                                        )
                                    }
                                }
                            },
                            headlineContent = {
                                Text(
                                    text = "Tidak ada pengajuan tertunda",
                                    style = MaterialTheme.typography.titleSmall
                                )
                            },
                            supportingContent = {
                                Text(
                                    text = "Semua pengajuan cuti, izin, dan lembur Anda sudah diproses.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            trailingContent = {
                                FilledTonalButton(
                                    onClick = { onNavigateToRequests("CUTI") }
                                ) {
                                    Text("Ajukan")
                                }
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    } else {
                        Column {
                            pendingRequests.take(3).forEachIndexed { idx, req ->
                                ListItem(
                                    leadingContent = {
                                        Surface(
                                            shape = CircleShape,
                                            color = StatusWarningBg,
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Filled.HourglassTop,
                                                    contentDescription = null,
                                                    tint = StatusWarning
                                                )
                                            }
                                        }
                                    },
                                    overlineContent = {
                                        Text(
                                            text = "${req.category} • MENUNGGU APPROVAL",
                                            color = StatusWarning,
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    headlineContent = {
                                        Text(
                                            text = req.title,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                    },
                                    supportingContent = {
                                        Text(
                                            text = "${req.startDateLabel} (${req.durationLabel}) • ${req.approverName}",
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    trailingContent = {
                                        AssistChip(
                                            onClick = { onNavigateToRequests(null) },
                                            label = { Text("Detail") }
                                        )
                                    },
                                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                    modifier = Modifier.clickable { onNavigateToRequests(null) }
                                )
                                if (idx < pendingRequests.take(3).lastIndex) {
                                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                }
                            }
                        }
                    }
                }
            }

            // 4. Upcoming Shifts Section (LazyRow of Material 3 Elevated/Outlined Cards)
            item {
                SectionHeader(
                    title = "Jadwal Shift Mendatang",
                    actionLabel = "Presensi",
                    onActionClick = onNavigateToAttendance
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("upcoming_shifts_row")
                ) {
                    items(upcomingShifts, key = { it.id }) { shift ->
                        UpcomingShiftCard(
                            shift = shift,
                            onClick = onNavigateToAttendance
                        )
                    }
                }
            }

            // 5. Quick Services Grid
            item {
                SectionHeader(title = "Layanan Cepat HC")
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickServiceCard(
                        icon = Icons.Filled.EventAvailable,
                        label = "Ajukan Cuti",
                        accent = AlfaCrimson,
                        onClick = { onNavigateToRequests("CUTI") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_leave_button")
                    )
                    QuickServiceCard(
                        icon = Icons.Filled.MoreTime,
                        label = "Lembur",
                        accent = AlfaGold,
                        onClick = { onNavigateToRequests("LEMBUR") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_overtime_button")
                    )
                    QuickServiceCard(
                        icon = Icons.Filled.Payments,
                        label = "Slip Gaji",
                        accent = StatusSuccess,
                        onClick = onNavigateToPayroll,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_payroll_button")
                    )
                    QuickServiceCard(
                        icon = Icons.Filled.School,
                        label = "E-Learning",
                        accent = AlfaNavy,
                        onClick = onNavigateToTraining,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_training_button")
                    )
                }
            }

            // 6. Leave & Training Summary Strip
            item {
                val remainingLeave = (employee.annualLeaveQuota - employee.annualLeaveUsed).coerceAtLeast(0)
                val completedModules = uiState.trainingModules.count { it.isCompleted }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedCard(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Sisa Cuti Tahunan",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$remainingLeave Hari",
                                style = AlfaMonoTextStyles.figureLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Terpakai ${employee.annualLeaveUsed} dari ${employee.annualLeaveQuota}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedCard(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToTraining() }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Progres E-Learning",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$completedModules / ${uiState.trainingModules.size} Modul",
                                style = AlfaMonoTextStyles.figureLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "Ketuk untuk lanjut belajar",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 7. HR Announcements Section
            item {
                SectionHeader(title = "Buletin & Info HC")
            }

            items(uiState.announcements, key = { it.id }) { announcement ->
                OutlinedCard(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (announcement.isRead) {
                            MaterialTheme.colorScheme.outlineVariant
                        } else {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedAnnouncement = announcement
                            onMarkAnnouncementRead(announcement.id)
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Campaign,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${announcement.category} • ${announcement.publishedLabel}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (!announcement.isRead) {
                                    StatusPill(
                                        label = "Baru",
                                        color = MaterialTheme.colorScheme.primary,
                                        backgroundColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = announcement.title,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = announcement.summary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }

    selectedAnnouncement?.let { item ->
        ModalBottomSheet(onDismissRequest = { selectedAnnouncement = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatusPill(
                    label = item.category,
                    color = MaterialTheme.colorScheme.secondary,
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer
                )
                Text(text = item.title, style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = item.publishedLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = item.body,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(
                    onClick = { selectedAnnouncement = null },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Tutup")
                }
            }
        }
    }
}

@Composable
private fun UpcomingShiftCard(
    shift: UpcomingShift,
    onClick: () -> Unit
) {
    val borderColor = when {
        shift.isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    val containerColor = when {
        shift.isDayOff -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        shift.isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        else -> MaterialTheme.colorScheme.surface
    }

    OutlinedCard(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.outlinedCardColors(containerColor = containerColor),
        border = BorderStroke(if (shift.isToday) 1.5.dp else 1.dp, borderColor),
        modifier = Modifier
            .width(156.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = shift.dayLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (shift.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = shift.dateLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (shift.isDayOff) {
                        MaterialTheme.colorScheme.outlineVariant
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    }
                ) {
                    Text(
                        text = shift.shiftCode,
                        style = AlfaMonoTextStyles.badgeCode.copy(fontWeight = FontWeight.Bold),
                        color = if (shift.isDayOff) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = null,
                    tint = if (shift.isDayOff) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = shift.timeWindow,
                    style = AlfaMonoTextStyles.figureMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1
                )
            }

            Text(
                text = shift.shiftTitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PunchTimeStat(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.72f)
        )
        Text(
            text = value,
            style = AlfaMonoTextStyles.figureMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
    }
}

@Composable
private fun QuickServiceCard(
    icon: ImageVector,
    label: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = accent.copy(alpha = 0.12f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = accent,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
