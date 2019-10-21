package com.javanapps.moneymanager.firebase

import android.annotation.SuppressLint
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.javanapps.moneymanager.core.data.repository.PreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Handles incoming FCM messages. The only supported action is the remote kill-switch:
 * sending {"action":"kill"} deactivates the app; {"action":"activate"} re-enables it.
 */
@SuppressLint("MissingFirebaseInstanceTokenRefresh")
@AndroidEntryPoint
class KillSwitchMessagingService : FirebaseMessagingService() {
    @Inject lateinit var preferencesRepository: PreferencesRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(message: RemoteMessage) {
        val action = message.data["action"] ?: return
        serviceScope.launch {
            when (action) {
                "kill" -> preferencesRepository.setAppActive(false)
                "activate" -> preferencesRepository.setAppActive(true)
            }
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
