package com.ridestracker.domain.model

data class ActiveRideState(
    val status: RideStatus = RideStatus.IDLE,
    val coordinates: List<Coordinate> = emptyList(),
    val currentSpeedKmh: Float = 0f,
    val avgSpeedKmh: Float = 0f,
    val maxSpeedKmh: Float = 0f,
    val distanceKm: Double = 0.0,
    val elapsedSeconds: Long = 0L,
    val currentAltitudeM: Double = 0.0,
    val totalAscentM: Float = 0f,
    val totalDescentM: Float = 0f,
    val rideId: String? = null
)

enum class RideStatus { IDLE, RECORDING, PAUSED, COMPLETED }
