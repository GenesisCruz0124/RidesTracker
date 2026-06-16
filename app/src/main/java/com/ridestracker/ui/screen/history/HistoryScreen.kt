package com.ridestracker.ui.screen.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ridestracker.ui.component.RideCard
import com.ridestracker.ui.theme.OrangeAccent
import com.ridestracker.ui.theme.TextSecondary

@Composable
fun HistoryScreen(
    onRideClick: (String) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val rides by viewModel.rides.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Ride History",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        if (rides.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.DirectionsBike, null,
                        modifier = Modifier.size(80.dp), tint = TextSecondary
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("No rides yet", style = MaterialTheme.typography.titleLarge, color = TextSecondary)
                    Text("Start your first ride on the Track tab", color = TextSecondary)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(rides, key = { it.id }) { ride ->
                    RideCard(ride = ride, onClick = { onRideClick(ride.id) })
                }
            }
        }
    }
}
