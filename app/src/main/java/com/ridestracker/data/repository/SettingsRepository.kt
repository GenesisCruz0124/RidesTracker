package com.ridestracker.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ridestracker.domain.model.MapStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val mapStyleKey = stringPreferencesKey("map_style")

    val mapStyle: Flow<MapStyle> = dataStore.data.map { prefs ->
        runCatching { MapStyle.valueOf(prefs[mapStyleKey] ?: MapStyle.STANDARD.name) }
            .getOrDefault(MapStyle.STANDARD)
    }

    suspend fun setMapStyle(style: MapStyle) {
        dataStore.edit { prefs -> prefs[mapStyleKey] = style.name }
    }
}
