package com.ridestracker.data.local.dao

import androidx.room.*
import com.ridestracker.data.local.entity.CoordinateEntity
import com.ridestracker.data.local.entity.RideEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RideDao {

    @Query("SELECT * FROM rides ORDER BY startedAt DESC")
    fun getAllRides(): Flow<List<RideEntity>>

    @Query("SELECT * FROM rides WHERE id = :rideId")
    suspend fun getRideById(rideId: String): RideEntity?

    @Query("SELECT * FROM coordinates WHERE rideId = :rideId ORDER BY timestamp ASC")
    suspend fun getCoordinatesForRide(rideId: String): List<CoordinateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRide(ride: RideEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoordinates(coordinates: List<CoordinateEntity>)

    @Update
    suspend fun updateRide(ride: RideEntity)

    @Query("DELETE FROM rides WHERE id = :rideId")
    suspend fun deleteRide(rideId: String)

    @Query("SELECT SUM(distanceKm) FROM rides")
    suspend fun getTotalDistanceKm(): Double?

    @Query("SELECT SUM(durationSeconds) FROM rides")
    suspend fun getTotalDurationSeconds(): Long?

    @Query("SELECT COUNT(*) FROM rides")
    suspend fun getTotalRideCount(): Int

    @Query("SELECT MAX(maxSpeedKmh) FROM rides")
    suspend fun getAllTimeMaxSpeed(): Float?

    @Query("SELECT MAX(distanceKm) FROM rides")
    suspend fun getLongestRideKm(): Double?

    @Query("SELECT * FROM rides WHERE startedAt >= :fromTimestamp ORDER BY startedAt DESC")
    fun getRidesSince(fromTimestamp: Long): Flow<List<RideEntity>>
}
