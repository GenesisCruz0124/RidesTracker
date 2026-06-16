package com.ridestracker.data.repository

import com.ridestracker.data.local.dao.RideDao
import com.ridestracker.data.local.entity.toDomain
import com.ridestracker.data.local.entity.toEntity
import com.ridestracker.domain.model.Coordinate
import com.ridestracker.domain.model.Ride
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RideRepository @Inject constructor(private val rideDao: RideDao) {

    fun getAllRides(): Flow<List<Ride>> =
        rideDao.getAllRides().map { it.map { entity -> entity.toDomain() } }

    suspend fun getRideById(id: String): Ride? = rideDao.getRideById(id)?.toDomain()

    suspend fun getCoordinatesForRide(rideId: String): List<Coordinate> =
        rideDao.getCoordinatesForRide(rideId).map { it.toDomain() }

    suspend fun saveRide(ride: Ride, coordinates: List<Coordinate>) {
        rideDao.insertRide(ride.toEntity())
        rideDao.insertCoordinates(coordinates.map { it.toEntity(ride.id) })
    }

    suspend fun updateRide(ride: Ride) = rideDao.updateRide(ride.toEntity())

    suspend fun deleteRide(rideId: String) = rideDao.deleteRide(rideId)

    suspend fun getLifetimeStats() = LifetimeStats(
        totalDistanceKm = rideDao.getTotalDistanceKm() ?: 0.0,
        totalDurationSeconds = rideDao.getTotalDurationSeconds() ?: 0L,
        totalRides = rideDao.getTotalRideCount(),
        allTimeMaxSpeedKmh = rideDao.getAllTimeMaxSpeed() ?: 0f,
        longestRideKm = rideDao.getLongestRideKm() ?: 0.0
    )
}

data class LifetimeStats(
    val totalDistanceKm: Double,
    val totalDurationSeconds: Long,
    val totalRides: Int,
    val allTimeMaxSpeedKmh: Float,
    val longestRideKm: Double
)
