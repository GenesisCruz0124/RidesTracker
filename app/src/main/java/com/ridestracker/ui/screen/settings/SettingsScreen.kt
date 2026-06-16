package com.ridestracker.ui.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ridestracker.ui.theme.OrangeAccent

@Composable
fun SettingsScreen(
    onNavigateMaintenance: () -> Unit,
    onNavigateFuel: () -> Unit
) {
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
        SettingsItem(Icons.Default.LocalPhone, "Emergency Contact", "SOS alert recipient", {})
        SettingsItem(Icons.Default.Warning, "Crash Detection", "Auto-detect hard impacts", {})

        Spacer(Modifier.height(8.dp))
        Text("App", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        SettingsItem(Icons.Default.Straighten, "Units", "km / miles", {})
        SettingsItem(Icons.Default.Map, "Map Style", "Dark / Satellite / Terrain", {})
        SettingsItem(Icons.Default.FileDownload, "Export Data", "GPX / CSV", {})
        SettingsItem(Icons.Default.Info, "About", "Version 1.0.0", {})
    }
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
