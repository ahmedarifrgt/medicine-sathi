package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.localization.LocalAppStrings
import com.example.ui.localization.appStrings
import com.example.ui.screens.*
import com.example.ui.theme.MedicineSathiTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isOnboardingCompleted by viewModel.prefs.isOnboardingCompleted.collectAsState()
            val themeMode by viewModel.prefs.themeMode.collectAsState()
            val language by viewModel.prefs.language.collectAsState()
            val appPin by viewModel.prefs.appPin.collectAsState()

            var isUnlocked by remember { mutableStateOf(false) }

            val darkTheme = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            val currentStrings = appStrings(language)

            CompositionLocalProvider(LocalAppStrings provides currentStrings) {
                MedicineSathiTheme(darkTheme = darkTheme) {
                    when {
                        // 1. First-run Onboarding
                        !isOnboardingCompleted -> {
                            OnboardingScreen(
                                onFinished = {
                                    viewModel.prefs.setOnboardingCompleted(true)
                                }
                            )
                        }

                        // 2. PIN Lock enforcement
                        appPin.isNotEmpty() && !isUnlocked -> {
                            LockScreen(
                                correctPin = appPin,
                                onUnlocked = { isUnlocked = true }
                            )
                        }

                        // 3. Main App UI
                        else -> {
                            MainContent(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainContent(viewModel: MainViewModel) {
    val strings = LocalAppStrings.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val selectedMemberId by viewModel.selectedMemberId.collectAsState()
    val members by viewModel.members.collectAsState()
    val doctors by viewModel.doctors.collectAsState()

    // Dialog flags for home quick actions
    var showAddMedicineDialog by remember { mutableStateOf(false) }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var showAddDoctorDialog by remember { mutableStateOf(false) }
    var showAddAppointmentDialog by remember { mutableStateOf(false) }
    var showAddPrescriptionDialog by remember { mutableStateOf(false) }

    // Bottom Navigation Bar is shown on primary tabs
    val isPrimaryTab = currentScreen in listOf(Screen.HOME, Screen.MEDICINES, Screen.CALENDAR, Screen.FAMILY, Screen.MORE)

    // BackHandler for secondary screens
    if (!isPrimaryTab) {
        BackHandler {
            when (currentScreen) {
                Screen.ABOUT_AUTHOR -> viewModel.navigateTo(Screen.MORE)
                Screen.SETTINGS -> viewModel.navigateTo(Screen.MORE)
                Screen.FAMILY_DETAIL -> viewModel.navigateTo(Screen.FAMILY)
                else -> viewModel.navigateTo(Screen.HOME)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (isPrimaryTab) {
                NavigationBar(
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = currentScreen == Screen.HOME,
                        onClick = { viewModel.navigateTo(Screen.HOME) },
                        icon = { Icon(Icons.Default.Home, contentDescription = strings.home) },
                        label = { Text(strings.home) },
                        modifier = Modifier.testTag("tab_home")
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.MEDICINES,
                        onClick = { viewModel.navigateTo(Screen.MEDICINES) },
                        icon = { Icon(Icons.Default.Medication, contentDescription = strings.medicines) },
                        label = { Text(strings.medicines) },
                        modifier = Modifier.testTag("tab_medicines")
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.CALENDAR,
                        onClick = { viewModel.navigateTo(Screen.CALENDAR) },
                        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = strings.calendar) },
                        label = { Text(strings.calendar) },
                        modifier = Modifier.testTag("tab_calendar")
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.FAMILY,
                        onClick = { viewModel.navigateTo(Screen.FAMILY) },
                        icon = { Icon(Icons.Default.FamilyRestroom, contentDescription = strings.family) },
                        label = { Text(strings.family) },
                        modifier = Modifier.testTag("tab_family")
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.MORE,
                        onClick = { viewModel.navigateTo(Screen.MORE) },
                        icon = { Icon(Icons.Default.Menu, contentDescription = strings.more) },
                        label = { Text(strings.more) },
                        modifier = Modifier.testTag("tab_more")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.HOME -> HomeScreen(
                    viewModel = viewModel,
                    onNavigate = { viewModel.navigateTo(it) },
                    onAddMedicineClick = { showAddMedicineDialog = true },
                    onAddMemberClick = { showAddMemberDialog = true },
                    onAddDoctorClick = { showAddDoctorDialog = true },
                    onAddAppointmentClick = { showAddAppointmentDialog = true },
                    onAddPrescriptionClick = { showAddPrescriptionDialog = true }
                )
                Screen.MEDICINES -> MedicinesScreen(
                    viewModel = viewModel,
                    isAddDialogOpen = showAddMedicineDialog,
                    onCloseAddDialog = { showAddMedicineDialog = false },
                    onOpenAddDialog = { showAddMedicineDialog = true }
                )
                Screen.CALENDAR -> CalendarScreen(
                    viewModel = viewModel
                )
                Screen.FAMILY -> FamilyScreen(
                    viewModel = viewModel,
                    isAddDialogOpen = showAddMemberDialog,
                    onCloseAddDialog = { showAddMemberDialog = false },
                    onOpenAddDialog = { showAddMemberDialog = true },
                    onMemberClick = { memId -> viewModel.openFamilyDetail(memId) }
                )
                Screen.FAMILY_DETAIL -> selectedMemberId?.let { id ->
                    FamilyDetailScreen(
                        viewModel = viewModel,
                        memberId = id,
                        onBack = { viewModel.navigateTo(Screen.FAMILY) }
                    )
                }
                Screen.MORE -> MoreScreen(
                    onNavigate = { viewModel.navigateTo(it) }
                )
                Screen.APPOINTMENTS -> AppointmentsScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(Screen.MORE) }
                )
                Screen.DOCTORS -> DoctorsScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(Screen.MORE) }
                )
                Screen.PRESCRIPTIONS -> PrescriptionsScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(Screen.MORE) }
                )
                Screen.CAREGIVERS -> CaregiversScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(Screen.MORE) }
                )
                Screen.INVENTORY -> InventoryScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(Screen.MORE) }
                )
                Screen.HISTORY -> HistoryScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(Screen.MORE) }
                )
                Screen.REPORTS -> ReportsScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(Screen.MORE) }
                )
                Screen.NOTIFICATIONS -> NotificationsScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(Screen.HOME) }
                )
                Screen.SEARCH -> SearchScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(Screen.HOME) },
                    onNavigate = { viewModel.navigateTo(it) }
                )
                Screen.SETTINGS -> SettingsScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(Screen.MORE) },
                    onNavigate = { viewModel.navigateTo(it) }
                )
                Screen.ABOUT_AUTHOR -> AboutAuthorScreen(
                    onBack = { viewModel.navigateTo(Screen.MORE) }
                )
            }
        }
    }

    // Modal dialogs triggered from Home quick actions
    if (showAddMedicineDialog && currentScreen != Screen.MEDICINES) {
        MedicineFormDialog(
            initialMedicine = null,
            members = members,
            doctors = doctors,
            onDismiss = { showAddMedicineDialog = false },
            onSave = { med ->
                viewModel.saveMedicine(med)
                showAddMedicineDialog = false
            }
        )
    }

    if (showAddMemberDialog && currentScreen != Screen.FAMILY) {
        MemberFormDialog(
            initialMember = null,
            onDismiss = { showAddMemberDialog = false },
            onSave = { mem ->
                viewModel.saveMember(mem)
                showAddMemberDialog = false
            }
        )
    }

    if (showAddDoctorDialog) {
        DoctorFormDialog(
            initial = null,
            onDismiss = { showAddDoctorDialog = false },
            onSave = { doc ->
                viewModel.saveDoctor(doc)
                showAddDoctorDialog = false
            }
        )
    }

    if (showAddAppointmentDialog) {
        AppointmentFormDialog(
            initial = null,
            members = members,
            doctors = doctors,
            onDismiss = { showAddAppointmentDialog = false },
            onSave = { apt ->
                viewModel.saveAppointment(apt)
                showAddAppointmentDialog = false
            }
        )
    }

    if (showAddPrescriptionDialog) {
        PrescriptionFormDialog(
            initial = null,
            members = members,
            doctors = doctors,
            onDismiss = { showAddPrescriptionDialog = false },
            onSave = { pres ->
                viewModel.savePrescription(pres)
                showAddPrescriptionDialog = false
            }
        )
    }
}
