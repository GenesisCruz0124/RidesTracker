package com.ridestracker.ui.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridestracker.data.repository.MaintenanceRepository
import com.ridestracker.domain.model.FuelEntry
import com.ridestracker.ui.theme.OrangeAccent
import com.ridestracker.util.FormatUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class FuelLogViewModel @Inject constructor(
    private val repository: MaintenanceRepository
) : ViewModel() {
    val entries: StateFlow<List<FuelEntry>> = repository.getAllFuelEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addEntry(odometerKm: Double, liters: Float, cost: Float) {
        viewModelScope.launch {
            repository.addFuelEntry(
                FuelEntry(
                    id = UUID.randomUUID().toString(),
                    date = System.currentTimeMillis(),
                    odometerKm = odometerKm,
                    liters = liters,
                    costTotal = cost
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelLogScreen(
    onBack: () -> Unit,
    viewModel: FuelLogViewModel = hiltViewModel()
) {
    val entries by viewModel.entries.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val avgEfficiency = if (entries.size >= 2) {
        val totalLiters = entries.sumOf { it.liters.toDouble() }
        val totalKm = (entries.maxOf { it.odometerKm } - entries.minOf { it.odometerKm }).coerceAtLeast(1.0)
        (totalLiters / totalKm * 100).toFloat()
    } else null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fuel Log") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = OrangeAccent) {
                Icon(Icons.Default.Add, "Add")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            avgEfficiency?.let { eff ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("%.1f L/100km".format(eff), style = MaterialTheme.typography.headlineSmall, color = OrangeAccent)
                            Text("Avg Efficiency", style = MaterialTheme.typography.bodySmall)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("%.1f L".format(entries.sumOf { it.liters.toDouble() }), style = MaterialTheme.typography.headlineSmall, color = OrangeAccent)
                            Text("Total Fuel", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(entries) { entry ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(FormatUtil.formatDate(entry.date), style = MaterialTheme.typography.titleSmall)
                                Text("%.0f km odometer".format(entry.odometerKm), style = MaterialTheme.typography.bodySmall)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("%.1f L".format(entry.liters), color = OrangeAccent, style = MaterialTheme.typography.titleSmall)
                                Text("₱%.0f".format(entry.costTotal), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddFuelDialog(
            onConfirm = { odometer, liters, cost ->
                viewModel.addEntry(odometer, liters, cost)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun AddFuelDialog(onConfirm: (Double, Float, Float) -> Unit, onDismiss: () -> Unit) {
    var odometer by remember { mutableStateOf("") }
    var liters by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Fill-up") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = odometer, onValueChange = { odometer = it }, label = { Text("Odometer (km)") }, singleLine = true)
                OutlinedTextField(value = liters, onValueChange = { liters = it }, label = { Text("Liters") }, singleLine = true)
                OutlinedTextField(value = cost, onValueChange = { cost = it }, label = { Text("Total cost") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = {
                val o = odometer.toDoubleOrNull() ?: return@Button
                val l = liters.toFloatOrNull() ?: return@Button
                val c = cost.toFloatOrNull() ?: return@Button
                onConfirm(o, l, c)
            }) { Text("Save") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
