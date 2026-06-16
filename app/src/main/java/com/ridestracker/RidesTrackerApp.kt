package com.ridestracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class RidesTrackerApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_RIDE_TRACKING,
                    getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW
                ).apply { description = getString(R.string.notification_channel_description) }
            )

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_SOS,
                    getString(R.string.sos_channel_name),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = getString(R.string.sos_channel_description) }
            )

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_MAINTENANCE,
                    getString(R.string.maintenance_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }
    }

    companion object {
        const val CHANNEL_RIDE_TRACKING = "ride_tracking"
        const val CHANNEL_SOS = "sos_alerts"
        const val CHANNEL_MAINTENANCE = "maintenance_reminders"
    }
}
