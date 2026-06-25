package com.ridestracker.ui.screen.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridestracker.data.repository.LifetimeStats
import com.ridestracker.data.repository.RideRepository
import com.ridestracker.ui.component.StatCard
import com.ridestracker.ui.theme.OrangeAccent
import com.ridestracker.ui.theme.TextSecondary
import com.ridestracker.util.FormatUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val rideRepository: RideRepository
) : ViewModel() {
    private val _stats = MutableStateFlow<LifetimeStats?>(null)
    val stats: StateFlow<LifetimeStats?> = _stats

    init {
        viewModelScope.launch { _stats.value = rideRepository.getLifetimeStats() }
    }
}

@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
    val stats by viewModel.stats.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Your Stats", style = MaterialTheme.typography.headlineMedium)

        stats?.let { s ->
            if (s.totalRides == 0) {
                Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Insights, null, modifier = Modifier.size(80.dp), tint = TextSecondary)
                        Spacer(Modifier.height(16.dp))
                        Text("No stats yet", style = MaterialTheme.typography.titleLarge, color = TextSecondary)
                        Text("Complete a ride to see your stats here", color = TextSecondary)
                    }
                }
                return@let
            }
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("All Time", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        StatCard("TOTAL KM", FormatUtil.formatDistance(s.totalDistanceKm))
                        StatCard("TOTAL RIDES", s.totalRides.toString())
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        StatCard("TOTAL TIME", FormatUtil.formatDuration(s.totalDurationSeconds))
                        StatCard("TOP SPEED", FormatUtil.formatSpeed(s.allTimeMaxSpeedKmh))
                    }
                }
            }

            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EmojiEvents, null, tint = OrangeAccent)
                        Spacer(Modifier.width(8.dp))
                        Text("Personal Records", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        StatCard("LONGEST", FormatUtil.formatDistance(s.longestRideKm))
                        StatCard("FASTEST", FormatUtil.formatSpeed(s.allTimeMaxSpeedKmh))
                    }
                }
            }
        } ?: run {
            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = OrangeAccent)
            }
        }
    }
}
