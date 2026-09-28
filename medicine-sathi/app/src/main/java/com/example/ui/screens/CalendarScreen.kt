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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.MainViewModel
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.localization.LocalAppStrings
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: MainViewModel
) {
    val strings = LocalAppStrings.current
    val selectedDate by viewModel.calendarSelectedDate.collectAsState()
    val medicines by viewModel.medicines.collectAsState()
    val appointments by viewModel.appointments.collectAsState()
    val history by viewModel.history.collectAsState()
    val members by viewModel.members.collectAsState()

    var viewMode by remember { mutableStateOf("Day") } // "Day", "Week", "Month"

    val sdfDisplay = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault())
    val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val sdfDay = SimpleDateFormat("d", Locale.getDefault())
    val sdfDayName = SimpleDateFormat("EEE", Locale.getDefault())

    // Generate current week dates centered around today or selected date
    val weekDates = remember(selectedDate) {
        val cal = Calendar.getInstance()
        try {
            val parsed = sdfDate.parse(selectedDate)
            if (parsed != null) cal.time = parsed
        } catch (_: Exception) {}

        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        val list = mutableListOf<String>()
        for (i in 0..6) {
            list.add(sdfDate.format(cal.time))
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        list
    }

    // Schedule for selected date
    val memberMap = members.associateBy { it.id }
    val dayAppointments = appointments.filter { it.appointmentDate == selectedDate }
    val dayHistory = history.filter { it.scheduledDate == selectedDate }
    val historyMap = dayHistory.associateBy { "${it.medicineId}_${it.scheduledTime}" }

    // Doses for selected date
    val dayDoses = remember(selectedDate, medicines, history) {
        val items = mutableListOf<CalendarDoseItem>()
        for (med in medicines) {
            val times = med.doseTimesJson.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val member = memberMap[med.familyMemberId]
            for (time in times) {
                val hist = historyMap["${med.id}_$time"]
                val status = hist?.status ?: "UPCOMING"
                items.add(
                    CalendarDoseItem(
                        medicineName = med.name,
                        memberName = member?.fullName ?: "General",
                        time = time,
                        status = status
                    )
                )
            }
        }
        items.sortedBy { it.time }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.calendar, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("calendar_screen")
        ) {
            // View Mode Selector (Day, Week, Month)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    listOf("Day", "Week", "Month").forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = viewMode == mode,
                            onClick = { viewMode = mode },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = 3)
                        ) {
                            Text(mode)
                        }
                    }
                }
            }

            // Date Navigation Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    val cal = Calendar.getInstance()
                    try {
                        val d = sdfDate.parse(selectedDate)
                        if (d != null) cal.time = d
                    } catch (_: Exception) {}
                    cal.add(Calendar.DAY_OF_MONTH, -1)
                    viewModel.setCalendarDate(sdfDate.format(cal.time))
                }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day")
                }

                Text(
                    text = try {
                        val d = sdfDate.parse(selectedDate)
                        if (d != null) sdfDisplay.format(d) else selectedDate
                    } catch (_: Exception) {
                        selectedDate
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = {
                    val cal = Calendar.getInstance()
                    try {
                        val d = sdfDate.parse(selectedDate)
                        if (d != null) cal.time = d
                    } catch (_: Exception) {}
                    cal.add(Calendar.DAY_OF_MONTH, 1)
                    viewModel.setCalendarDate(sdfDate.format(cal.time))
                }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Day")
                }
            }

            // Horizontal Week Days Strip
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(weekDates) { dateStr ->
                    val isSelected = dateStr == selectedDate
                    val cal = Calendar.getInstance()
                    try {
                        val d = sdfDate.parse(dateStr)
                        if (d != null) cal.time = d
                    } catch (_: Exception) {}

                    val dayNum = sdfDay.format(cal.time)
                    val dayName = sdfDayName.format(cal.time)

                    Card(
                        modifier = Modifier
                            .width(52.dp)
                            .height(68.dp)
                            .clickable { viewModel.setCalendarDate(dateStr) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = dayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = dayNum,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

            // Selected Day Schedule
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Appointments Section
                if (dayAppointments.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Appointments on this date")
                    }
                    items(dayAppointments) { apt ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = apt.doctorName.ifEmpty { "Doctor Appointment" }, fontWeight = FontWeight.Bold)
                                    Text(text = "${apt.appointmentTime} • ${apt.hospitalClinic}", style = MaterialTheme.typography.bodySmall)
                                }
                                StatusBadge(status = apt.status)
                            }
                        }
                    }
                }

                // Medicines Section
                item {
                    SectionHeader(title = "Medication Schedule (${dayDoses.size} doses)")
                }

                if (dayDoses.isEmpty()) {
                    item {
                        Text(
                            text = "No doses scheduled for this date.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
                        )
                    }
                } else {
                    items(dayDoses) { dose ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = dose.time,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(text = dose.medicineName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text(text = dose.memberName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                StatusBadge(status = dose.status)
                            }
                        }
                    }
                }
            }
        }
    }
}

data class CalendarDoseItem(
    val medicineName: String,
    val memberName: String,
    val time: String,
    val status: String
)
