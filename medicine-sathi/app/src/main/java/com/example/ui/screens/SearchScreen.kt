package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.SectionHeader
import com.example.ui.localization.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onNavigate: (Screen) -> Unit
) {
    BackHandler { onBack() }
    val strings = LocalAppStrings.current
    val query by viewModel.searchQuery.collectAsState()

    val medicines by viewModel.medicines.collectAsState()
    val members by viewModel.members.collectAsState()
    val doctors by viewModel.doctors.collectAsState()
    val appointments by viewModel.appointments.collectAsState()
    val prescriptions by viewModel.prescriptions.collectAsState()

    val filteredMeds = remember(query, medicines) {
        if (query.isBlank()) emptyList()
        else medicines.filter { it.name.contains(query, ignoreCase = true) || it.genericName.contains(query, ignoreCase = true) }
    }

    val filteredMembers = remember(query, members) {
        if (query.isBlank()) emptyList()
        else members.filter { it.fullName.contains(query, ignoreCase = true) || it.nickname.contains(query, ignoreCase = true) }
    }

    val filteredDoctors = remember(query, doctors) {
        if (query.isBlank()) emptyList()
        else doctors.filter { it.name.contains(query, ignoreCase = true) || it.specialty.contains(query, ignoreCase = true) || it.hospitalClinic.contains(query, ignoreCase = true) }
    }

    val filteredAppointments = remember(query, appointments) {
        if (query.isBlank()) emptyList()
        else appointments.filter { it.doctorName.contains(query, ignoreCase = true) || it.hospitalClinic.contains(query, ignoreCase = true) || it.reason.contains(query, ignoreCase = true) }
    }

    val filteredPrescriptions = remember(query, prescriptions) {
        if (query.isBlank()) emptyList()
        else prescriptions.filter { it.title.contains(query, ignoreCase = true) || it.notes.contains(query, ignoreCase = true) }
    }

    val totalMatches = filteredMeds.size + filteredMembers.size + filteredDoctors.size + filteredAppointments.size + filteredPrescriptions.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text(strings.searchPlaceholder) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("global_search_textfield"),
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        if (query.isBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Type to search medicines, doctors, family members, appointments, and prescriptions.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(32.dp)
                )
            }
        } else if (totalMatches == 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = strings.noResultsFound,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Medicines Matches
                if (filteredMeds.isNotEmpty()) {
                    item { SectionHeader(title = "Medicines (${filteredMeds.size})") }
                    items(filteredMeds) { med ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigate(Screen.MEDICINES) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Medication, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = med.name, fontWeight = FontWeight.Bold)
                                    Text(text = "${med.medicineType} • ${med.dosage} ${med.unit}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                // Family Members Matches
                if (filteredMembers.isNotEmpty()) {
                    item { SectionHeader(title = "Family Members (${filteredMembers.size})") }
                    items(filteredMembers) { mem ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.openFamilyDetail(mem.id) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = mem.fullName, fontWeight = FontWeight.Bold)
                                    Text(text = mem.relationship, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                // Doctors Matches
                if (filteredDoctors.isNotEmpty()) {
                    item { SectionHeader(title = "Doctors (${filteredDoctors.size})") }
                    items(filteredDoctors) { doc ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigate(Screen.DOCTORS) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LocalHospital, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = doc.name, fontWeight = FontWeight.Bold)
                                    Text(text = "${doc.specialty} • ${doc.hospitalClinic}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                // Appointments Matches
                if (filteredAppointments.isNotEmpty()) {
                    item { SectionHeader(title = "Appointments (${filteredAppointments.size})") }
                    items(filteredAppointments) { apt ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigate(Screen.APPOINTMENTS) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = apt.doctorName, fontWeight = FontWeight.Bold)
                                    Text(text = "${apt.appointmentDate} at ${apt.appointmentTime}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                // Prescriptions Matches
                if (filteredPrescriptions.isNotEmpty()) {
                    item { SectionHeader(title = "Prescriptions (${filteredPrescriptions.size})") }
                    items(filteredPrescriptions) { pres ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigate(Screen.PRESCRIPTIONS) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = pres.title, fontWeight = FontWeight.Bold)
                                    Text(text = pres.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
