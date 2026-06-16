package com.ridestracker.ui.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ridestracker.ui.theme.OrangeAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyContactScreen(
    onBack: () -> Unit,
    viewModel: EmergencyContactViewModel = hiltViewModel()
) {
    val contact by viewModel.emergencyContact.collectAsState()
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var initialized by remember { mutableStateOf(false) }

    if (!initialized && (contact.name.isNotEmpty() || contact.phoneNumber.isNotEmpty())) {
        name = contact.name
        phoneNumber = contact.phoneNumber
        initialized = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emergency Contact") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "This person will receive an SMS with your live location if a crash is detected or you tap Send SOS.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Contact name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone number") },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { viewModel.save(name.trim(), phoneNumber.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                enabled = phoneNumber.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}
