package com.ridestracker.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridestracker.data.repository.SettingsRepository
import com.ridestracker.domain.model.MapStyle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val mapStyle: StateFlow<MapStyle> = settingsRepository.mapStyle
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MapStyle.STANDARD)

    fun selectMapStyle(style: MapStyle) {
        viewModelScope.launch { settingsRepository.setMapStyle(style) }
    }
}
