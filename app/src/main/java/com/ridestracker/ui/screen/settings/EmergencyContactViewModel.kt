package com.ridestracker.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridestracker.data.repository.EmergencyContactRepository
import com.ridestracker.domain.model.EmergencyContact
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmergencyContactViewModel @Inject constructor(
    private val repository: EmergencyContactRepository
) : ViewModel() {

    val emergencyContact: StateFlow<EmergencyContact> = repository.emergencyContact
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EmergencyContact())

    fun save(name: String, phoneNumber: String) {
        viewModelScope.launch {
            repository.saveEmergencyContact(name, phoneNumber)
        }
    }
}
