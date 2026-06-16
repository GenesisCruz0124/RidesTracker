package com.ridestracker.ui.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridestracker.data.repository.RideRepository
import com.ridestracker.domain.model.Ride
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val rideRepository: RideRepository
) : ViewModel() {

    val rides: StateFlow<List<Ride>> = rideRepository.getAllRides()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteRide(rideId: String) {
        viewModelScope.launch { rideRepository.deleteRide(rideId) }
    }
}
