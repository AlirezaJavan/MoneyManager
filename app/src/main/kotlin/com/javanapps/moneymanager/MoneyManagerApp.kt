package com.javanapps.moneymanager

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.javanapps.moneymanager.core.data.repository.PreferencesRepository
import com.javanapps.moneymanager.sms.SmsForegroundService
import com.javanapps.moneymanager.sms.SmsHealthCheckWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltAndroidApp
class MoneyManagerApp :
    Application(),
    Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory

    @Inject lateinit var preferencesRepository: PreferencesRepository

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // React to SMS service toggle changes; starts/stops service and WorkManager health check
        preferencesRepository.userData
            .map { it.smsServiceEnabled }
            .distinctUntilChanged()
            .onEach { enabled ->
                val wm = WorkManager.getInstance(this)
                if (enabled) {
                    SmsForegroundService.start(this)
                    SmsHealthCheckWorker.scheduleHealthCheck(wm)
                } else {
                    SmsForegroundService.stop(this)
                    SmsHealthCheckWorker.cancelHealthCheck(wm)
                }
            }.launchIn(appScope)
    }

    override val workManagerConfiguration: Configuration
        get() =
            Configuration
                .Builder()
                .setWorkerFactory(workerFactory)
                .build()
}
