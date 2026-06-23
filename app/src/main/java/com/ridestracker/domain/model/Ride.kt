package com.ridestracker.domain.model

import java.util.UUID

data class Coordinate(
    val lat: Double,
    val lng: Double,
    val altitude: Double,
    val speed: Float,
    val timestamp: Long
)

data class Ride(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val startedAt: Long,
    val endedAt: Long,
    val distanceKm: Double,
    val durationSeconds: Long,
    val maxSpeedKmh: Float,
    val avgSpeedKmh: Float,
    val totalAscentM: Float,
    val totalDescentM: Float,
    val polylineEncoded: String,
    val weatherJson: String? = null,
    val notes: String? = null,
    val tags: List<String> = emptyList(),
    val moodRating: Int = 0,
    val vehicleType: VehicleType = VehicleType.BICYCLE,
    val steps: Int = 0
)

enum class VehicleType { BICYCLE, MOTORCYCLE, WALKING }

enum class RideTag(val label: String) {
    COMMUTE("Commute"),
    TRAINING("Training"),
    TOURING("Touring"),
    OFFROAD("Off-road"),
    SOCIAL("Social"),
    RACE("Race")
}

data class MaintenanceItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val lastDoneKm: Double,
    val intervalKm: Double,
    val lastDoneDate: Long
) {
    val nextDueKm: Double get() = lastDoneKm + intervalKm
}

data class FuelEntry(
    val id: String = UUID.randomUUID().toString(),
    val date: Long,
    val odometerKm: Double,
    val liters: Float,
    val costTotal: Float
) {
    val costPerLiter: Float get() = if (liters > 0) costTotal / liters else 0f
}

data class WeatherSnapshot(
    val temperatureCelsius: Float,
    val conditionCode: Int,
    val conditionLabel: String,
    val windSpeedKmh: Float,
    val precipitationMm: Float,
    val humidity: Int
)
