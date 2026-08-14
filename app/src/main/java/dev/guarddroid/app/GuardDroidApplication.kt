package dev.guarddroid.app

import android.app.Application
import androidx.work.Configuration
import androidx.hilt.work.HiltWorkerFactory
import dagger.hilt.android.HiltAndroidApp
import dev.guarddroid.core.update.UpdateNotificationManager
import dev.guarddroid.core.update.UpdateScheduler
import javax.inject.Inject

@HiltAndroidApp
class GuardDroidApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var updateScheduler: UpdateScheduler
    @Inject lateinit var updateNotificationManager: UpdateNotificationManager

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        updateNotificationManager.createChannel()
        updateScheduler.schedulePeriodicCheck()
    }
}
