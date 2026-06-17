package com.ridestracker.ui.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.ui.viewinterop.AndroidView
import com.ridestracker.domain.model.Coordinate
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import java.io.File

@Composable
fun RouteMapView(
    coordinates: List<Coordinate>,
    modifier: Modifier = Modifier
) {
    if (coordinates.size < 2) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Route, null)
                Text("No route recorded", style = MaterialTheme.typography.bodyMedium)
            }
        }
        return
    }

    val points = remember(coordinates) { coordinates.map { GeoPoint(it.lat, it.lng) } }

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(16.dp)),
        factory = { ctx ->
            Configuration.getInstance().apply {
                userAgentValue = ctx.packageName
                val cacheDir = File(ctx.cacheDir, "osmdroid")
                osmdroidBasePath = cacheDir
                osmdroidTileCache = File(cacheDir, "tiles")
            }
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                isTilesScaledToDpi = true

                val polyline = Polyline().apply {
                    outlinePaint.color = 0xFFFF6B00.toInt()
                    outlinePaint.strokeWidth = 10f
                    setPoints(points)
                }
                overlays.add(polyline)

                onResume()
                controller.setZoom(16.0)
                controller.setCenter(points.first())

                post {
                    runCatching {
                        val bounds = BoundingBox.fromGeoPoints(points)
                        if (bounds.latNorth != bounds.latSouth || bounds.lonEast != bounds.lonWest) {
                            zoomToBoundingBox(bounds, false, 64)
                        }
                    }
                }
            }
        },
        update = { mapView ->
            mapView.overlays.filterIsInstance<Polyline>().forEach { it.setPoints(points) }
            mapView.invalidate()
        }
    )
}
