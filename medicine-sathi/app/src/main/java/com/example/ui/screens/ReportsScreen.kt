package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.MainViewModel
import com.example.ui.components.SectionHeader
import com.example.ui.localization.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val strings = LocalAppStrings.current
    val context = LocalContext.current

    val medicines by viewModel.medicines.collectAsState()
    val history by viewModel.history.collectAsState()
    val appointments by viewModel.appointments.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.reports, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("reports_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Assessment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Export and share your family's health reports with your doctor or caregiver.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Report 1: Medication Summary
            item {
                ReportCard(
                    title = "Active Medication Schedule Report",
                    description = "Includes all prescribed medicines, dosage units, frequency, and remaining quantity.",
                    countInfo = "${medicines.size} medicines recorded",
                    onShare = {
                        val csv = viewModel.generateMedicationReportCsv()
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Medicine Sathi - Medication Report:\n\n$csv")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Medication Report"))
                    }
                )
            }

            // Report 2: History & Adherence Report
            item {
                val totalDoses = history.size
                val takenDoses = history.count { it.status == "TAKEN" }
                val rate = if (totalDoses > 0) ((takenDoses.toDouble() / totalDoses) * 100).toInt() else 0

                ReportCard(
                    title = "Medication History & Adherence Report",
                    description = "Log of all taken, missed, and skipped medication doses with compliance statistics.",
                    countInfo = "$totalDoses total doses logged • $rate% adherence",
                    onShare = {
                        val csv = viewModel.generateHistoryReportCsv()
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Medicine Sathi - Medication History Log:\n\n$csv")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Adherence Report"))
                    }
                )
            }

            // Report 3: Appointments Summary
            item {
                ReportCard(
                    title = "Doctor Appointments Summary",
                    description = "List of all past and upcoming doctor consultations, clinics, and appointment dates.",
                    countInfo = "${appointments.size} appointments recorded",
                    onShare = {
                        val sb = StringBuilder()
                        sb.append("Doctor,Hospital,Date,Time,Status,Reason\n")
                        appointments.forEach { apt ->
                            sb.append("\"${apt.doctorName}\",\"${apt.hospitalClinic}\",\"${apt.appointmentDate}\",\"${apt.appointmentTime}\",\"${apt.status}\",\"${apt.reason}\"\n")
                        }
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Medicine Sathi - Appointments Report:\n\n$sb")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Appointments Report"))
                    }
                )
            }
        }
    }
}

@Composable
fun ReportCard(
    title: String,
    description: String,
    countInfo: String,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = countInfo, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onShare,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export & Share Report")
            }
        }
    }
}
