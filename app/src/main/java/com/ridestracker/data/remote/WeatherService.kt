package com.ridestracker.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {

    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,weathercode,windspeed_10m,precipitation,relative_humidity_2m",
        @Query("wind_speed_unit") windSpeedUnit: String = "kmh",
        @Query("timezone") timezone: String = "auto"
    ): WeatherResponse
}

data class WeatherResponse(
    @SerializedName("current") val current: CurrentWeather
)

data class CurrentWeather(
    @SerializedName("temperature_2m") val temperatureCelsius: Float,
    @SerializedName("weathercode") val weatherCode: Int,
    @SerializedName("windspeed_10m") val windSpeedKmh: Float,
    @SerializedName("precipitation") val precipitationMm: Float,
    @SerializedName("relative_humidity_2m") val humidity: Int
)

fun Int.toWeatherLabel(): String = when (this) {
    0 -> "Clear sky"
    1, 2, 3 -> "Partly cloudy"
    45, 48 -> "Foggy"
    51, 53, 55 -> "Drizzle"
    61, 63, 65 -> "Rain"
    71, 73, 75 -> "Snow"
    80, 81, 82 -> "Rain showers"
    95 -> "Thunderstorm"
    else -> "Unknown"
}
