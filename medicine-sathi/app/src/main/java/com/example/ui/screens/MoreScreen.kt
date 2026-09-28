package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.Screen
import com.example.ui.localization.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onNavigate: (Screen) -> Unit
) {
    val strings = LocalAppStrings.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.more, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("more_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Health Management Section
            item {
                Text(
                    text = "Health Management",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }

            item {
                MoreMenuCard(
                    icon = Icons.Default.Event,
                    title = "Appointments",
                    subtitle = "Manage doctor consultations & reminders",
                    iconTint = Color(0xFF0284C7),
                    onClick = { onNavigate(Screen.APPOINTMENTS) },
                    tag = "menu_appointments"
                )
            }

            item {
                MoreMenuCard(
                    icon = Icons.Default.LocalHospital,
                    title = strings.doctors,
                    subtitle = "Doctor contact directory & chamber address",
                    iconTint = Color(0xFF0D9488),
                    onClick = { onNavigate(Screen.DOCTORS) },
                    tag = "menu_doctors"
                )
            }

            item {
                MoreMenuCard(
                    icon = Icons.Default.Description,
                    title = strings.prescriptions,
                    subtitle = "Safely store and categorize prescriptions",
                    iconTint = Color(0xFF7C3AED),
                    onClick = { onNavigate(Screen.PRESCRIPTIONS) },
                    tag = "menu_prescriptions"
                )
            }

            item {
                MoreMenuCard(
                    icon = Icons.Default.Inventory2,
                    title = strings.inventory,
                    subtitle = "Track quantity, stock thresholds & expiry",
                    iconTint = Color(0xFFEA580C),
                    onClick = { onNavigate(Screen.INVENTORY) },
                    tag = "menu_inventory"
                )
            }

            item {
                MoreMenuCard(
                    icon = Icons.Default.History,
                    title = strings.history,
                    subtitle = "Medication adherence log and statistics",
                    iconTint = Color(0xFF059669),
                    onClick = { onNavigate(Screen.HISTORY) },
                    tag = "menu_history"
                )
            }

            // Family & Caregiver Section
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Caregiver & Family",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }

            item {
                MoreMenuCard(
                    icon = Icons.Default.People,
                    title = strings.caregivers,
                    subtitle = "Trusted caregivers notification preferences",
                    iconTint = Color(0xFF2563EB),
                    onClick = { onNavigate(Screen.CAREGIVERS) },
                    tag = "menu_caregivers"
                )
            }

            item {
                MoreMenuCard(
                    icon = Icons.Default.Assessment,
                    title = strings.reports,
                    subtitle = "Generate & export medication / doctor reports",
                    iconTint = Color(0xFF9333EA),
                    onClick = { onNavigate(Screen.REPORTS) },
                    tag = "menu_reports"
                )
            }

            // Preferences & About Section
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Application & Author",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }

            item {
                MoreMenuCard(
                    icon = Icons.Default.Settings,
                    title = strings.settings,
                    subtitle = "Language, theme, PIN lock & backup",
                    iconTint = Color(0xFF475569),
                    onClick = { onNavigate(Screen.SETTINGS) },
                    tag = "menu_settings"
                )
            }

            item {
                MoreMenuCard(
                    icon = Icons.Default.AccountCircle,
                    title = strings.aboutAuthor,
                    subtitle = "MD ARIF • Aspiring Software Engineer Profile",
                    iconTint = MaterialTheme.colorScheme.primary,
                    highlight = true,
                    onClick = { onNavigate(Screen.ABOUT_AUTHOR) },
                    tag = "menu_about_author"
                )
            }
        }
    }
}

@Composable
fun MoreMenuCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color,
    onClick: () -> Unit,
    tag: String,
    highlight: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(tag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
        ),
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
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
