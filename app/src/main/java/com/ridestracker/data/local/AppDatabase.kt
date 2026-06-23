package com.ridestracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ridestracker.data.local.dao.MaintenanceDao
import com.ridestracker.data.local.dao.RideDao
import com.ridestracker.data.local.entity.CoordinateEntity
import com.ridestracker.data.local.entity.FuelEntryEntity
import com.ridestracker.data.local.entity.MaintenanceItemEntity
import com.ridestracker.data.local.entity.RideEntity

@Database(
    entities = [
        RideEntity::class,
        CoordinateEntity::class,
        MaintenanceItemEntity::class,
        FuelEntryEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun rideDao(): RideDao
    abstract fun maintenanceDao(): MaintenanceDao
}
