package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Prescription
import com.example.ui.MainViewModel
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.localization.LocalAppStrings
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrescriptionsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val prescriptions by viewModel.prescriptions.collectAsState()
    val members by viewModel.members.collectAsState()
    val doctors by viewModel.doctors.collectAsState()

    var isAddDialogOpen by remember { mutableStateOf(false) }
    var prescriptionToEdit by remember { mutableStateOf<Prescription?>(null) }
    var prescriptionToDelete by remember { mutableStateOf<Prescription?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.prescriptions, fontWeight = FontWeight.Bold) },
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
                modifier = Modifier.testTag("fab_add_prescription"),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = strings.uploadPrescription, tint = Color.White)
            }
        }
    ) { innerPadding ->
        if (prescriptions.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Description,
                title = strings.noPrescriptionsTitle,
                description = "Store photos and notes of doctor prescriptions to keep them organized and accessible.",
                actionButtonText = strings.uploadPrescription,
                onActionClick = { isAddDialogOpen = true },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(prescriptions, key = { it.id }) { prescription ->
                    val member = members.find { it.id == prescription.familyMemberId }
                    val doctor = doctors.find { it.id == prescription.doctorId }

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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(text = prescription.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            text = "For: ${member?.fullName ?: "General"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row {
                                    IconButton(onClick = {
                                        val shareText = "Prescription: ${prescription.title}\nFor: ${member?.fullName}\nDoctor: ${doctor?.name ?: "N/A"}\nDate: ${prescription.date}\nNotes: ${prescription.notes}"
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, shareText)
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Share Prescription"))
                                    }) {
                                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(onClick = { prescriptionToEdit = prescription }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(onClick = { prescriptionToDelete = prescription }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (doctor != null) "Doctor: Dr. ${doctor.name}" else "Doctor: Not specified",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Date: ${prescription.date}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            if (prescription.notes.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = prescription.notes,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (isAddDialogOpen || prescriptionToEdit != null) {
        PrescriptionFormDialog(
            initial = prescriptionToEdit,
            members = members,
            doctors = doctors,
            onDismiss = {
                isAddDialogOpen = false
                prescriptionToEdit = null
            },
            onSave = { saved ->
                viewModel.savePrescription(saved)
                isAddDialogOpen = false
                prescriptionToEdit = null
            }
        )
    }

    prescriptionToDelete?.let { pres ->
        ConfirmDeleteDialog(
            title = strings.confirmDelete,
            message = "Are you sure you want to delete this prescription?",
            onConfirm = { viewModel.deletePrescription(pres) },
            onDismiss = { prescriptionToDelete = null }
        )
    }
}

@Composable
fun PrescriptionFormDialog(
    initial: Prescription?,
    members: List<com.example.data.model.FamilyMember>,
    doctors: List<com.example.data.model.Doctor>,
    onDismiss: () -> Unit,
    onSave: (Prescription) -> Unit
) {
    val strings = LocalAppStrings.current
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var selectedMemberId by remember { mutableStateOf(initial?.familyMemberId ?: members.firstOrNull()?.id ?: 0L) }
    var selectedDoctorId by remember { mutableStateOf(initial?.doctorId) }
    var date by remember { mutableStateOf(initial?.date ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) strings.uploadPrescription else "Edit Prescription", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (errorMessage != null) {
                    Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; errorMessage = null },
                    label = { Text(strings.prescriptionTitle + " *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

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

                // Doctor
                if (doctors.isNotEmpty()) {
                    Text("Select Doctor", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedDoctorId == null,
                            onClick = { selectedDoctorId = null },
                            label = { Text("None") }
                        )
                        doctors.forEach { doc ->
                            FilterChip(
                                selected = selectedDoctorId == doc.id,
                                onClick = { selectedDoctorId = doc.id },
                                label = { Text(doc.name) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(strings.notes + " & Dosages") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (title.isBlank()) {
                    errorMessage = "Please enter prescription title."
                    return@Button
                }
                val pres = (initial ?: Prescription(title = title.trim(), familyMemberId = selectedMemberId)).copy(
                    title = title.trim(),
                    familyMemberId = selectedMemberId,
                    doctorId = selectedDoctorId,
                    date = date.trim(),
                    notes = notes.trim()
                )
                onSave(pres)
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
