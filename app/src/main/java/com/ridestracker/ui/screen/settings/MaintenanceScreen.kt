package com.ridestracker.ui.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridestracker.data.repository.MaintenanceRepository
import com.ridestracker.domain.model.MaintenanceItem
import com.ridestracker.ui.theme.OrangeAccent
import com.ridestracker.ui.theme.TextSecondary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MaintenanceViewModel @Inject constructor(
    private val repository: MaintenanceRepository
) : ViewModel() {
    val items: StateFlow<List<MaintenanceItem>> = repository.getAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addItem(name: String, intervalKm: Double) {
        viewModelScope.launch {
            repository.saveItem(
                MaintenanceItem(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    lastDoneKm = 0.0,
                    intervalKm = intervalKm,
                    lastDoneDate = System.currentTimeMillis()
                )
            )
        }
    }

    fun logService(item: MaintenanceItem, currentKm: Double) {
        viewModelScope.launch {
            repository.updateItem(item.copy(lastDoneKm = currentKm, lastDoneDate = System.currentTimeMillis()))
        }
    }

    fun deleteItem(id: String) { viewModelScope.launch { repository.deleteItem(id) } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceScreen(
    onBack: () -> Unit,
    viewModel: MaintenanceViewModel = hiltViewModel()
) {
    val items by viewModel.items.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var logServiceTarget by remember { mutableStateOf<MaintenanceItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Maintenance") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = OrangeAccent) {
                Icon(Icons.Default.Add, "Add")
            }
        }
    ) { padding ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Build, null, modifier = Modifier.size(80.dp), tint = TextSecondary)
                    Spacer(Modifier.height(16.dp))
                    Text("No maintenance items yet", style = MaterialTheme.typography.titleLarge, color = TextSecondary)
                    Text("Tap + to add a service reminder", color = TextSecondary)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                items(items, key = { it.id }) { item ->
                    MaintenanceCard(
                        item = item,
                        onLogService = { logServiceTarget = item },
                        onDelete = { viewModel.deleteItem(item.id) }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }

    if (showAddDialog) {
        AddMaintenanceDialog(
            onConfirm = { name, interval ->
                viewModel.addItem(name, interval)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    logServiceTarget?.let { target ->
        LogServiceDialog(
            item = target,
            onConfirm = { currentKm ->
                viewModel.logService(target, currentKm)
                logServiceTarget = null
            },
            onDismiss = { logServiceTarget = null }
        )
    }
}

@Composable
private fun MaintenanceCard(item: MaintenanceItem, onLogService: () -> Unit, onDelete: () -> Unit) {
    val progress = ((item.lastDoneKm) / item.nextDueKm).coerceIn(0.0, 1.0).toFloat()
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onLogService)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = if (progress > 0.8f) MaterialTheme.colorScheme.error else OrangeAccent
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Next due at %.0f km · tap to log service".format(item.nextDueKm),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LogServiceDialog(item: MaintenanceItem, onConfirm: (Double) -> Unit, onDismiss: () -> Unit) {
    var currentKm by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Service · ${item.name}") },
        text = {
            OutlinedTextField(
                value = currentKm,
                onValueChange = { currentKm = it },
                label = { Text("Current odometer (km)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = {
                val km = currentKm.toDoubleOrNull() ?: return@Button
                onConfirm(km)
            }) { Text("Log Service") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AddMaintenanceDialog(onConfirm: (String, Double) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var intervalKm by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Maintenance Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item name") }, singleLine = true)
                OutlinedTextField(value = intervalKm, onValueChange = { intervalKm = it }, label = { Text("Interval (km)") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = {
                val interval = intervalKm.toDoubleOrNull() ?: return@Button
                if (name.isNotBlank()) onConfirm(name, interval)
            }) { Text("Add") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
