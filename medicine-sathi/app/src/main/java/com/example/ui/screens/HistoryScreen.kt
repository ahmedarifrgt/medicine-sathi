package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.MainViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.localization.LocalAppStrings
import com.example.ui.theme.OceanSecondaryLight
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val strings = LocalAppStrings.current
    val history by viewModel.history.collectAsState()
    val members by viewModel.members.collectAsState()

    var selectedStatusFilter by remember { mutableStateOf("All") }
    var selectedMemberId by remember { mutableStateOf<Long?>(null) }

    val filteredHistory = history.filter { h ->
        val matchesStatus = selectedStatusFilter == "All" || h.status.equals(selectedStatusFilter, ignoreCase = true)
        val matchesMember = selectedMemberId == null || h.familyMemberId == selectedMemberId
        matchesStatus && matchesMember
    }

    val totalDoses = history.size
    val takenDoses = history.count { it.status.equals("TAKEN", ignoreCase = true) }
    val missedDoses = history.count { it.status.equals("MISSED", ignoreCase = true) }
    val skippedDoses = history.count { it.status.equals("SKIPPED", ignoreCase = true) }
    val adherencePercent = if (totalDoses > 0) ((takenDoses.toDouble() / totalDoses) * 100).toInt() else 0

    val sdfTime = SimpleDateFormat("h:mm a", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.history, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("history_screen")
        ) {
            // Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Adherence",
                    value = "$adherencePercent%",
                    icon = Icons.Default.History,
                    containerColor = StatusSuccess.copy(alpha = 0.12f),
                    contentColor = StatusSuccess,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Taken",
                    value = takenDoses.toString(),
                    icon = Icons.Default.History,
                    containerColor = OceanSecondaryLight.copy(alpha = 0.12f),
                    contentColor = OceanSecondaryLight,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Missed",
                    value = missedDoses.toString(),
                    icon = Icons.Default.History,
                    containerColor = StatusDanger.copy(alpha = 0.12f),
                    contentColor = StatusDanger,
                    modifier = Modifier.weight(1f)
                )
            }

            // Filters
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listOf("All", "TAKEN", "MISSED", "SKIPPED", "SNOOZED")) { st ->
                    FilterChip(
                        selected = selectedStatusFilter == st,
                        onClick = { selectedStatusFilter = st },
                        label = { Text(if (st == "All") "All Status" else st) }
                    )
                }
            }

            if (history.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.History,
                    title = strings.noHistoryTitle,
                    description = "As you mark doses as taken or skipped from notifications and the dashboard, your timeline will appear here.",
                    modifier = Modifier.weight(1f)
                )
            } else if (filteredHistory.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(strings.noResultsFound, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredHistory, key = { it.id }) { h ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = h.medicineName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = "Scheduled: ${h.scheduledDate} at ${h.scheduledTime}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (h.actionTime > 0) {
                                        Text(
                                            text = "Logged: ${sdfTime.format(Date(h.actionTime))}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                StatusBadge(status = h.status)
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "Note: This medication history is for personal tracking purposes and does not constitute a clinical or medical diagnosis.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
