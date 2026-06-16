package com.ridestracker.ui.screen.track

import android.Manifest
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import com.ridestracker.domain.model.RideStatus
import com.ridestracker.ui.component.StatCard
import com.ridestracker.ui.theme.*
import com.ridestracker.util.DistanceUtil
import com.ridestracker.util.FormatUtil

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TrackScreen(
    onNavigateToSummary: (String) -> Unit,
    viewModel: TrackViewModel = hiltViewModel()
) {
    val rideState by viewModel.rideState.collectAsState()
    val savedRideId by viewModel.savedRideId.collectAsState()
    val weather by viewModel.weather.collectAsState()
    val showCrashAlert by viewModel.showCrashAlert.collectAsState()

    val locationPermissions = rememberMultiplePermissionsState(
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    )

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(14.5995, 120.9842), 13f)
    }

    LaunchedEffect(savedRideId) {
        savedRideId?.let { id ->
            viewModel.clearSavedRideId()
            onNavigateToSummary(id)
        }
    }

    LaunchedEffect(rideState.coordinates) {
        rideState.coordinates.lastOrNull()?.let { coord ->
            val pos = LatLng(coord.lat, coord.lng)
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(pos, 16f))
            if (rideState.coordinates.size == 1) {
                viewModel.fetchWeather(coord.lat, coord.lng)
            }
        }
    }

    if (showCrashAlert) {
        CrashAlertDialog(
            onDismiss = { viewModel.dismissCrashAlert() },
            onSendSOS = { viewModel.dismissCrashAlert() /* TODO: send SMS */ }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (locationPermissions.allPermissionsGranted) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = true,
                    mapStyleOptions = MapStyleOptions(DARK_MAP_STYLE)
                ),
                uiSettings = MapUiSettings(zoomControlsEnabled = false)
            ) {
                if (rideState.coordinates.size >= 2) {
                    Polyline(
                        points = rideState.coordinates.map { DistanceUtil.toLatLng(it) },
                        color = OrangeAccent,
                        width = 12f
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.LocationOff, null, modifier = Modifier.size(64.dp), tint = OrangeAccent)
                Spacer(Modifier.height(16.dp))
                Text("Location permission required", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Button(onClick = { locationPermissions.launchMultiplePermissionRequest() }) {
                    Text("Grant Permission")
                }
            }
        }

        weather?.let { w ->
            WeatherBadge(
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).statusBarsPadding(),
                temperature = w.temperatureCelsius,
                condition = w.conditionLabel
            )
        }

        AnimatedVisibility(
            visible = rideState.status == RideStatus.RECORDING || rideState.status == RideStatus.PAUSED,
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp).statusBarsPadding(),
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            RecordingIndicator(isPaused = rideState.status == RideStatus.PAUSED)
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
        ) {
            if (rideState.status != RideStatus.IDLE) {
                StatsBottomSheet(
                    distanceKm = rideState.distanceKm,
                    elapsedSeconds = rideState.elapsedSeconds,
                    currentSpeedKmh = rideState.currentSpeedKmh,
                    avgSpeedKmh = rideState.avgSpeedKmh,
                    maxSpeedKmh = rideState.maxSpeedKmh,
                    elevationM = rideState.currentAltitudeM.toFloat()
                )
            }

            RideControls(
                status = rideState.status,
                onStart = {
                    if (locationPermissions.allPermissionsGranted) viewModel.startRide()
                    else locationPermissions.launchMultiplePermissionRequest()
                },
                onPause = { viewModel.pauseRide() },
                onResume = { viewModel.resumeRide() },
                onStop = { viewModel.stopRide() }
            )
        }
    }
}

