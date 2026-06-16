package com.ridestracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ridestracker.domain.model.FuelEntry

@Entity(tableName = "fuel_entries")
data class FuelEntryEntity(
    @PrimaryKey val id: String,
    val date: Long,
    val odometerKm: Double,
    val liters: Float,
    val costTotal: Float
)

fun FuelEntryEntity.toDomain() = FuelEntry(id, date, odometerKm, liters, costTotal)
fun FuelEntry.toEntity() = FuelEntryEntity(id, date, odometerKm, liters, costTotal)
