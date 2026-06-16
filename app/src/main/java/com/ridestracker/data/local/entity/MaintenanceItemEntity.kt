package com.ridestracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ridestracker.domain.model.MaintenanceItem

@Entity(tableName = "maintenance_items")
data class MaintenanceItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val lastDoneKm: Double,
    val intervalKm: Double,
    val lastDoneDate: Long
)

fun MaintenanceItemEntity.toDomain() = MaintenanceItem(id, name, lastDoneKm, intervalKm, lastDoneDate)
fun MaintenanceItem.toEntity() = MaintenanceItemEntity(id, name, lastDoneKm, intervalKm, lastDoneDate)
