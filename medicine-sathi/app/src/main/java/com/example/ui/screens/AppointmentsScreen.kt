package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Appointment
import com.example.ui.MainViewModel
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusBadge
import com.example.ui.localization.LocalAppStrings
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val strings = LocalAppStrings.current
    val appointments by viewModel.appointments.collectAsState()
    val members by viewModel.members.collectAsState()
    val doctors by viewModel.doctors.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Upcoming (Scheduled), 1: Completed, 2: All
    var isAddDialogOpen by remember { mutableStateOf(false) }
    var appointmentToEdit by remember { mutableStateOf<Appointment?>(null) }
    var appointmentToDelete by remember { mutableStateOf<Appointment?>(null) }

    val filteredAppointments = appointments.filter { apt ->
        when (selectedTab) {
            0 -> apt.status == "SCHEDULED"
            1 -> apt.status == "COMPLETED"
            else -> true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.upcomingAppointments, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isAddDialogOpen = true },
                modifier = Modifier.testTag("fab_add_appointment"),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = strings.addAppointment)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("appointments_screen")
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Upcoming") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Completed") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("All") }
                )
            }

            if (filteredAppointments.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Event,
                    title = strings.noAppointmentsTitle,
                    description = "Stay organized and never miss a doctor consultation.",
                    actionButtonText = strings.addAppointment,
                    onActionClick = { isAddDialogOpen = true },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredAppointments, key = { it.id }) { apt ->
                        val member = members.find { it.id == apt.familyMemberId }
                        AppointmentCard(
                            appointment = apt,
                            memberName = member?.fullName ?: "General",
                            onMarkCompleted = {
                                viewModel.saveAppointment(apt.copy(status = "COMPLETED"))
                            },
                            onEdit = { appointmentToEdit = apt },
                            onDelete = { appointmentToDelete = apt }
                        )
                    }
                }
            }
        }
    }

    if (isAddDialogOpen || appointmentToEdit != null) {
        AppointmentFormDialog(
            initial = appointmentToEdit,
            members = members,
            doctors = doctors,
            onDismiss = {
                isAddDialogOpen = false
                appointmentToEdit = null
            },
            onSave = { saved ->
                viewModel.saveAppointment(saved)
                isAddDialogOpen = false
                appointmentToEdit = null
            }
        )
    }

    appointmentToDelete?.let { apt ->
        ConfirmDeleteDialog(
            title = strings.confirmDelete,
            message = "Are you sure you want to delete this appointment?",
            onConfirm = { viewModel.deleteAppointment(apt) },
            onDismiss = { appointmentToDelete = null }
        )
    }
}

@Composable
fun AppointmentCard(
    appointment: Appointment,
    memberName: String,
    onMarkCompleted: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = appointment.doctorName.ifEmpty { "Doctor Appointment" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(status = appointment.status)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${appointment.hospitalClinic} • For: $memberName",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${appointment.appointmentDate} at ${appointment.appointmentTime}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (appointment.reason.isNotEmpty()) {
                Text(
                    text = "Reason: ${appointment.reason}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (appointment.status == "SCHEDULED") {
                    TextButton(onClick = onMarkCompleted) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mark Completed")
                    }
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun AppointmentFormDialog(
    initial: Appointment?,
    members: List<com.example.data.model.FamilyMember>,
    doctors: List<com.example.data.model.Doctor>,
    onDismiss: () -> Unit,
    onSave: (Appointment) -> Unit
) {
    val strings = LocalAppStrings.current
    var selectedMemberId by remember { mutableStateOf(initial?.familyMemberId ?: members.firstOrNull()?.id ?: 0L) }
    var selectedDoctorId by remember { mutableStateOf(initial?.doctorId) }
    var doctorName by remember { mutableStateOf(initial?.doctorName ?: "") }
    var hospitalClinic by remember { mutableStateOf(initial?.hospitalClinic ?: "") }
    var appointmentDate by remember { mutableStateOf(initial?.appointmentDate ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var appointmentTime by remember { mutableStateOf(initial?.appointmentTime ?: "10:00") }
    var reason by remember { mutableStateOf(initial?.reason ?: "") }
    var reminderBeforeMinutes by remember { mutableStateOf(initial?.reminderBeforeMinutes ?: 60) }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) strings.addAppointment else "Edit Appointment", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Family Member
                Text(strings.selectFamilyMember, style = MaterialTheme.typography.labelMedium)
                if (members.isNotEmpty()) {
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        members.forEach { m ->
                            FilterChip(
                                selected = selectedMemberId == m.id,
                                onClick = { selectedMemberId = m.id },
                                label = { Text(m.fullName) }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Doctor Quick Select
                if (doctors.isNotEmpty()) {
                    Text("Select Doctor", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        doctors.forEach { doc ->
                            FilterChip(
                                selected = selectedDoctorId == doc.id,
                                onClick = {
                                    selectedDoctorId = doc.id
                                    doctorName = doc.name
                                    hospitalClinic = doc.hospitalClinic
                                },
                                label = { Text(doc.name) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = doctorName,
                    onValueChange = { doctorName = it },
                    label = { Text(strings.doctorName) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = hospitalClinic,
                    onValueChange = { hospitalClinic = it },
                    label = { Text(strings.hospitalClinic) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = appointmentDate,
                        onValueChange = { appointmentDate = it },
                        label = { Text(strings.appointmentDate) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = appointmentTime,
                        onValueChange = { appointmentTime = it },
                        label = { Text(strings.appointmentTime) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text(strings.reason) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Reminder options
                Text("Reminder Timing", style = MaterialTheme.typography.labelMedium)
                listOf(30 to "30 mins before", 60 to "1 hour before", 120 to "2 hours before", 1440 to "1 day before").forEach { (mins, lbl) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { reminderBeforeMinutes = mins },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = reminderBeforeMinutes == mins, onClick = { reminderBeforeMinutes = mins })
                        Text(lbl, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(strings.notes) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val apt = (initial ?: Appointment(
                    familyMemberId = selectedMemberId,
                    appointmentDate = appointmentDate.trim(),
                    appointmentTime = appointmentTime.trim()
                )).copy(
                    familyMemberId = selectedMemberId,
                    doctorId = selectedDoctorId,
                    doctorName = doctorName.trim(),
                    hospitalClinic = hospitalClinic.trim(),
                    appointmentDate = appointmentDate.trim(),
                    appointmentTime = appointmentTime.trim(),
                    reason = reason.trim(),
                    reminderBeforeMinutes = reminderBeforeMinutes,
                    notes = notes.trim()
                )
                onSave(apt)
            }) {
                Text(strings.save)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
