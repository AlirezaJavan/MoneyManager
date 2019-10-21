package com.javanapps.moneymanager.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.WorkManager
import com.javanapps.moneymanager.core.data.repository.PreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    @Inject lateinit var preferencesRepository: PreferencesRepository

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val action = intent.action
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userData = preferencesRepository.userData.first()
                if (userData.smsServiceEnabled) {
                    SmsForegroundService.start(context)
                    SmsHealthCheckWorker.scheduleHealthCheck(WorkManager.getInstance(context))
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
