package com.ridestracker.data.repository

import com.ridestracker.data.local.dao.MaintenanceDao
import com.ridestracker.data.local.entity.toDomain
import com.ridestracker.data.local.entity.toEntity
import com.ridestracker.domain.model.FuelEntry
import com.ridestracker.domain.model.MaintenanceItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MaintenanceRepository @Inject constructor(private val dao: MaintenanceDao) {

    fun getAllItems(): Flow<List<MaintenanceItem>> =
        dao.getAllItems().map { it.map { e -> e.toDomain() } }

    suspend fun saveItem(item: MaintenanceItem) = dao.insertItem(item.toEntity())

    suspend fun updateItem(item: MaintenanceItem) = dao.updateItem(item.toEntity())

    suspend fun deleteItem(id: String) = dao.deleteItem(id)

    fun getAllFuelEntries(): Flow<List<FuelEntry>> =
        dao.getAllFuelEntries().map { it.map { e -> e.toDomain() } }

    suspend fun addFuelEntry(entry: FuelEntry) = dao.insertFuelEntry(entry.toEntity())

    suspend fun deleteFuelEntry(id: String) = dao.deleteFuelEntry(id)
}
