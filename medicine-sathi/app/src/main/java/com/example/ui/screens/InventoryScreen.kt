package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Medicine
import com.example.ui.ExpiryAlertItem
import com.example.ui.MainViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.components.QuantityUpdateDialog
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.localization.LocalAppStrings
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val strings = LocalAppStrings.current
    val medicines by viewModel.medicines.collectAsState()
    val expiringMedicines by viewModel.expiringMedicines.collectAsState()

    var medicineToUpdateStock by remember { mutableStateOf<Medicine?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.inventory, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        if (medicines.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Inventory2,
                title = "No Medicines in Inventory",
                description = "Add medicines to track their remaining stock, reorder thresholds, and expiration dates.",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("inventory_screen"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Expired / Expiring Medicines Section
                if (expiringMedicines.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Expiry Alerts (${expiringMedicines.size})")
                    }

                    items(expiringMedicines) { item ->
                        val isExpired = item.status == "EXPIRED"
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isExpired) StatusDanger.copy(alpha = 0.12f) else StatusWarning.copy(alpha = 0.12f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isExpired) Icons.Default.Dangerous else Icons.Default.WarningAmber,
                                    contentDescription = null,
                                    tint = if (isExpired) StatusDanger else StatusWarning,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.medicine.name,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (isExpired) StatusDanger else Color(0xFFB45309)
                                    )
                                    Text(
                                        text = if (isExpired) "EXPIRED on ${item.medicine.expiryDate}. Do not take this medicine."
                                        else "Expires in ${item.daysRemaining} days (${item.medicine.expiryDate})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                StatusBadge(status = if (isExpired) "EXPIRED" else "EXPIRING")
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }

                // Inventory Stock Tracker Section
                item {
                    SectionHeader(title = "Medicine Stock Levels")
                }

                items(medicines, key = { it.id }) { med ->
                    val isLow = med.remainingQuantity <= med.minQuantityThreshold
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
                                    Text(text = med.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = "${med.medicineType} • Alert threshold: ≤ ${med.minQuantityThreshold} ${med.unit}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isLow) {
                                    StatusBadge(status = "LOW_STOCK")
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Quantity Bar / Indicator
                            LinearProgressIndicator(
                                progress = {
                                    if (med.initialQuantity > 0)
                                        (med.remainingQuantity.toFloat() / med.initialQuantity.toFloat()).coerceIn(0f, 1f)
                                    else 0f
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (isLow) StatusWarning else MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${med.remainingQuantity} / ${med.initialQuantity} ${med.unit}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLow) StatusWarning else MaterialTheme.colorScheme.primary
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    FilledTonalIconButton(
                                        onClick = {
                                            if (med.remainingQuantity > 0) {
                                                viewModel.updateMedicineQuantity(med.id, med.remainingQuantity - 1)
                                            }
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease 1")
                                    }

                                    FilledTonalIconButton(
                                        onClick = {
                                            viewModel.updateMedicineQuantity(med.id, med.remainingQuantity + 1)
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase 1")
                                    }

                                    OutlinedButton(
                                        onClick = { medicineToUpdateStock = med },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Set Qty")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

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
