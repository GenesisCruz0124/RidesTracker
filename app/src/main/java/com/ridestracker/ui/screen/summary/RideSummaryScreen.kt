package com.ridestracker.ui.screen.summary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridestracker.data.repository.RideRepository
import com.ridestracker.domain.model.Coordinate
import com.ridestracker.domain.model.Ride
import com.ridestracker.ui.component.RouteMapView
import com.ridestracker.ui.component.StatCard
import com.ridestracker.ui.theme.GreenSuccess
import com.ridestracker.ui.theme.OrangeAccent
import com.ridestracker.util.FormatUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RideSummaryViewModel @Inject constructor(
    private val rideRepository: RideRepository
) : ViewModel() {
    private val _ride = MutableStateFlow<Ride?>(null)
    val ride: StateFlow<Ride?> = _ride

    private val _coordinates = MutableStateFlow<List<Coordinate>>(emptyList())
    val coordinates: StateFlow<List<Coordinate>> = _coordinates

    fun loadRide(id: String) {
        viewModelScope.launch {
            _ride.value = rideRepository.getRideById(id)
            _coordinates.value = rideRepository.getCoordinatesForRide(id)
        }
    }

    fun updateTitle(rideId: String, title: String) {
        viewModelScope.launch {
            _ride.value?.let { rideRepository.updateRide(it.copy(title = title)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideSummaryScreen(
    rideId: String,
    onDone: () -> Unit,
    viewModel: RideSummaryViewModel = hiltViewModel()
) {
    LaunchedEffect(rideId) { viewModel.loadRide(rideId) }
    val ride by viewModel.ride.collectAsState()
    val coordinates by viewModel.coordinates.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Ride Complete!") })
        }
    ) { padding ->
        ride?.let { r ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(64.dp), tint = GreenSuccess)
                        Spacer(Modifier.height(8.dp))
                        Text(r.title, style = MaterialTheme.typography.titleLarge)
                        Text(FormatUtil.formatDate(r.startedAt), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Card(shape = RoundedCornerShape(16.dp)) {
                    RouteMapView(coordinates = coordinates)
                }

                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Summary", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            StatCard("DISTANCE", FormatUtil.formatDistance(r.distanceKm))
                            StatCard("DURATION", FormatUtil.formatDuration(r.durationSeconds))
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            StatCard("AVG SPEED", FormatUtil.formatSpeed(r.avgSpeedKmh))
                            StatCard("MAX SPEED", FormatUtil.formatSpeed(r.maxSpeedKmh))
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            StatCard("ASCENT", FormatUtil.formatElevation(r.totalAscentM))
                            StatCard("DESCENT", FormatUtil.formatElevation(r.totalDescentM))
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { /* TODO: share */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Share")
                    }
                    Button(
                        onClick = onDone,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }
}
