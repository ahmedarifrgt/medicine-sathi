package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.FamilyMember
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.localization.LocalAppStrings
import com.example.ui.theme.MemberColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyDetailScreen(
    viewModel: MainViewModel,
    memberId: Long,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val strings = LocalAppStrings.current
    val members by viewModel.members.collectAsState()
    val medicines by viewModel.medicines.collectAsState()
    val history by viewModel.history.collectAsState()
    val appointments by viewModel.appointments.collectAsState()
    val prescriptions by viewModel.prescriptions.collectAsState()

    val member = members.find { it.id == memberId }
    var isEditDialogOpen by remember { mutableStateOf(false) }
    var isDeleteDialogOpen by remember { mutableStateOf(false) }

    if (member == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Family member not found.")
        }
        return
    }

    val memberMeds = medicines.filter { it.familyMemberId == memberId }
    val memberHistory = history.filter { it.familyMemberId == memberId }.take(5)
    val memberAppointments = appointments.filter { it.familyMemberId == memberId }
    val memberPrescriptions = prescriptions.filter { it.familyMemberId == memberId }
    val avatarColor = MemberColors[member.colorIndex % MemberColors.size]

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(member.fullName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { isEditDialogOpen = true }) {
                        Icon(Icons.Default.Edit, contentDescription = strings.edit)
                    }
                    IconButton(onClick = { isDeleteDialogOpen = true }) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = strings.delete, tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Profile Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(avatarColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = member.fullName.take(1).uppercase(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = avatarColor
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = member.fullName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        if (member.nickname.isNotEmpty()) {
                            Text(
                                text = "Nickname: ${member.nickname}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AssistChip(
                                onClick = {},
                                label = { Text(member.relationship) }
                            )
                            if (member.bloodGroup.isNotEmpty()) {
                                AssistChip(
                                    onClick = {},
                                    label = { Text("Blood: ${member.bloodGroup}") }
                                )
                            }
                            if (member.gender.isNotEmpty()) {
                                AssistChip(
                                    onClick = {},
                                    label = { Text(member.gender) }
                                )
                            }
                        }

                        if (member.phoneNumber.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Phone: ${member.phoneNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (member.dateOfBirth.isNotEmpty()) {
                            Text(
                                text = "DOB: ${member.dateOfBirth}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (member.notes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Notes: ${member.notes}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Active Medicines
            item {
                SectionHeader(title = "${strings.activeMedicines} (${memberMeds.size})")
            }

            if (memberMeds.isEmpty()) {
                item {
                    Text(
                        text = "No active medicines for this family member.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            } else {
                items(memberMeds) { med ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = med.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = "${med.dosage} ${med.unit} • ${med.beforeAfterFood}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${med.remainingQuantity} left",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Prescriptions
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(title = "${strings.prescriptions} (${memberPrescriptions.size})")
            }

            if (memberPrescriptions.isEmpty()) {
                item {
                    Text(
                        text = "No prescriptions stored for this member.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            } else {
                items(memberPrescriptions) { prescription ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = prescription.title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                Text(text = prescription.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // Appointments
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(title = "${strings.upcomingAppointments} (${memberAppointments.size})")
            }

            if (memberAppointments.isEmpty()) {
                item {
                    Text(
                        text = "No upcoming appointments.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            } else {
                items(memberAppointments) { apt ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = apt.doctorName.ifEmpty { "Doctor Visit" }, fontWeight = FontWeight.SemiBold)
                                Text(text = "${apt.appointmentDate} at ${apt.appointmentTime} • ${apt.hospitalClinic}", style = MaterialTheme.typography.bodySmall)
                            }
                            StatusBadge(status = apt.status)
                        }
                    }
                }
            }

            // Recent Dose History
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(title = "Recent Doses History")
            }

            if (memberHistory.isEmpty()) {
                item {
                    Text(
                        text = "No doses recorded yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            } else {
                items(memberHistory) { hist ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = hist.medicineName, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            Text(text = "${hist.scheduledDate} ${hist.scheduledTime}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        StatusBadge(status = hist.status)
                    }
                }
            }
        }
    }

    // Edit Member Dialog
    if (isEditDialogOpen) {
        MemberFormDialog(
            initialMember = member,
            onDismiss = { isEditDialogOpen = false },
            onSave = { updated ->
                viewModel.saveMember(updated)
                isEditDialogOpen = false
            }
        )
    }

    // Delete Member Dialog
    if (isDeleteDialogOpen) {
        ConfirmDeleteDialog(
            title = strings.confirmDelete,
            message = "Are you sure you want to delete profile of ${member.fullName}?",
            onConfirm = {
                viewModel.deleteMember(member)
                onBack()
            },
            onDismiss = { isDeleteDialogOpen = false }
        )
    }
}
