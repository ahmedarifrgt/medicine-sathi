package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.SectionHeader
import com.example.ui.localization.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onNavigate: (Screen) -> Unit
) {
    BackHandler { onBack() }
    val strings = LocalAppStrings.current
    val context = LocalContext.current

    val language by viewModel.prefs.language.collectAsState()
    val themeMode by viewModel.prefs.themeMode.collectAsState()
    val privateNotifs by viewModel.prefs.privateNotifications.collectAsState()
    val medReminders by viewModel.prefs.medicineReminders.collectAsState()
    val aptReminders by viewModel.prefs.appointmentReminders.collectAsState()
    val expReminders by viewModel.prefs.expiryReminders.collectAsState()
    val soundEnabled by viewModel.prefs.soundEnabled.collectAsState()
    val vibrationEnabled by viewModel.prefs.vibrationEnabled.collectAsState()
    val currentPin by viewModel.prefs.appPin.collectAsState()

    var isClearDataDialogOpen by remember { mutableStateOf(false) }
    var isPinDialogOpen by remember { mutableStateOf(false) }
    var pinInputValue by remember { mutableStateOf("") }
    var showBackupDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.settings, fontWeight = FontWeight.Bold) },
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
                .testTag("settings_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Appearance & Language Section
            item {
                SectionHeader(title = "Preferences")
            }

            // Language Switcher
            item {
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
                            Text(text = strings.language, fontWeight = FontWeight.SemiBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = language == "en",
                                    onClick = { viewModel.prefs.setLanguage("en") },
                                    label = { Text("English") }
                                )
                                FilterChip(
                                    selected = language == "bn",
                                    onClick = { viewModel.prefs.setLanguage("bn") },
                                    label = { Text("বাংলা") }
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)

                        // Theme Mode
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = strings.appearance, fontWeight = FontWeight.SemiBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = themeMode == "LIGHT",
                                    onClick = { viewModel.prefs.setThemeMode("LIGHT") },
                                    label = { Text("Light") }
                                )
                                FilterChip(
                                    selected = themeMode == "DARK",
                                    onClick = { viewModel.prefs.setThemeMode("DARK") },
                                    label = { Text("Dark") }
                                )
                                FilterChip(
                                    selected = themeMode == "SYSTEM",
                                    onClick = { viewModel.prefs.setThemeMode("SYSTEM") },
                                    label = { Text("Auto") }
                                )
                            }
                        }
                    }
                }
            }

            // Notifications Section
            item {
                SectionHeader(title = "Notification Controls")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SettingSwitchRow(
                            title = "Medicine Dose Reminders",
                            subtitle = "Receive alarms for scheduled doses",
                            checked = medReminders,
                            onCheckedChange = { viewModel.prefs.setMedicineReminders(it) }
                        )
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        SettingSwitchRow(
                            title = "Appointment Reminders",
                            subtitle = "Receive notifications for doctor visits",
                            checked = aptReminders,
                            onCheckedChange = { viewModel.prefs.setAppointmentReminders(it) }
                        )
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        SettingSwitchRow(
                            title = "Expiry & Inventory Alerts",
                            subtitle = "Alert when medicines expire or run low",
                            checked = expReminders,
                            onCheckedChange = { viewModel.prefs.setExpiryReminders(it) }
                        )
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        SettingSwitchRow(
                            title = strings.privateNotification,
                            subtitle = strings.privateNotificationDesc,
                            checked = privateNotifs,
                            onCheckedChange = { viewModel.prefs.setPrivateNotifications(it) }
                        )
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        SettingSwitchRow(
                            title = "Sound & Vibration",
                            subtitle = "Play chime and vibrate for alarms",
                            checked = soundEnabled && vibrationEnabled,
                            onCheckedChange = {
                                viewModel.prefs.setSoundEnabled(it)
                                viewModel.prefs.setVibrationEnabled(it)
                            }
                        )
                    }
                }
            }

            // Security Section
            item {
                SectionHeader(title = "Security & Privacy")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    pinInputValue = ""
                                    isPinDialogOpen = true
                                },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "App Lock PIN", fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = if (currentPin.isNotEmpty()) "PIN is set (Active)" else "No PIN set (Disabled)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (currentPin.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(onClick = {
                                pinInputValue = ""
                                isPinDialogOpen = true
                            }) {
                                Text(if (currentPin.isNotEmpty()) "Change PIN" else "Set PIN")
                            }
                        }

                        if (currentPin.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = { viewModel.prefs.setAppPin("") },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Remove App Lock PIN")
                            }
                        }
                    }
                }
            }

            // Data Management Section
            item {
                SectionHeader(title = "Data & Backup")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedButton(
                            onClick = {
                                val json = viewModel.exportBackupJson()
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, json)
                                    type = "application/json"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Backup Medicine Sathi Data"))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Backup Data (JSON Export)")
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = { isClearDataDialogOpen = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.clearData)
                        }
                    }
                }
            }

            // About Author Section Link
            item {
                SectionHeader(title = "About")
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(Screen.ABOUT_AUTHOR) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = strings.aboutAuthor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(text = "MD ARIF • Southern University Bangladesh", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Medicine Sathi v1.0.0",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Designed for family medication management & health safety.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }

    // Set PIN Dialog
    if (isPinDialogOpen) {
        AlertDialog(
            onDismissRequest = { isPinDialogOpen = false },
            title = { Text("Set 4-Digit PIN", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter a 4-digit numeric code to protect sensitive family health data:")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pinInputValue,
                        onValueChange = { if (it.length <= 4 && it.all { ch -> ch.isDigit() }) pinInputValue = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        label = { Text("PIN") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInputValue.length == 4) {
                            viewModel.prefs.setAppPin(pinInputValue)
                            isPinDialogOpen = false
                        }
                    },
                    enabled = pinInputValue.length == 4
                ) {
                    Text("Save PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { isPinDialogOpen = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Clear Data Confirmation Dialog
    if (isClearDataDialogOpen) {
        ConfirmDeleteDialog(
            title = "Reset All Data",
            message = "This will erase all family members, medicines, dose schedules, appointments, prescriptions, and history. Are you sure?",
            onConfirm = {
                viewModel.clearEntireDatabase {
                    onNavigate(Screen.HOME)
                }
            },
            onDismiss = { isClearDataDialogOpen = false }
        )
    }
}

@Composable
fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
