package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.TodayDoseItem
import com.example.ui.components.*
import com.example.ui.localization.LocalAppStrings
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigate: (Screen) -> Unit,
    onAddMedicineClick: () -> Unit,
    onAddMemberClick: () -> Unit,
    onAddDoctorClick: () -> Unit,
    onAddAppointmentClick: () -> Unit,
    onAddPrescriptionClick: () -> Unit
) {
    val strings = LocalAppStrings.current
    val todayDoses by viewModel.todayDoses.collectAsState()
    val medicines by viewModel.medicines.collectAsState()
    val appointments by viewModel.appointments.collectAsState()
    val expiringMedicines by viewModel.expiringMedicines.collectAsState()
    val unreadNotifs by viewModel.unreadNotificationsCount.collectAsState()
    val lowStockMeds by viewModel.lowInventoryMedicines.collectAsState()

    var snoozeDialogItem by remember { mutableStateOf<TodayDoseItem?>(null) }

    val completedCount = todayDoses.count { it.status.equals("TAKEN", ignoreCase = true) }
    val missedCount = todayDoses.count { it.status.equals("MISSED", ignoreCase = true) }
    val upcomingCount = todayDoses.count { it.status.equals("UPCOMING", ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HealthAndSafety,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = strings.appName,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onNavigate(Screen.SEARCH) },
                        modifier = Modifier.testTag("home_search_button")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = strings.search)
                    }
                    IconButton(
                        onClick = { onNavigate(Screen.NOTIFICATIONS) },
                        modifier = Modifier.testTag("home_notifications_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotifs > 0) {
                                    Badge { Text(unreadNotifs.toString()) }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = strings.notifications)
                        }
                    }
                    IconButton(
                        onClick = { onNavigate(Screen.SETTINGS) },
                        modifier = Modifier.testTag("home_settings_button")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = strings.settings)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("home_screen_content"),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Quick Actions Horizontal Row
            item {
                SectionHeader(title = strings.quickActions)
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        QuickActionButton(
                            icon = Icons.Default.AddCircle,
                            label = strings.addMedicine,
                            color = MaterialTheme.colorScheme.primary,
                            onClick = onAddMedicineClick,
                            tag = "quick_add_medicine"
                        )
                    }
                    item {
                        QuickActionButton(
                            icon = Icons.Default.PersonAdd,
                            label = strings.addFamilyMember,
                            color = OceanSecondaryLight,
                            onClick = onAddMemberClick,
                            tag = "quick_add_member"
                        )
                    }
                    item {
                        QuickActionButton(
                            icon = Icons.Default.MedicalInformation,
                            label = strings.addDoctor,
                            color = Color(0xFF7C3AED),
                            onClick = onAddDoctorClick,
                            tag = "quick_add_doctor"
                        )
                    }
                    item {
                        QuickActionButton(
                            icon = Icons.Default.Event,
                            label = strings.addAppointment,
                            color = CoralTertiaryLight,
                            onClick = onAddAppointmentClick,
                            tag = "quick_add_appointment"
                        )
                    }
                    item {
                        QuickActionButton(
                            icon = Icons.Default.UploadFile,
                            label = strings.uploadPrescription,
                            color = Color(0xFF059669),
                            onClick = onAddPrescriptionClick,
                            tag = "quick_add_prescription"
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Expiry Alert Banner
            if (expiringMedicines.isNotEmpty()) {
                val expiredCount = expiringMedicines.count { it.status == "EXPIRED" }
                val soonCount = expiringMedicines.size - expiredCount

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable { onNavigate(Screen.INVENTORY) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (expiredCount > 0) StatusDanger.copy(alpha = 0.12f) else StatusWarning.copy(alpha = 0.12f)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = if (expiredCount > 0) StatusDanger else StatusWarning,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (expiredCount > 0) "Expired Medicines Detected!" else "Medicines Expiring Soon",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (expiredCount > 0) StatusDanger else Color(0xFFB45309)
                                )
                                Text(
                                    text = if (expiredCount > 0) "$expiredCount expired, $soonCount expiring soon. Tap to review." else "$soonCount medicines expiring within 30 days. Tap to review.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Low Stock Alert Banner
            if (lowStockMeds.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable { onNavigate(Screen.INVENTORY) },
                        colors = CardDefaults.cardColors(containerColor = StatusWarning.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = StatusWarning,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${lowStockMeds.size} ${strings.lowStock}",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFB45309)
                                )
                                Text(
                                    text = "Restock soon to ensure uninterrupted medication schedules.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Today's Overview Stats
            item {
                SectionHeader(title = strings.todayOverview)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = strings.completedDoses,
                        value = completedCount.toString(),
                        icon = Icons.Default.CheckCircle,
                        containerColor = StatusSuccess.copy(alpha = 0.12f),
                        contentColor = StatusSuccess,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = strings.upcomingDoses,
                        value = upcomingCount.toString(),
                        icon = Icons.Default.Schedule,
                        containerColor = OceanSecondaryLight.copy(alpha = 0.12f),
                        contentColor = OceanSecondaryLight,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = strings.missedDoses,
                        value = missedCount.toString(),
                        icon = Icons.Default.ErrorOutline,
                        containerColor = StatusDanger.copy(alpha = 0.12f),
                        contentColor = StatusDanger,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Today's Medicines list
            item {
                SectionHeader(
                    title = strings.todaysMedicines,
                    actionText = if (todayDoses.isNotEmpty()) strings.all else null,
                    onActionClick = { onNavigate(Screen.MEDICINES) }
                )
            }

            if (medicines.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.Medication,
                        title = strings.noMedicinesTitle,
                        description = "Start your organized medicine routine by adding your first medicine.",
                        actionButtonText = strings.addMedicine,
                        onActionClick = onAddMedicineClick,
                        testTag = "home_empty_medicines"
                    )
                }
            } else if (todayDoses.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "No doses scheduled for today.",
                            modifier = Modifier.padding(20.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(todayDoses) { dose ->
                    TodayDoseCard(
                        item = dose,
                        onTaken = { viewModel.markDoseTaken(dose) },
                        onSkip = { viewModel.markDoseSkipped(dose) },
                        onSnooze = { snoozeDialogItem = dose }
                    )
                }
            }

            // Upcoming Appointments Preview
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(
                    title = strings.upcomingAppointments,
                    actionText = if (appointments.isNotEmpty()) strings.all else null,
                    onActionClick = { onNavigate(Screen.APPOINTMENTS) }
                )
            }

            val upcomingAppointments = appointments.filter { it.status == "SCHEDULED" }.take(2)
            if (upcomingAppointments.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = strings.noAppointmentsTitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = onAddAppointmentClick) {
                                Text(strings.addAppointment)
                            }
                        }
                    }
                }
            } else {
                items(upcomingAppointments) { appointment ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CoralTertiaryLight.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Event, contentDescription = null, tint = CoralTertiaryLight)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = appointment.doctorName.ifEmpty { "Doctor Appointment" },
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "${appointment.hospitalClinic} • ${appointment.appointmentDate} at ${appointment.appointmentTime}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            StatusBadge(status = appointment.status)
                        }
                    }
                }
            }
        }
    }

    // Snooze dialog
    snoozeDialogItem?.let { item ->
        SnoozeDialog(
            medicineName = item.medicineName,
            onSnoozeSelected = { minutes ->
                viewModel.snoozeDose(item, minutes)
            },
            onDismiss = { snoozeDialogItem = null }
        )
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    tag: String
) {
    Card(
        modifier = Modifier
            .width(105.dp)
            .height(90.dp)
            .clickable { onClick() }
            .testTag(tag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = color,
                maxLines = 2,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun TodayDoseCard(
    item: TodayDoseItem,
    onTaken: () -> Unit,
    onSkip: () -> Unit,
    onSnooze: () -> Unit
) {
    val strings = LocalAppStrings.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .testTag("dose_item_${item.medicineId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = item.time,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.memberName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(status = item.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.medicineName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${item.dosage} • ${item.beforeAfterFood}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${item.remainingQuantity} ${item.unit} left",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (item.remainingQuantity <= 5) StatusWarning else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (item.remainingQuantity <= 5) FontWeight.Bold else FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (item.status.equals("TAKEN", ignoreCase = true)) {
                    FilledTonalButton(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.taken)
                    }
                } else {
                    Button(
                        onClick = onTaken,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dose_taken_button_${item.medicineId}"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.taken)
                    }

                    OutlinedButton(
                        onClick = onSnooze,
                        modifier = Modifier.testTag("dose_snooze_button_${item.medicineId}"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Outlined.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.snooze)
                    }

                    OutlinedButton(
                        onClick = onSkip,
                        modifier = Modifier.testTag("dose_skip_button_${item.medicineId}"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(strings.skip)
                    }
                }
            }
        }
    }
}
