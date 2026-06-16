package com.ridestracker.data.local.dao

import androidx.room.*
import com.ridestracker.data.local.entity.FuelEntryEntity
import com.ridestracker.data.local.entity.MaintenanceItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceDao {

    @Query("SELECT * FROM maintenance_items ORDER BY name ASC")
    fun getAllItems(): Flow<List<MaintenanceItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: MaintenanceItemEntity)

    @Update
    suspend fun updateItem(item: MaintenanceItemEntity)

    @Query("DELETE FROM maintenance_items WHERE id = :id")
    suspend fun deleteItem(id: String)

    @Query("SELECT * FROM fuel_entries ORDER BY date DESC")
    fun getAllFuelEntries(): Flow<List<FuelEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFuelEntry(entry: FuelEntryEntity)

    @Query("DELETE FROM fuel_entries WHERE id = :id")
    suspend fun deleteFuelEntry(id: String)

    @Query("SELECT SUM(liters) FROM fuel_entries")
    suspend fun getTotalLiters(): Float?

    @Query("SELECT SUM(costTotal) FROM fuel_entries")
    suspend fun getTotalFuelCost(): Float?
}
