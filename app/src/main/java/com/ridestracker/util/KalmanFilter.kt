package com.ridestracker.util

class KalmanFilter(private val processNoise: Double = 3.0) {

    private var estimatedLat = 0.0
    private var estimatedLng = 0.0
    private var errorCovarianceLat = 1.0
    private var errorCovarianceLng = 1.0
    private var initialized = false

    fun filter(lat: Double, lng: Double, accuracy: Float): Pair<Double, Double> {
        val measurementNoise = accuracy.toDouble().coerceAtLeast(1.0)

        if (!initialized) {
            estimatedLat = lat
            estimatedLng = lng
            initialized = true
            return Pair(lat, lng)
        }

        errorCovarianceLat += processNoise
        errorCovarianceLng += processNoise

        val gainLat = errorCovarianceLat / (errorCovarianceLat + measurementNoise)
        val gainLng = errorCovarianceLng / (errorCovarianceLng + measurementNoise)

        estimatedLat += gainLat * (lat - estimatedLat)
        estimatedLng += gainLng * (lng - estimatedLng)

        errorCovarianceLat *= (1 - gainLat)
        errorCovarianceLng *= (1 - gainLng)

        return Pair(estimatedLat, estimatedLng)
    }

    fun reset() {
        initialized = false
        errorCovarianceLat = 1.0
        errorCovarianceLng = 1.0
    }
}
