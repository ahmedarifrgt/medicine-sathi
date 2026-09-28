package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.example.ui.MainViewModel
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.localization.LocalAppStrings
import com.example.ui.theme.MemberColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyScreen(
    viewModel: MainViewModel,
    isAddDialogOpen: Boolean,
    onCloseAddDialog: () -> Unit,
    onOpenAddDialog: () -> Unit,
    onMemberClick: (Long) -> Unit
) {
    val strings = LocalAppStrings.current
    val members by viewModel.members.collectAsState()
    val medicines by viewModel.medicines.collectAsState()

    var memberToEdit by remember { mutableStateOf<FamilyMember?>(null) }
    var memberToDelete by remember { mutableStateOf<FamilyMember?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.family, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenAddDialog,
                modifier = Modifier.testTag("fab_add_member"),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = strings.addFamilyMember, tint = Color.White)
            }
        }
    ) { innerPadding ->
        if (members.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.FamilyRestroom,
                title = strings.noFamilyMembersTitle,
                description = "Add family members like Father, Mother, or Children to track their doses separately.",
                actionButtonText = strings.addFamilyMember,
                onActionClick = onOpenAddDialog,
                testTag = "family_empty_view",
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
                items(members, key = { it.id }) { member ->
                    val memberMedsCount = medicines.count { it.familyMemberId == member.id }
                    val avatarColor = MemberColors[member.colorIndex % MemberColors.size]

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onMemberClick(member.id) }
                            .testTag("family_member_card_${member.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(avatarColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = member.fullName.take(1).uppercase(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = avatarColor
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = member.fullName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (member.nickname.isNotEmpty()) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "(${member.nickname})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                    ) {
                                        Text(
                                            text = member.relationship,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    if (member.bloodGroup.isNotEmpty()) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color.Red.copy(alpha = 0.1f)
                                        ) {
                                            Text(
                                                text = member.bloodGroup,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.Red,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "$memberMedsCount active medicines",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
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
        }
    }

    // Add / Edit Member Dialog
    if (isAddDialogOpen || memberToEdit != null) {
        MemberFormDialog(
            initialMember = memberToEdit,
            onDismiss = {
                onCloseAddDialog()
                memberToEdit = null
            },
            onSave = { mem ->
                viewModel.saveMember(mem)
                onCloseAddDialog()
                memberToEdit = null
            }
        )
    }

    // Delete Member Dialog
    memberToDelete?.let { mem ->
        ConfirmDeleteDialog(
            title = strings.confirmDelete,
            message = "Are you sure you want to delete ${mem.fullName}? All associated medicines and history will remain in archive.",
            onConfirm = { viewModel.deleteMember(mem) },
            onDismiss = { memberToDelete = null }
        )
    }
}

@Composable
fun MemberFormDialog(
    initialMember: FamilyMember?,
    onDismiss: () -> Unit,
    onSave: (FamilyMember) -> Unit
) {
    val strings = LocalAppStrings.current

    var fullName by remember { mutableStateOf(initialMember?.fullName ?: "") }
    var nickname by remember { mutableStateOf(initialMember?.nickname ?: "") }
    var dateOfBirth by remember { mutableStateOf(initialMember?.dateOfBirth ?: "") }
    var gender by remember { mutableStateOf(initialMember?.gender ?: "Male") }
    var bloodGroup by remember { mutableStateOf(initialMember?.bloodGroup ?: "O+") }
    var phoneNumber by remember { mutableStateOf(initialMember?.phoneNumber ?: "") }
    var relationship by remember { mutableStateOf(initialMember?.relationship ?: "Mother") }
    var colorIndex by remember { mutableStateOf(initialMember?.colorIndex ?: 0) }
    var notes by remember { mutableStateOf(initialMember?.notes ?: "") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val relationships = listOf("Self", "Father", "Mother", "Brother", "Sister", "Son", "Daughter", "Spouse", "Grandparent", "Other")
    val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    val genders = listOf("Male", "Female", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialMember == null) strings.addFamilyMember else "Edit Profile",
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

                // Full Name
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it; errorMessage = null },
                    label = { Text(strings.fullName + " *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_fullname_input")
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Nickname
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text(strings.nickname) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Relationship chips
                Text(strings.relationship, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                LazyRow(
                    modifier = Modifier.padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(relationships) { rel ->
                        FilterChip(
                            selected = relationship == rel,
                            onClick = { relationship = rel },
                            label = { Text(rel) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Gender
                Text(strings.gender, style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    genders.forEach { g ->
                        FilterChip(
                            selected = gender == g,
                            onClick = { gender = g },
                            label = { Text(g) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Blood Group
                Text(strings.bloodGroup, style = MaterialTheme.typography.labelMedium)
                LazyRow(
                    modifier = Modifier.padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(bloodGroups) { bg ->
                        FilterChip(
                            selected = bloodGroup == bg,
                            onClick = { bloodGroup = bg },
                            label = { Text(bg) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Phone number (Bangladesh format friendly e.g. 018...)
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text(strings.phone + " (e.g. 018XXXXXXXX)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Date of Birth
                OutlinedTextField(
                    value = dateOfBirth,
                    onValueChange = { dateOfBirth = it },
                    label = { Text(strings.dob + " (YYYY-MM-DD)") },
                    singleLine = true,
                    placeholder = { Text("1985-05-15") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Profile Color
                Text("Profile Avatar Color", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MemberColors.forEachIndexed { idx, color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { colorIndex = idx },
                            contentAlignment = Alignment.Center
                        ) {
                            if (colorIndex == idx) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Notes
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
            Button(
                onClick = {
                    if (fullName.isBlank()) {
                        errorMessage = "Please enter a full name."
                        return@Button
                    }
                    val mem = (initialMember ?: FamilyMember(fullName = fullName.trim())).copy(
                        fullName = fullName.trim(),
                        nickname = nickname.trim(),
                        dateOfBirth = dateOfBirth.trim(),
                        gender = gender,
                        bloodGroup = bloodGroup,
                        phoneNumber = phoneNumber.trim(),
                        relationship = relationship,
                        colorIndex = colorIndex,
                        notes = notes.trim()
                    )
                    onSave(mem)
                },
                modifier = Modifier.testTag("member_save_button")
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
