package com.ridestracker.util

import com.ridestracker.domain.model.Coordinate
import org.osmdroid.util.GeoPoint
import kotlin.math.*

object DistanceUtil {

    private const val EARTH_RADIUS_KM = 6371.0

    fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return EARTH_RADIUS_KM * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    fun totalDistanceKm(coordinates: List<Coordinate>): Double {
        if (coordinates.size < 2) return 0.0
        return coordinates.zipWithNext { a, b ->
            haversineKm(a.lat, a.lng, b.lat, b.lng)
        }.sum()
    }

    fun elevationGainLoss(coordinates: List<Coordinate>): Pair<Float, Float> {
        var gain = 0f
        var loss = 0f
        coordinates.zipWithNext { a, b ->
            val delta = (b.altitude - a.altitude).toFloat()
            if (delta > 0.5f) gain += delta else if (delta < -0.5f) loss += abs(delta)
        }
        return Pair(gain, loss)
    }

    fun toGeoPoint(coordinate: Coordinate) = GeoPoint(coordinate.lat, coordinate.lng)

    fun kmToMiles(km: Double): Double = km * 0.621371

    fun mToFeet(m: Double): Double = m * 3.28084

    private const val AVG_STRIDE_METERS = 0.75

    fun estimateSteps(distanceKm: Double): Int = ((distanceKm * 1000.0) / AVG_STRIDE_METERS).toInt()
}
