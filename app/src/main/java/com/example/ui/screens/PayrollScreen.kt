package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.AlfaoneUiState
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.PinVerificationDialog
import com.example.ui.theme.AlfaCrimson
import com.example.ui.theme.AlfaGold
import com.example.ui.theme.AlfaMonoTextStyles
import com.example.ui.theme.AlfaNavy
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.util.Formatters

@Composable
fun PayrollScreen(
    uiState: AlfaoneUiState,
    onVerifyPinForSalary: (pin: String, onPinInvalid: () -> Unit) -> Unit,
    onHideSalary: () -> Unit
) {
    val context = LocalContext.current
    val employee = uiState.employee ?: return
    val payslips = uiState.payslips
    var selectedIndex by remember { mutableIntStateOf(0) }
    var showPinDialog by remember { mutableStateOf(false) }

    val activeSlip = payslips.getOrNull(selectedIndex)
    val revealed = uiState.isSalaryRevealed

    fun maskOrShow(amount: Long): String {
        return if (revealed) Formatters.formatIdr(amount) else "Rp •••••••"
    }

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
                            text = "Slip Gaji Digital",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "Dilindungi verifikasi PIN HC karyawan",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = {
                            if (revealed) onHideSalary() else showPinDialog = true
                        },
                        modifier = Modifier.testTag("reveal_salary_button")
                    ) {
                        Icon(
                            imageVector = if (revealed) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = stringResource(R.string.cd_toggle_salary_visibility),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (payslips.isEmpty() || activeSlip == null) {
                item {
                    EmptyStateCard(
                        icon = Icons.Outlined.ReceiptLong,
                        title = "Belum ada data slip gaji",
                        subtitle = "Slip gaji bulanan akan tampil di sini setiap periode penggajian."
                    )
                }
            } else {
                // Period Chips
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        payslips.forEachIndexed { index, slip ->
                            FilterChip(
                                selected = index == selectedIndex,
                                onClick = { selectedIndex = index },
                                label = { Text(slip.periodLabel) }
                            )
                        }
                    }
                }

                // Take-Home Pay Hero Card
                item {
                    Card(
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(listOf(AlfaNavy, AlfaCrimson))
                                )
                                .padding(22.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Take-Home Pay • ${activeSlip.periodLabel}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                    Surface(
                                        color = Color.White.copy(alpha = 0.16f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = activeSlip.payDateLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AlfaGold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = maskOrShow(activeSlip.takeHomePay),
                                    style = AlfaMonoTextStyles.clockHero,
                                    color = Color.White
                                )

                                Text(
                                    text = "${employee.bankName} ${employee.maskedAccount} • ${activeSlip.workDays} Hari Kerja • ${activeSlip.overtimeHours} Jam Lembur",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )

                                if (!revealed) {
                                    Button(
                                        onClick = { showPinDialog = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.White,
                                            contentColor = AlfaNavy
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.testTag("unlock_payslip_button")
                                    ) {
                                        Icon(Icons.Filled.Lock, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Buka Rincian dengan PIN HC")
                                    }
                                }
                            }
                        }
                    }
                }

                // Earnings Breakdown
                item {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Pendapatan",
                                style = MaterialTheme.typography.titleMedium,
                                color = StatusSuccess
                            )
                            PayslipLineRow("Gaji Pokok", maskOrShow(activeSlip.baseSalary))
                            PayslipLineRow("Tunjangan Jabatan & Makan", maskOrShow(activeSlip.allowance))
                            PayslipLineRow("Insentif Toko", maskOrShow(activeSlip.incentive))
                            PayslipLineRow("Upah Lembur (${activeSlip.overtimeHours} jam)", maskOrShow(activeSlip.overtimePay))
                            HorizontalDivider()
                            PayslipLineRow(
                                label = "Total Pendapatan",
                                value = maskOrShow(activeSlip.totalEarnings),
                                bold = true
                            )
                        }
                    }
                }

                // Deductions Breakdown
                item {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Potongan",
                                style = MaterialTheme.typography.titleMedium,
                                color = StatusError
                            )
                            PayslipLineRow("BPJS Kesehatan", maskOrShow(activeSlip.bpjsHealth))
                            PayslipLineRow("BPJS Ketenagakerjaan", maskOrShow(activeSlip.bpjsEmployment))
                            PayslipLineRow("Pajak PPh 21", maskOrShow(activeSlip.taxPph21))
                            PayslipLineRow("Iuran Koperasi Karyawan", maskOrShow(activeSlip.cooperative))
                            HorizontalDivider()
                            PayslipLineRow(
                                label = "Total Potongan",
                                value = maskOrShow(activeSlip.totalDeductions),
                                bold = true
                            )
                        }
                    }
                }

                // Share Button
                item {
                    Button(
                        onClick = {
                            if (!revealed) {
                                showPinDialog = true
                            } else {
                                val summary = buildString {
                                    appendLine("SLIP GAJI ALFAONE HC - ${activeSlip.periodLabel}")
                                    appendLine("Nama: ${employee.fullName} (NIK ${employee.nik})")
                                    appendLine("Unit: ${employee.storeBranch}")
                                    appendLine("Tanggal Bayar: ${activeSlip.payDateLabel}")
                                    appendLine("--------------------------------")
                                    appendLine("Total Pendapatan: ${Formatters.formatIdr(activeSlip.totalEarnings)}")
                                    appendLine("Total Potongan: ${Formatters.formatIdr(activeSlip.totalDeductions)}")
                                    appendLine("Take-Home Pay: ${Formatters.formatIdr(activeSlip.takeHomePay)}")
                                }
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Slip Gaji ${activeSlip.periodLabel}")
                                    putExtra(Intent.EXTRA_TEXT, summary)
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Bagikan Ringkasan Slip Gaji"))
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("share_payslip_button")
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.btn_share_payslip))
                    }
                }
            }
        }
    }

    if (showPinDialog) {
        PinVerificationDialog(
            title = "Verifikasi PIN HC",
            subtitle = "Masukkan 6 digit PIN HC Anda untuk membuka nominal slip gaji.",
            onDismiss = { showPinDialog = false },
            onConfirmPin = { pinAttempt, onError ->
                var valid = true
                onVerifyPinForSalary(pinAttempt) {
                    valid = false
                    onError("PIN HC tidak sesuai. Demo PIN: 123456")
                }
                if (valid) {
                    showPinDialog = false
                }
            }
        )
    }
}

@Composable
private fun PayslipLineRow(label: String, value: String, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (bold) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            color = if (bold) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = AlfaMonoTextStyles.figureMedium.copy(
                fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
