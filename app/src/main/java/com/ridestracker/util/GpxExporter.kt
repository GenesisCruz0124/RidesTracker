package com.ridestracker.util

import com.ridestracker.domain.model.Coordinate
import com.ridestracker.domain.model.Ride
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object GpxExporter {

    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun buildGpx(ride: Ride, coordinates: List<Coordinate>): String {
        val points = coordinates.joinToString("\n") { coord ->
            """        <trkpt lat="${coord.lat}" lon="${coord.lng}">
          <ele>${coord.altitude}</ele>
          <time>${isoFormat.format(Date(coord.timestamp))}</time>
          <extensions><speed>${coord.speed}</speed></extensions>
        </trkpt>"""
        }

        return """<?xml version="1.0" encoding="UTF-8"?>
<gpx version="1.1" creator="RidesTracker"
     xmlns="http://www.topografix.com/GPX/1/1">
  <metadata>
    <name>${ride.title}</name>
    <time>${isoFormat.format(Date(ride.startedAt))}</time>
  </metadata>
  <trk>
    <name>${ride.title}</name>
    <trkseg>
$points
    </trkseg>
  </trk>
</gpx>"""
    }
}
