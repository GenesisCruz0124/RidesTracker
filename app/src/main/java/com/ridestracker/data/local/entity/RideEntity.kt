package com.ridestracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ridestracker.domain.model.Ride
import com.ridestracker.domain.model.VehicleType

@Entity(tableName = "rides")
data class RideEntity(
    @PrimaryKey val id: String,
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
    val weatherJson: String?,
    val notes: String?,
    val tags: String,
    val moodRating: Int,
    val vehicleType: String,
    val steps: Int = 0
)

fun RideEntity.toDomain() = Ride(
    id = id,
    title = title,
    startedAt = startedAt,
    endedAt = endedAt,
    distanceKm = distanceKm,
    durationSeconds = durationSeconds,
    maxSpeedKmh = maxSpeedKmh,
    avgSpeedKmh = avgSpeedKmh,
    totalAscentM = totalAscentM,
    totalDescentM = totalDescentM,
    polylineEncoded = polylineEncoded,
    weatherJson = weatherJson,
    notes = notes,
    tags = if (tags.isBlank()) emptyList() else tags.split(","),
    moodRating = moodRating,
    vehicleType = runCatching { VehicleType.valueOf(vehicleType) }.getOrDefault(VehicleType.BICYCLE),
    steps = steps
)

fun Ride.toEntity() = RideEntity(
    id = id,
    title = title,
    startedAt = startedAt,
    endedAt = endedAt,
    distanceKm = distanceKm,
    durationSeconds = durationSeconds,
    maxSpeedKmh = maxSpeedKmh,
    avgSpeedKmh = avgSpeedKmh,
    totalAscentM = totalAscentM,
    totalDescentM = totalDescentM,
    polylineEncoded = polylineEncoded,
    weatherJson = weatherJson,
    notes = notes,
    tags = tags.joinToString(","),
    moodRating = moodRating,
    vehicleType = vehicleType.name,
    steps = steps
)
