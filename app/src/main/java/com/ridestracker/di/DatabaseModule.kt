package com.ridestracker.di

import android.content.Context
import androidx.room.Room
import com.ridestracker.data.local.AppDatabase
import com.ridestracker.data.local.dao.MaintenanceDao
import com.ridestracker.data.local.dao.RideDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "rides_tracker.db").build()

    @Provides fun provideRideDao(db: AppDatabase): RideDao = db.rideDao()

    @Provides fun provideMaintenanceDao(db: AppDatabase): MaintenanceDao = db.maintenanceDao()
}
