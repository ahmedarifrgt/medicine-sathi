package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.FamilyMember
import com.example.data.model.Medicine
import com.example.ui.MainViewModel
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.QuantityUpdateDialog
import com.example.ui.components.StatusBadge
import com.example.ui.localization.LocalAppStrings
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicinesScreen(
    viewModel: MainViewModel,
    isAddDialogOpen: Boolean,
    onCloseAddDialog: () -> Unit,
    onOpenAddDialog: () -> Unit
) {
    val strings = LocalAppStrings.current
    val medicines by viewModel.medicines.collectAsState()
    val members by viewModel.members.collectAsState()
    val doctors by viewModel.doctors.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf("All") }
    var selectedMemberFilter by remember { mutableStateOf<Long?>(null) }

    var medicineToEdit by remember { mutableStateOf<Medicine?>(null) }
    var medicineToDelete by remember { mutableStateOf<Medicine?>(null) }
    var medicineToUpdateStock by remember { mutableStateOf<Medicine?>(null) }

    val medicineTypes = listOf("All", "Tablet", "Capsule", "Syrup", "Injection", "Cream", "Drops", "Inhaler", "Other")

    val filteredMedicines = medicines.filter { med ->
        val matchesSearch = med.name.contains(searchQuery, ignoreCase = true) ||
                med.genericName.contains(searchQuery, ignoreCase = true)
        val matchesType = selectedTypeFilter == "All" || med.medicineType.equals(selectedTypeFilter, ignoreCase = true)
        val matchesMember = selectedMemberFilter == null || med.familyMemberId == selectedMemberFilter
        matchesSearch && matchesType && matchesMember
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.medicines, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenAddDialog,
                modifier = Modifier.testTag("fab_add_medicine"),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = strings.addMedicine, tint = Color.White)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("medicines_search_input"),
                placeholder = { Text(strings.searchPlaceholder) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Type Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(medicineTypes) { type ->
                    FilterChip(
                        selected = selectedTypeFilter == type,
                        onClick = { selectedTypeFilter = type },
                        label = { Text(type) }
                    )
                }
            }

            if (medicines.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Medication,
                    title = strings.noMedicinesTitle,
                    description = "Keep all your prescriptions and dose schedules neatly tracked.",
                    actionButtonText = strings.addMedicine,
                    onActionClick = onOpenAddDialog,
                    testTag = "medicines_empty_view",
                    modifier = Modifier.weight(1f)
                )
            } else if (filteredMedicines.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.noResultsFound,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredMedicines, key = { it.id }) { med ->
                        val member = members.find { it.id == med.familyMemberId }
                        MedicineCard(
                            medicine = med,
                            memberName = member?.fullName ?: "General",
                            onEdit = { medicineToEdit = med },
                            onDelete = { medicineToDelete = med },
                            onUpdateStock = { medicineToUpdateStock = med }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Dialog
    if (isAddDialogOpen || medicineToEdit != null) {
        MedicineFormDialog(
            initialMedicine = medicineToEdit,
            members = members,
            doctors = doctors,
            onDismiss = {
                onCloseAddDialog()
                medicineToEdit = null
            },
            onSave = { med ->
                viewModel.saveMedicine(med)
                onCloseAddDialog()
                medicineToEdit = null
            }
        )
    }

    // Delete Confirmation
    medicineToDelete?.let { med ->
        ConfirmDeleteDialog(
            title = strings.confirmDelete,
            message = "Are you sure you want to remove ${med.name}? This will also delete related reminders.",
            onConfirm = { viewModel.deleteMedicine(med) },
            onDismiss = { medicineToDelete = null }
        )
    }

    // Stock update dialog
    medicineToUpdateStock?.let { med ->
        QuantityUpdateDialog(
            medicineName = med.name,
            currentQuantity = med.remainingQuantity,
            unit = med.unit,
            onConfirm = { newQty ->
                viewModel.updateMedicineQuantity(med.id, newQty)
            },
            onDismiss = { medicineToUpdateStock = null }
        )
    }
}

@Composable
fun MedicineCard(
    medicine: Medicine,
    memberName: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onUpdateStock: () -> Unit
) {
    val strings = LocalAppStrings.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("medicine_card_${medicine.id}"),
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
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = medicine.name,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (medicine.genericName.isNotEmpty()) {
                            Text(
                                text = medicine.genericName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = strings.edit, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = strings.delete, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Details row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text("Type / Dose", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${medicine.medicineType} • ${medicine.dosage} ${medicine.unit}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
                Column {
                    Text("Family Member", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(memberName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
                Column {
                    Text("Stock", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${medicine.remainingQuantity} ${medicine.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (medicine.remainingQuantity <= medicine.minQuantityThreshold) StatusWarning else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dose times chips
            val times = medicine.doseTimesJson.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            if (times.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Times: ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    times.forEach { t ->
                        Box(
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = t, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Expiry date badge if set
            if (medicine.expiryDate.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EventBusy, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Expiry: ${medicine.expiryDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Update stock button
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onUpdateStock,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Update Stock / Quantity", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun MedicineFormDialog(
    initialMedicine: Medicine?,
    members: List<FamilyMember>,
    doctors: List<com.example.data.model.Doctor>,
    onDismiss: () -> Unit,
    onSave: (Medicine) -> Unit
) {
    val strings = LocalAppStrings.current

    var name by remember { mutableStateOf(initialMedicine?.name ?: "") }
    var genericName by remember { mutableStateOf(initialMedicine?.genericName ?: "") }
    var medicineType by remember { mutableStateOf(initialMedicine?.medicineType ?: "Tablet") }
    var strength by remember { mutableStateOf(initialMedicine?.strength ?: "") }
    var unit by remember { mutableStateOf(initialMedicine?.unit ?: "Tablet") }
    var dosage by remember { mutableStateOf(initialMedicine?.dosage ?: "1") }
    var initialQuantity by remember { mutableStateOf(initialMedicine?.initialQuantity?.toString() ?: "30") }
    var remainingQuantity by remember { mutableStateOf(initialMedicine?.remainingQuantity?.toString() ?: "30") }
    var minThreshold by remember { mutableStateOf(initialMedicine?.minQuantityThreshold?.toString() ?: "5") }
    var startDate by remember { mutableStateOf(initialMedicine?.startDate ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var endDate by remember { mutableStateOf(initialMedicine?.endDate ?: "") }
    var frequency by remember { mutableStateOf(initialMedicine?.frequency ?: "Once daily") }
    var doseTimesList by remember {
        mutableStateOf(
            initialMedicine?.doseTimesJson?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
                ?: listOf("08:00")
        )
    }
    var beforeAfterFood by remember { mutableStateOf(initialMedicine?.beforeAfterFood ?: "After food") }
    var selectedMemberId by remember {
        mutableStateOf(initialMedicine?.familyMemberId ?: members.firstOrNull()?.id ?: 0L)
    }
    var selectedDoctorId by remember { mutableStateOf(initialMedicine?.doctorId) }
    var expiryDate by remember { mutableStateOf(initialMedicine?.expiryDate ?: "") }
    var notes by remember { mutableStateOf(initialMedicine?.notes ?: "") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialMedicine == null) strings.addMedicine else "Edit Medicine",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Family Member Dropdown / Selector
                Text(strings.selectFamilyMember, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                if (members.isEmpty()) {
                    Text(
                        "No family members created yet. Medicine will be assigned to General profile.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    LazyRow(
                        modifier = Modifier.padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(members) { mem ->
                            FilterChip(
                                selected = selectedMemberId == mem.id,
                                onClick = { selectedMemberId = mem.id },
                                label = { Text(mem.fullName) }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                // Medicine Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMessage = null },
                    label = { Text(strings.medicineName + " *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("medicine_name_input")
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Generic Name
                OutlinedTextField(
                    value = genericName,
                    onValueChange = { genericName = it },
                    label = { Text(strings.genericName) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Medicine Type
                Text(strings.medicineType, style = MaterialTheme.typography.labelMedium)
                LazyRow(
                    modifier = Modifier.padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(listOf("Tablet", "Capsule", "Syrup", "Injection", "Cream", "Drops", "Inhaler", "Other")) { t ->
                        FilterChip(
                            selected = medicineType == t,
                            onClick = { medicineType = t },
                            label = { Text(t) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Dosage & Strength
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        label = { Text("Dose (e.g. 1)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit (e.g. Tablet)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Meal Timing
                Text(strings.beforeAfterFood, style = MaterialTheme.typography.labelMedium)
                LazyRow(
                    modifier = Modifier.padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(listOf("Before food", "With food", "After food", "No preference")) { timing ->
                        FilterChip(
                            selected = beforeAfterFood == timing,
                            onClick = { beforeAfterFood = timing },
                            label = { Text(timing) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Dose Times
                Text(strings.doseTimes, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                doseTimesList.forEachIndexed { index, timeStr ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = timeStr,
                            onValueChange = { newTime ->
                                val list = doseTimesList.toMutableList()
                                list[index] = newTime
                                doseTimesList = list
                            },
                            label = { Text("Dose ${index + 1} (HH:mm)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        if (doseTimesList.size > 1) {
                            IconButton(
                                onClick = {
                                    val list = doseTimesList.toMutableList()
                                    list.removeAt(index)
                                    doseTimesList = list
                                }
                            ) {
                                Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Remove")
                            }
                        }
                    }
                }
                TextButton(
                    onClick = {
                        doseTimesList = doseTimesList + "12:00"
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.addDoseTime)
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Quantity & Threshold
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = remainingQuantity,
                        onValueChange = { remainingQuantity = it.filter { ch -> ch.isDigit() } },
                        label = { Text(strings.remainingQuantity) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minThreshold,
                        onValueChange = { minThreshold = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Low Stock Alert") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Expiry Date (YYYY-MM-DD)
                OutlinedTextField(
                    value = expiryDate,
                    onValueChange = { expiryDate = it },
                    label = { Text(strings.expiryDate + " (YYYY-MM-DD)") },
                    placeholder = { Text("2026-12-31") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Doctor dropdown
                if (doctors.isNotEmpty()) {
                    Text(strings.prescribedByDoctor, style = MaterialTheme.typography.labelMedium)
                    LazyRow(
                        modifier = Modifier.padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedDoctorId == null,
                                onClick = { selectedDoctorId = null },
                                label = { Text("None") }
                            )
                        }
                        items(doctors) { doc ->
                            FilterChip(
                                selected = selectedDoctorId == doc.id,
                                onClick = { selectedDoctorId = doc.id },
                                label = { Text(doc.name) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(strings.medicineNotes) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Please enter a medicine name."
                        return@Button
                    }
                    val remQty = remainingQuantity.toIntOrNull() ?: 30
                    val initQty = initialQuantity.toIntOrNull() ?: remQty
                    val minT = minThreshold.toIntOrNull() ?: 5

                    val medicine = (initialMedicine ?: Medicine(
                        name = name.trim(),
                        familyMemberId = selectedMemberId
                    )).copy(
                        name = name.trim(),
                        genericName = genericName.trim(),
                        medicineType = medicineType,
                        strength = strength.trim(),
                        unit = unit.trim(),
                        dosage = dosage.trim(),
                        initialQuantity = initQty,
                        remainingQuantity = remQty,
                        minQuantityThreshold = minT,
                        startDate = startDate,
                        endDate = endDate,
                        frequency = frequency,
                        doseTimesJson = doseTimesList.joinToString(","),
                        beforeAfterFood = beforeAfterFood,
                        familyMemberId = selectedMemberId,
                        doctorId = selectedDoctorId,
                        expiryDate = expiryDate.trim(),
                        notes = notes.trim()
                    )
                    onSave(medicine)
                },
                modifier = Modifier.testTag("medicine_save_button")
            ) {
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
