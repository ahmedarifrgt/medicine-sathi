package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.Caregiver
import com.example.ui.MainViewModel
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.localization.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiversScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val caregivers by viewModel.caregivers.collectAsState()

    var isAddDialogOpen by remember { mutableStateOf(false) }
    var caregiverToEdit by remember { mutableStateOf<Caregiver?>(null) }
    var caregiverToDelete by remember { mutableStateOf<Caregiver?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.caregivers, fontWeight = FontWeight.Bold) },
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
                modifier = Modifier.testTag("fab_add_caregiver"),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Caregiver", tint = Color.White)
            }
        }
    ) { innerPadding ->
        if (caregivers.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.People,
                title = strings.noCaregiversTitle,
                description = "Add trusted caregivers who should be notified if a family member misses a dose or needs prescription refills.",
                actionButtonText = "Add Caregiver",
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
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = strings.caregiverAlertInfo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }

                items(caregivers, key = { it.id }) { cg ->
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
                                Column {
                                    Text(text = cg.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text(text = cg.relationship, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                }
                                Row {
                                    IconButton(onClick = { caregiverToEdit = cg }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(onClick = { caregiverToDelete = cg }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            if (cg.phoneNumber.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Phone: ${cg.phoneNumber}", style = MaterialTheme.typography.bodySmall)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Alert preferences
                            Text("Alert Preferences:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (cg.notifyMissed) AssistChip(onClick = {}, label = { Text("Missed Doses", style = MaterialTheme.typography.labelSmall) })
                                if (cg.notifyReminder) AssistChip(onClick = {}, label = { Text("Reminders", style = MaterialTheme.typography.labelSmall) })
                                if (cg.notifyExpiry) AssistChip(onClick = {}, label = { Text("Expiry", style = MaterialTheme.typography.labelSmall) })
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Quick Contact Actions
                            if (cg.phoneNumber.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${cg.phoneNumber}")))
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(strings.call)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${cg.phoneNumber}")))
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.Message, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(strings.message)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (isAddDialogOpen || caregiverToEdit != null) {
        CaregiverFormDialog(
            initial = caregiverToEdit,
            onDismiss = {
                isAddDialogOpen = false
                caregiverToEdit = null
            },
            onSave = { saved ->
                viewModel.saveCaregiver(saved)
                isAddDialogOpen = false
                caregiverToEdit = null
            }
        )
    }

    caregiverToDelete?.let { cg ->
        ConfirmDeleteDialog(
            title = strings.confirmDelete,
            message = "Are you sure you want to remove caregiver ${cg.name}?",
            onConfirm = { viewModel.deleteCaregiver(cg) },
            onDismiss = { caregiverToDelete = null }
        )
    }
}

@Composable
fun CaregiverFormDialog(
    initial: Caregiver?,
    onDismiss: () -> Unit,
    onSave: (Caregiver) -> Unit
) {
    val strings = LocalAppStrings.current
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var relationship by remember { mutableStateOf(initial?.relationship ?: "Family Friend") }
    var phoneNumber by remember { mutableStateOf(initial?.phoneNumber ?: "") }
    var email by remember { mutableStateOf(initial?.email ?: "") }
    var notifyMissed by remember { mutableStateOf(initial?.notifyMissed ?: true) }
    var notifyReminder by remember { mutableStateOf(initial?.notifyReminder ?: true) }
    var notifyAppointment by remember { mutableStateOf(initial?.notifyAppointment ?: true) }
    var notifyExpiry by remember { mutableStateOf(initial?.notifyExpiry ?: true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Add Caregiver" else "Edit Caregiver", fontWeight = FontWeight.Bold) },
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
                    value = name,
                    onValueChange = { name = it; errorMessage = null },
                    label = { Text(strings.caregiverName + " *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = relationship,
                    onValueChange = { relationship = it },
                    label = { Text(strings.relationship) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text(strings.phone) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(strings.email) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text("Notification Events:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = notifyMissed, onCheckedChange = { notifyMissed = it })
                    Text(strings.notifyMissed, style = MaterialTheme.typography.bodySmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = notifyReminder, onCheckedChange = { notifyReminder = it })
                    Text(strings.notifyReminder, style = MaterialTheme.typography.bodySmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = notifyAppointment, onCheckedChange = { notifyAppointment = it })
                    Text(strings.notifyAppointment, style = MaterialTheme.typography.bodySmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = notifyExpiry, onCheckedChange = { notifyExpiry = it })
                    Text(strings.notifyExpiry, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isBlank()) {
                    errorMessage = "Please enter caregiver name."
                    return@Button
                }
                val cg = (initial ?: Caregiver(name = name.trim())).copy(
                    name = name.trim(),
                    relationship = relationship.trim(),
                    phoneNumber = phoneNumber.trim(),
                    email = email.trim(),
                    notifyMissed = notifyMissed,
                    notifyReminder = notifyReminder,
                    notifyAppointment = notifyAppointment,
                    notifyExpiry = notifyExpiry
                )
                onSave(cg)
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
