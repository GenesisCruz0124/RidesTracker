package com.ridestracker.ui.screen.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridestracker.data.repository.RideRepository
import com.ridestracker.data.repository.SettingsRepository
import com.ridestracker.domain.model.Coordinate
import com.ridestracker.domain.model.MapStyle
import com.ridestracker.domain.model.Ride
import com.ridestracker.domain.model.VehicleType
import com.ridestracker.ui.component.RouteMapView
import com.ridestracker.ui.component.StatCard
import com.ridestracker.util.FormatUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RideDetailViewModel @Inject constructor(
    private val rideRepository: RideRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {
    private val _ride = MutableStateFlow<Ride?>(null)
    val ride: StateFlow<Ride?> = _ride

    private val _coordinates = MutableStateFlow<List<Coordinate>>(emptyList())
    val coordinates: StateFlow<List<Coordinate>> = _coordinates

    val mapStyle: StateFlow<MapStyle> = settingsRepository.mapStyle
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MapStyle.STANDARD)

    fun loadRide(rideId: String) {
        viewModelScope.launch {
            _ride.value = rideRepository.getRideById(rideId)
            _coordinates.value = rideRepository.getCoordinatesForRide(rideId)
        }
    }

    fun deleteRide(rideId: String, onDeleted: () -> Unit) {
        viewModelScope.launch {
            rideRepository.deleteRide(rideId)
            onDeleted()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideDetailScreen(
    rideId: String,
    onBack: () -> Unit,
    viewModel: RideDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(rideId) { viewModel.loadRide(rideId) }

    val ride by viewModel.ride.collectAsState()
    val coordinates by viewModel.coordinates.collectAsState()
    val mapStyle by viewModel.mapStyle.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(ride?.title ?: "Ride Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        ride?.let { r ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(FormatUtil.formatDate(r.startedAt), style = MaterialTheme.typography.bodyMedium)

                Card(shape = RoundedCornerShape(16.dp)) {
                    RouteMapView(coordinates = coordinates, mapStyle = mapStyle)
                }

                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Stats", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            StatCard("DISTANCE", FormatUtil.formatDistance(r.distanceKm))
                            StatCard("DURATION", FormatUtil.formatDuration(r.durationSeconds))
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            StatCard("AVG SPEED", FormatUtil.formatSpeed(r.avgSpeedKmh))
                            StatCard("MAX SPEED", FormatUtil.formatSpeed(r.maxSpeedKmh))
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            StatCard("ASCENT", FormatUtil.formatElevation(r.totalAscentM))
                            StatCard("DESCENT", FormatUtil.formatElevation(r.totalDescentM))
                        }
                        if (r.vehicleType == VehicleType.WALKING) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                StatCard("STEPS", r.steps.toString())
                            }
                        }
                    }
                }

                if (!r.notes.isNullOrBlank()) {
                    Card {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Notes", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            Text(r.notes)
                        }
                    }
                }

                if (r.tags.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        r.tags.forEach { tag ->
                            AssistChip(onClick = {}, label = { Text(tag) })
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Delete Ride")
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Ride") },
            text = { Text("This ride will be permanently deleted.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteRide(rideId, onBack) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = { OutlinedButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }
}
