package com.ridestracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ridestracker.domain.model.Coordinate

@Entity(
    tableName = "coordinates",
    foreignKeys = [ForeignKey(
        entity = RideEntity::class,
        parentColumns = ["id"],
        childColumns = ["rideId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("rideId")]
)
data class CoordinateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rideId: String,
    val lat: Double,
    val lng: Double,
    val altitude: Double,
    val speed: Float,
    val timestamp: Long
)

fun CoordinateEntity.toDomain() = Coordinate(lat, lng, altitude, speed, timestamp)

fun Coordinate.toEntity(rideId: String) = CoordinateEntity(
    rideId = rideId, lat = lat, lng = lng,
    altitude = altitude, speed = speed, timestamp = timestamp
)
