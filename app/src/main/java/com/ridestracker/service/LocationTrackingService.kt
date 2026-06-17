package com.ridestracker.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.ridestracker.MainActivity
import com.ridestracker.R
import com.ridestracker.RidesTrackerApp.Companion.CHANNEL_RIDE_TRACKING
import com.ridestracker.domain.model.ActiveRideState
import com.ridestracker.domain.model.Coordinate
import com.ridestracker.domain.model.RideStatus
import com.ridestracker.util.DistanceUtil
import com.ridestracker.util.FormatUtil
import com.ridestracker.util.KalmanFilter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class LocationTrackingService : Service() {

    @Inject lateinit var fusedLocationClient: FusedLocationProviderClient

    private val binder = LocalBinder()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val kalmanFilter = KalmanFilter()

    private val _state = MutableStateFlow(ActiveRideState())
    val state: StateFlow<ActiveRideState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private var lastAltitude = 0.0
    private var timerStartEpoch = 0L
    private var accumulatedSeconds = 0L

    inner class LocalBinder : Binder() {
        fun getService(): LocationTrackingService = this@LocationTrackingService
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            if (_state.value.status != RideStatus.RECORDING) return
            val location = result.lastLocation ?: return

            val (filteredLat, filteredLng) = kalmanFilter.filter(
                location.latitude, location.longitude, location.accuracy
            )

            val speedKmh = (location.speed * 3.6f).coerceAtLeast(0f)
            val coord = Coordinate(
                lat = filteredLat, lng = filteredLng,
                altitude = location.altitude,
                speed = speedKmh,
                timestamp = System.currentTimeMillis()
            )

            _state.update { current ->
                val newCoords = current.coordinates + coord
                val newDistance = if (newCoords.size >= 2) {
                    current.distanceKm + DistanceUtil.haversineKm(
                        newCoords[newCoords.size - 2].lat, newCoords[newCoords.size - 2].lng,
                        filteredLat, filteredLng
                    )
                } else current.distanceKm

                val elevDelta = location.altitude - lastAltitude
                val newAscent = if (elevDelta > 0.5) current.totalAscentM + elevDelta.toFloat() else current.totalAscentM
                val newDescent = if (elevDelta < -0.5) current.totalDescentM + (-elevDelta).toFloat() else current.totalDescentM
                lastAltitude = location.altitude

                val totalSeconds = current.elapsedSeconds
                val newAvgSpeed = if (totalSeconds > 0) ((newDistance / (totalSeconds / 3600.0)).toFloat()) else 0f

                current.copy(
                    coordinates = newCoords,
                    currentSpeedKmh = speedKmh,
                    avgSpeedKmh = newAvgSpeed,
                    maxSpeedKmh = maxOf(current.maxSpeedKmh, speedKmh),
                    distanceKm = newDistance,
                    currentAltitudeM = location.altitude,
                    totalAscentM = newAscent,
                    totalDescentM = newDescent
                )
            }

            updateNotification()
        }
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())
        return START_STICKY
    }

    fun startRide() {
        val rideId = UUID.randomUUID().toString()
        kalmanFilter.reset()
        lastAltitude = 0.0
        timerStartEpoch = System.currentTimeMillis()
        accumulatedSeconds = 0L
        _state.value = ActiveRideState(status = RideStatus.RECORDING, rideId = rideId)
        startLocationUpdates()
        startTimer()
    }

    fun pauseRide() {
        accumulatedSeconds = _state.value.elapsedSeconds
        _state.update { it.copy(status = RideStatus.PAUSED) }
        timerJob?.cancel()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    fun resumeRide() {
        timerStartEpoch = System.currentTimeMillis()
        _state.update { it.copy(status = RideStatus.RECORDING) }
        startLocationUpdates()
        startTimer()
    }

    fun stopRide(): ActiveRideState {
        val finalState = _state.value.copy(status = RideStatus.COMPLETED)
        timerJob?.cancel()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        _state.value = ActiveRideState()
        stopForeground(STOP_FOREGROUND_REMOVE)
        return finalState
    }

    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
            .setMinUpdateDistanceMeters(5f)
            .build()
        try {
            fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        } catch (_: SecurityException) {}
    }

    private fun startTimer() {
        timerStartEpoch = System.currentTimeMillis()
        timerJob = scope.launch {
            while (isActive) {
                delay(1000L)
                val elapsed = (System.currentTimeMillis() - timerStartEpoch) / 1000L
                _state.update { it.copy(elapsedSeconds = accumulatedSeconds + elapsed) }
            }
        }
    }

    private fun buildNotification(): Notification {
        val intent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val state = _state.value
        return NotificationCompat.Builder(this, CHANNEL_RIDE_TRACKING)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(
                "${FormatUtil.formatDistance(state.distanceKm)} · ${FormatUtil.formatDuration(state.elapsedSeconds)}"
            )
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(intent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification())
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    companion object {
        const val NOTIFICATION_ID = 1001
    }
}