@Composable
private fun StatsBottomSheet(
    distanceKm: Double, elapsedSeconds: Long, currentSpeedKmh: Float,
    avgSpeedKmh: Float, maxSpeedKmh: Float, elevationM: Float
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceDark.copy(alpha = 0.95f),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard("DISTANCE", FormatUtil.formatDistance(distanceKm))
                StatCard("DURATION", FormatUtil.formatDuration(elapsedSeconds))
                StatCard("SPEED", FormatUtil.formatSpeed(currentSpeedKmh))
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard("AVG", FormatUtil.formatSpeed(avgSpeedKmh))
                StatCard("MAX", FormatUtil.formatSpeed(maxSpeedKmh))
                StatCard("ELEVATION", "%.0f m".format(elevationM))
            }
        }
    }
}

@Composable
private fun RideControls(
    status: RideStatus,
    onStart: () -> Unit, onPause: () -> Unit,
    onResume: () -> Unit, onStop: () -> Unit
) {
    Surface(color = SurfaceDark) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 16.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (status) {
                RideStatus.IDLE -> {
                    FloatingActionButton(
                        onClick = onStart,
                        modifier = Modifier.size(80.dp),
                        containerColor = OrangeAccent
                    ) {
                        Icon(Icons.Default.PlayArrow, "Start", Modifier.size(40.dp), tint = Color.White)
                    }
                }
                RideStatus.RECORDING -> {
                    FloatingActionButton(
                        onClick = onPause,
                        modifier = Modifier.size(64.dp),
                        containerColor = AmberPause
                    ) {
                        Icon(Icons.Default.Pause, "Pause", tint = Color.White)
                    }
                    Spacer(Modifier.width(32.dp))
                    FloatingActionButton(
                        onClick = onStop,
                        modifier = Modifier.size(64.dp),
                        containerColor = RedDanger
                    ) {
                        Icon(Icons.Default.Stop, "Stop", tint = Color.White)
                    }
                }
                RideStatus.PAUSED -> {
                    FloatingActionButton(
                        onClick = onResume,
                        modifier = Modifier.size(64.dp),
                        containerColor = GreenSuccess
                    ) {
                        Icon(Icons.Default.PlayArrow, "Resume", tint = Color.White)
                    }
                    Spacer(Modifier.width(32.dp))
                    FloatingActionButton(
                        onClick = onStop,
                        modifier = Modifier.size(64.dp),
                        containerColor = RedDanger
                    ) {
                        Icon(Icons.Default.Stop, "Stop", tint = Color.White)
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun WeatherBadge(modifier: Modifier, temperature: Float, condition: String) {
    Surface(
        modifier = modifier,
        color = SurfaceDark.copy(alpha = 0.85f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.WbSunny, null, tint = AmberPause, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("%.0f°C · %s".format(temperature, condition), style = MaterialTheme.typography.labelLarge, color = TextPrimary)
        }
    }
}

@Composable
private fun RecordingIndicator(isPaused: Boolean) {
    Surface(
        color = if (isPaused) AmberPause else RedDanger,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.White))
            Spacer(Modifier.width(6.dp))
            Text(if (isPaused) "PAUSED" else "REC", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
private fun CrashAlertDialog(onDismiss: () -> Unit, onSendSOS: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crash Detected!", color = RedDanger, fontWeight = FontWeight.Bold) },
        text = { Text("A hard impact was detected. Are you okay? Tap 'I'm OK' to dismiss or 'Send SOS' to alert your emergency contact.") },
        confirmButton = {
            Button(onClick = onSendSOS, colors = ButtonDefaults.buttonColors(containerColor = RedDanger)) {
                Text("Send SOS")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("I'm OK") }
        }
    )
}

private const val DARK_MAP_STYLE = """[{"elementType":"geometry","stylers":[{"color":"#212121"}]},{"elementType":"labels.icon","stylers":[{"visibility":"off"}]},{"elementType":"labels.text.fill","stylers":[{"color":"#757575"}]},{"elementType":"labels.text.stroke","stylers":[{"color":"#212121"}]},{"featureType":"road","elementType":"geometry","stylers":[{"color":"#2c2c2c"}]},{"featureType":"road.highway","elementType":"geometry","stylers":[{"color":"#3c3c3c"}]},{"featureType":"water","elementType":"geometry","stylers":[{"color":"#000000"}]}]"""
