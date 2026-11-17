package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.LeaveRequestEntity
import com.example.ui.AlfaoneUiState
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.StatusPill
import com.example.ui.components.requestStatusStyle
import com.example.ui.theme.AlfaMonoTextStyles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestsScreen(
    uiState: AlfaoneUiState,
    initialDraftCategory: String? = null,
    onConsumeDraftCategory: () -> Unit = {},
    onSubmitRequest: (
        category: String,
        title: String,
        startDateLabel: String,
        endDateLabel: String,
        durationLabel: String,
        reason: String,
        attachmentUri: String?
    ) -> Unit,
    onCancelRequest: (Int) -> Unit
) {
    val employee = uiState.employee ?: return
    val categories = listOf("Semua", "CUTI", "SAKIT", "IZIN", "LEMBUR")
    var selectedCategory by remember { mutableStateOf("Semua") }
    var showNewRequestSheet by remember(initialDraftCategory) {
        mutableStateOf(initialDraftCategory != null)
    }
    var draftCategory by remember(initialDraftCategory) {
        mutableStateOf(initialDraftCategory ?: "CUTI")
    }
    var detailRequest by remember { mutableStateOf<LeaveRequestEntity?>(null) }

    val filteredRequests = remember(uiState.leaveRequests, selectedCategory) {
        if (selectedCategory == "Semua") uiState.leaveRequests
        else uiState.leaveRequests.filter { it.category == selectedCategory }
    }

    val remainingLeave = (employee.annualLeaveQuota - employee.annualLeaveUsed).coerceAtLeast(0)
    val pendingCount = uiState.leaveRequests.count { it.status == "MENUNGGU" }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 92.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = "Pengajuan Cuti, Izin & Lembur",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "Kelola ketidakhadiran & surat dokter secara digital",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Quota Summary Cards
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Sisa Cuti", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$remainingLeave Hari", style = AlfaMonoTextStyles.figureLarge, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Menunggu Approval", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$pendingCount Pengajuan", style = AlfaMonoTextStyles.figureLarge, color = MaterialTheme.colorScheme.secondary)
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
                        categories.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(categoryDisplayName(cat)) }
                            )
                        }
                    }
                }

                if (filteredRequests.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Outlined.Assignment,
                            title = "Belum ada pengajuan",
                            subtitle = "Tekan tombol + Ajukan di kanan bawah untuk membuat pengajuan baru."
                        )
                    }
                } else {
                    items(filteredRequests, key = { it.id }) { req ->
                        val (statusLabel, statusColor, statusBg) = requestStatusStyle(req.status)
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { detailRequest = req }
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    StatusPill(
                                        label = categoryDisplayName(req.category),
                                        color = MaterialTheme.colorScheme.secondary,
                                        backgroundColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                    StatusPill(
                                        label = statusLabel,
                                        color = statusColor,
                                        backgroundColor = statusBg
                                    )
                                }
                                Text(text = req.title, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = "${req.startDateLabel} (${req.durationLabel})",
                                    style = AlfaMonoTextStyles.figureMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = req.reason,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Approver: ${req.approverName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (req.status == "MENUNGGU") {
                                        IconButton(
                                            onClick = { onCancelRequest(req.id) },
                                            modifier = Modifier.testTag("cancel_request_${req.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.DeleteOutline,
                                                contentDescription = "Batalkan pengajuan",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = {
                draftCategory = "CUTI"
                showNewRequestSheet = true
            },
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text(stringResource(R.string.btn_new_request)) },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("new_request_fab")
        )
    }

    if (showNewRequestSheet) {
        NewRequestBottomSheet(
            initialCategory = draftCategory,
            onDismiss = {
                showNewRequestSheet = false
                onConsumeDraftCategory()
            },
            onSubmit = { cat, title, start, end, duration, reason, uri ->
                onSubmitRequest(cat, title, start, end, duration, reason, uri)
                showNewRequestSheet = false
                onConsumeDraftCategory()
            }
        )
    }

    detailRequest?.let { req ->
        ModalBottomSheet(onDismissRequest = { detailRequest = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val (statusLabel, statusColor, statusBg) = requestStatusStyle(req.status)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusPill(
                        label = categoryDisplayName(req.category),
                        color = MaterialTheme.colorScheme.secondary,
                        backgroundColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                    StatusPill(label = statusLabel, color = statusColor, backgroundColor = statusBg)
                }
                Text(req.title, style = MaterialTheme.typography.headlineSmall)
                Text("Periode: ${req.startDateLabel} – ${req.endDateLabel} (${req.durationLabel})", style = MaterialTheme.typography.bodyMedium)
                Text("Alasan: ${req.reason}", style = MaterialTheme.typography.bodyMedium)
                Text("Atasan: ${req.approverName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                if (!req.attachmentUri.isNullOrBlank()) {
                    Text("Lampiran Dokumen:", style = MaterialTheme.typography.labelLarge)
                    AsyncImage(
                        model = Uri.parse(req.attachmentUri),
                        contentDescription = stringResource(R.string.cd_attachment_preview),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                }
                TextButton(
                    onClick = { detailRequest = null },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Tutup")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewRequestBottomSheet(
    initialCategory: String,
    onDismiss: () -> Unit,
    onSubmit: (
        category: String,
        title: String,
        startDateLabel: String,
        endDateLabel: String,
        durationLabel: String,
        reason: String,
        attachmentUri: String?
    ) -> Unit
) {
    val cats = listOf("CUTI", "SAKIT", "IZIN", "LEMBUR")
    var category by remember { mutableStateOf(if (initialCategory in cats) initialCategory else "CUTI") }
    var title by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("20 Nov 2026") }
    var endDate by remember { mutableStateOf("20 Nov 2026") }
    var duration by remember { mutableStateOf(if (category == "LEMBUR") "3 Jam" else "1 Hari") }
    var reason by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> if (uri != null) selectedImageUri = uri }
    )

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Buat Pengajuan Baru", style = MaterialTheme.typography.headlineSmall)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                cats.forEach { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = {
                            category = cat
                            duration = if (cat == "LEMBUR") "3 Jam" else "1 Hari"
                        },
                        label = { Text(categoryDisplayName(cat)) }
                    )
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    errorMsg = null
                },
                label = { Text("Judul Pengajuan") },
                placeholder = {
                    Text(
                        when (category) {
                            "LEMBUR" -> "Contoh: Lembur Promo Gajian"
                            "SAKIT" -> "Contoh: Izin Sakit Klinik"
                            "IZIN" -> "Contoh: Izin Keperluan Keluarga"
                            else -> "Contoh: Cuti Tahunan"
                        }
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("request_title_input")
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Tanggal Mulai") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = endDate,
                    onValueChange = { endDate = it },
                    label = { Text("Tanggal Selesai") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it },
                label = { Text(if (category == "LEMBUR") "Durasi Jam" else "Durasi Hari") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = reason,
                onValueChange = {
                    reason = it
                    errorMsg = null
                },
                label = { Text("Keterangan / Alasan") },
                minLines = 2,
                isError = errorMsg != null,
                supportingText = errorMsg?.let { msg -> { Text(msg) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("request_reason_input")
            )

            // Zero-Permission Photo Picker for supporting docs (e.g. Surat Dokter)
            OutlinedButton(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("attach_document_button")
            ) {
                Icon(Icons.Filled.AttachFile, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (selectedImageUri == null) "Lampirkan Foto / Surat Dokter (Opsional)"
                    else "Ganti Lampiran Foto"
                )
            }

            selectedImageUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = stringResource(R.string.cd_attachment_preview),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            Button(
                onClick = {
                    val resolvedTitle = title.ifBlank { categoryDisplayName(category) }
                    if (reason.isBlank()) {
                        errorMsg = "Mohon lengkapi alasan pengajuan"
                    } else {
                        onSubmit(
                            category,
                            resolvedTitle,
                            startDate.trim(),
                            endDate.trim(),
                            duration.trim(),
                            reason.trim(),
                            selectedImageUri?.toString()
                        )
                    }
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_request_button")
            ) {
                Text("Kirim Pengajuan", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

private fun categoryDisplayName(category: String): String {
    return when (category) {
        "CUTI" -> "Cuti"
        "SAKIT" -> "Sakit"
        "IZIN" -> "Izin"
        "LEMBUR" -> "Lembur"
        else -> category
    }
}
