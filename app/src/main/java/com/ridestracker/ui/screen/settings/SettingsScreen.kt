package com.ridestracker.ui.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ridestracker.BuildConfig
import com.ridestracker.domain.model.MapStyle
import com.ridestracker.ui.theme.OrangeAccent

private const val DEVELOPER_EMAIL = "genesiscruz.dev@gmail.com"

@Composable
fun SettingsScreen(
    onNavigateMaintenance: () -> Unit,
    onNavigateFuel: () -> Unit,
    onNavigateEmergencyContact: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val mapStyle by viewModel.mapStyle.collectAsState()
    var showMapStyleDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("More", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))

        Text("Vehicle", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        SettingsItem(Icons.Default.TwoWheeler, "Maintenance Tracker", "Service reminders & history", onNavigateMaintenance)
        SettingsItem(Icons.Default.LocalGasStation, "Fuel Log", "Track fill-ups & efficiency", onNavigateFuel)

        Spacer(Modifier.height(8.dp))
        Text("Safety", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        SettingsItem(Icons.Default.LocalPhone, "Emergency Contact", "SOS alert recipient", onNavigateEmergencyContact)
        SettingsItem(Icons.Default.Warning, "Crash Detection", "Auto-detect hard impacts", {})

        Spacer(Modifier.height(8.dp))
        Text("App", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        SettingsItem(Icons.Default.Straighten, "Units", "km / miles", {})
        SettingsItem(Icons.Default.Map, "Map Style", mapStyle.label, { showMapStyleDialog = true })
        SettingsItem(Icons.Default.FileDownload, "Export Data", "GPX / CSV", {})
        SettingsItem(Icons.Default.Info, "About", "Version ${BuildConfig.VERSION_NAME}", { showAboutDialog = true })
    }

    if (showMapStyleDialog) {
        MapStyleDialog(
            selected = mapStyle,
            onSelect = {
                viewModel.selectMapStyle(it)
                showMapStyleDialog = false
            },
            onDismiss = { showMapStyleDialog = false }
        )
    }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }
}

@Composable
private fun MapStyleDialog(selected: MapStyle, onSelect: (MapStyle) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Map Style") },
        text = {
            Column {
                MapStyle.entries.forEach { style ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(style) }.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = style == selected, onClick = { onSelect(style) })
                        Spacer(Modifier.width(8.dp))
                        Text(style.label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("About RidesTracker") },
        text = {
            Column {
                Text("Version ${BuildConfig.VERSION_NAME}")
                Spacer(Modifier.height(12.dp))
                Text("Developer", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(DEVELOPER_EMAIL)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun SettingsItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = OrangeAccent, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
