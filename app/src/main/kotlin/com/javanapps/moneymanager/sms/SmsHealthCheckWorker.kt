package com.javanapps.moneymanager.sms

import android.app.ActivityManager
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.javanapps.moneymanager.core.data.repository.PreferencesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

/**
 * Periodic WorkManager task that checks every 15 minutes whether [SmsForegroundService] is running.
 * If SMS monitoring is enabled and the service is dead, restarts it.
 * This provides a resilient revival mechanism on top of START_STICKY and boot broadcast.
 */
@HiltWorker
class SmsHealthCheckWorker
    @AssistedInject
    constructor(
        @Assisted context: Context,
        @Assisted workerParams: WorkerParameters,
        private val preferencesRepository: PreferencesRepository,
    ) : Worker(context, workerParams) {
        override fun doWork(): Result {
            val enabled = runBlocking { preferencesRepository.userData.first().smsServiceEnabled }
            if (!enabled) return Result.success()

            if (!isServiceRunning(applicationContext)) {
                SmsForegroundService.start(applicationContext)
            }
            return Result.success()
        }

        @Suppress("DEPRECATION")
        private fun isServiceRunning(context: Context): Boolean {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            return activityManager
                .getRunningServices(Int.MAX_VALUE)
                .any { it.service.className == SmsForegroundService::class.java.name }
        }

        companion object {
            private const val WORK_NAME = "SmsHealthCheck"

            fun scheduleHealthCheck(workManager: WorkManager) {
                val request =
                    PeriodicWorkRequestBuilder<SmsHealthCheckWorker>(
                        15,
                        TimeUnit.MINUTES,
                    ).setConstraints(
                        Constraints.Builder().build(),
                    ).build()

                workManager.enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request,
                )
            }

            fun cancelHealthCheck(workManager: WorkManager) {
                workManager.cancelUniqueWork(WORK_NAME)
            }
        }
    }
