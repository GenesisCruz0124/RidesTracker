package com.ridestracker.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FormatUtil {

    fun formatDuration(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
    }

    fun formatDistance(km: Double, useImperial: Boolean = false): String {
        return if (useImperial) {
            val miles = DistanceUtil.kmToMiles(km)
            if (miles < 0.1) "%.0f ft".format(DistanceUtil.mToFeet(km * 1000))
            else "%.2f mi".format(miles)
        } else {
            if (km < 1.0) "%.0f m".format(km * 1000)
            else "%.2f km".format(km)
        }
    }

    fun formatSpeed(kmh: Float, useImperial: Boolean = false): String {
        return if (useImperial) "%.0f mph".format(kmh * 0.621371f)
        else "%.0f km/h".format(kmh)
    }

    fun formatElevation(meters: Float, useImperial: Boolean = false): String {
        return if (useImperial) "%.0f ft".format(DistanceUtil.mToFeet(meters.toDouble()))
        else "%.0f m".format(meters)
    }

    fun formatDate(timestamp: Long): String =
        SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(timestamp))

    fun formatDateShort(timestamp: Long): String =
        SimpleDateFormat("EEE MMM d", Locale.getDefault()).format(Date(timestamp))

    fun formatTime(timestamp: Long): String =
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
}
