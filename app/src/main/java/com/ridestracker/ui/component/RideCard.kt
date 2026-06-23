package com.ridestracker.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ridestracker.domain.model.Ride
import com.ridestracker.domain.model.VehicleType
import com.ridestracker.ui.theme.OrangeAccent
import com.ridestracker.ui.theme.SurfaceDark
import com.ridestracker.ui.theme.TextSecondary
import com.ridestracker.util.FormatUtil

@Composable
fun RideCard(ride: Ride, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (ride.vehicleType) {
                    VehicleType.MOTORCYCLE -> Icons.Default.TwoWheeler
                    VehicleType.WALKING -> Icons.AutoMirrored.Filled.DirectionsWalk
                    VehicleType.BICYCLE -> Icons.Default.DirectionsBike
                },
                contentDescription = null,
                tint = OrangeAccent,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(text = ride.title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = FormatUtil.formatDate(ride.startedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = FormatUtil.formatDistance(ride.distanceKm),
                    style = MaterialTheme.typography.titleMedium,
                    color = OrangeAccent
                )
                Text(
                    text = FormatUtil.formatDuration(ride.durationSeconds),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                if (ride.vehicleType == VehicleType.WALKING) {
                    Text(
                        text = "${ride.steps} steps",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
