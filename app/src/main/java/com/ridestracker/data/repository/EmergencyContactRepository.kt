package com.ridestracker.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ridestracker.domain.model.EmergencyContact
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class EmergencyContactRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val nameKey = stringPreferencesKey("emergency_contact_name")
    private val phoneKey = stringPreferencesKey("emergency_contact_phone")

    val emergencyContact: Flow<EmergencyContact> = dataStore.data.map { prefs ->
        EmergencyContact(
            name = prefs[nameKey] ?: "",
            phoneNumber = prefs[phoneKey] ?: ""
        )
    }

    suspend fun saveEmergencyContact(name: String, phoneNumber: String) {
        dataStore.edit { prefs ->
            prefs[nameKey] = name
            prefs[phoneKey] = phoneNumber
        }
    }
}
