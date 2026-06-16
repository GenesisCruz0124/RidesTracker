package com.ridestracker.ui.screen.track

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.telephony.SmsManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ridestracker.data.remote.WeatherService
import com.ridestracker.data.remote.toWeatherLabel
import com.ridestracker.data.repository.EmergencyContactRepository
import com.ridestracker.data.repository.RideRepository
import com.ridestracker.domain.model.ActiveRideState
import com.ridestracker.domain.model.Coordinate
import com.ridestracker.domain.model.Ride
import com.ridestracker.domain.model.RideStatus
import com.ridestracker.domain.model.VehicleType
import com.ridestracker.domain.model.WeatherSnapshot
import com.ridestracker.service.CrashDetectionService
import com.ridestracker.service.LocationTrackingService
import com.ridestracker.util.DistanceUtil
import com.ridestracker.util.FormatUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackViewModel @Inject constructor(
    application: Application,
    private val rideRepository: RideRepository,
    private val weatherService: WeatherService,
    private val emergencyContactRepository: EmergencyContactRepository,
    val crashDetectionService: CrashDetectionService
) : AndroidViewModel(application) {

    private val _trackingService = MutableStateFlow<LocationTrackingService?>(null)

    val rideState: StateFlow<ActiveRideState> = _trackingService
        .flatMapLatest { service -> service?.state ?: flowOf(ActiveRideState()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ActiveRideState())

    private val _savedRideId = MutableStateFlow<String?>(null)
    val savedRideId: StateFlow<String?> = _savedRideId.asStateFlow()

    private val _weather = MutableStateFlow<WeatherSnapshot?>(null)
    val weather: StateFlow<WeatherSnapshot?> = _weather.asStateFlow()

    private val _showCrashAlert = MutableStateFlow(false)
    val showCrashAlert: StateFlow<Boolean> = _showCrashAlert.asStateFlow()

    private val _sosResult = MutableStateFlow<SosResult?>(null)
    val sosResult: StateFlow<SosResult?> = _sosResult.asStateFlow()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            _trackingService.value = (binder as LocationTrackingService.LocalBinder).getService()
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            _trackingService.value = null
        }
    }

    init {
        bindToService()
        observeCrashDetection()
    }

    private fun bindToService() {
        val intent = Intent(getApplication(), LocationTrackingService::class.java)
        getApplication<Application>().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun observeCrashDetection() {
        viewModelScope.launch {
            crashDetectionService.crashDetected.collect { detected ->
                if (detected && rideState.value.status == RideStatus.RECORDING) {
                    _showCrashAlert.value = true
                }
            }
        }
    }

    fun startRide() {
        val app = getApplication<Application>()
        val intent = Intent(app, LocationTrackingService::class.java)
        app.startForegroundService(intent)
        _trackingService.value?.startRide()
        crashDetectionService.start()
    }

    fun pauseRide() = _trackingService.value?.pauseRide()

    fun resumeRide() = _trackingService.value?.resumeRide()

    fun stopRide() {
        val finalState = _trackingService.value?.stopRide() ?: return
        crashDetectionService.stop()
        viewModelScope.launch {
            val rideId = finalState.rideId ?: return@launch
            val (ascent, descent) = DistanceUtil.elevationGainLoss(finalState.coordinates)
            val ride = Ride(
                id = rideId,
                title = "Ride on ${FormatUtil.formatDate(System.currentTimeMillis())}",
                startedAt = System.currentTimeMillis() - (finalState.elapsedSeconds * 1000),
                endedAt = System.currentTimeMillis(),
                distanceKm = finalState.distanceKm,
                durationSeconds = finalState.elapsedSeconds,
                maxSpeedKmh = finalState.maxSpeedKmh,
                avgSpeedKmh = finalState.avgSpeedKmh,
                totalAscentM = ascent,
                totalDescentM = descent,
                polylineEncoded = encodePolyline(finalState.coordinates),
                weatherJson = _weather.value?.let { Gson().toJson(it) },
                vehicleType = VehicleType.BICYCLE
            )
            rideRepository.saveRide(ride, finalState.coordinates)
            _savedRideId.value = rideId
        }
    }

    fun fetchWeather(lat: Double, lng: Double) {
        viewModelScope.launch {
            runCatching {
                val response = weatherService.getCurrentWeather(lat, lng)
                _weather.value = WeatherSnapshot(
                    temperatureCelsius = response.current.temperatureCelsius,
                    conditionCode = response.current.weatherCode,
                    conditionLabel = response.current.weatherCode.toWeatherLabel(),
                    windSpeedKmh = response.current.windSpeedKmh,
                    precipitationMm = response.current.precipitationMm,
                    humidity = response.current.humidity
                )
            }
        }
    }

    fun dismissCrashAlert() {
        _showCrashAlert.value = false
        crashDetectionService.acknowledgeCrash()
    }

    fun sendSOS() {
        _showCrashAlert.value = false
        crashDetectionService.acknowledgeCrash()
        viewModelScope.launch {
            val contact = emergencyContactRepository.emergencyContact.first()
            if (!contact.isSet) {
                _sosResult.value = SosResult.NoContactConfigured
                return@launch
            }
            val coord = rideState.value.coordinates.lastOrNull()
            val locationText = if (coord != null) {
                "https://maps.google.com/?q=${coord.lat},${coord.lng}"
            } else {
                "location unavailable"
            }
            val message = "RidesTracker SOS: I may need help. My last known location: $locationText"
            try {
                SmsManager.getDefault().sendTextMessage(contact.phoneNumber, null, message, null, null)
                _sosResult.value = SosResult.Sent
            } catch (e: SecurityException) {
                _sosResult.value = SosResult.PermissionDenied
            }
        }
    }

    fun clearSosResult() { _sosResult.value = null }

    fun clearSavedRideId() { _savedRideId.value = null }

    private fun encodePolyline(coordinates: List<Coordinate>): String {
        val sb = StringBuilder()
        var prevLat = 0
        var prevLng = 0
        for (coord in coordinates) {
            val lat = (coord.lat * 1e5).toInt()
            val lng = (coord.lng * 1e5).toInt()
            sb.append(encodeValue(lat - prevLat))
            sb.append(encodeValue(lng - prevLng))
            prevLat = lat; prevLng = lng
        }
        return sb.toString()
    }

    private fun encodeValue(v: Int): String {
        var value = if (v < 0) (v shl 1).inv() else v shl 1
        val sb = StringBuilder()
        while (value >= 0x20) {
            sb.append(((0x20 or (value and 0x1f)) + 63).toChar())
            value = value shr 5
        }
        sb.append((value + 63).toChar())
        return sb.toString()
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().unbindService(serviceConnection)
    }
}

sealed class SosResult {
    object Sent : SosResult()
    object NoContactConfigured : SosResult()
    object PermissionDenied : SosResult()
}
