package com.javanapps.moneymanager.sms

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.provider.Telephony
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.javanapps.moneymanager.R
import com.javanapps.moneymanager.core.common.calendar.ShamsiCalendar
import com.javanapps.moneymanager.core.data.DefaultCategories
import com.javanapps.moneymanager.core.data.repository.BankSmsRuleRepository
import com.javanapps.moneymanager.core.data.repository.TransactionRepository
import com.javanapps.moneymanager.core.data.sms.SmsHeuristicParser
import com.javanapps.moneymanager.core.model.ParsedSms
import com.javanapps.moneymanager.core.model.Transaction
import com.javanapps.moneymanager.core.model.TransactionSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class SmsForegroundService : Service() {
    @Inject lateinit var parser: SmsHeuristicParser

    @Inject lateinit var ruleRepository: BankSmsRuleRepository

    @Inject lateinit var transactionRepository: TransactionRepository

    @Inject lateinit var smsOverlayManager: SmsOverlayManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val smsReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(
                context: Context,
                intent: Intent,
            ) {
                if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
                val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                if (messages.isNullOrEmpty()) return
                val sender = messages[0].originatingAddress ?: ""
                val body = messages.joinToString("") { it.messageBody }
                handleSms(sender, body)
            }
        }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        startForeground(NOTIFICATION_ID, buildForegroundNotification())
        val filter =
            IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION).apply {
                priority = 999
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(smsReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(smsReceiver, filter)
        }
    }

    private fun handleSms(
        sender: String,
        body: String,
    ) {
        serviceScope.launch {
            val rules = ruleRepository.enabledRules()
            val parsed = parser.parse(body, sender, rules) ?: return@launch

            // Save as a pending transaction
            val id =
                transactionRepository.add(
                    Transaction(
                        id = Transaction.NO_ID,
                        amountToman = parsed.amountToman,
                        type = parsed.type,
                        categoryName = DefaultCategories.MISC,
                        title = parsed.bankName,
                        note = "",
                        date = ShamsiCalendar.now(),
                        createdAtEpochMillis = System.currentTimeMillis(),
                        source = TransactionSource.SMS,
                        isPending = true,
                    ),
                )
            withContext(Dispatchers.Main) {
                if (smsOverlayManager.canShow()) {
                    smsOverlayManager.show(id, parsed, serviceScope)
                } else {
                    showConfirmationNotification(id, parsed)
                }
            }
        }
    }

    private fun showConfirmationNotification(
        transactionId: Long,
        parsed: ParsedSms,
    ) {
        val intent = SmsConfirmActivity.createIntent(this, transactionId, parsed)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent =
            PendingIntent.getActivity(
                this,
                transactionId.toInt(), // Use transactionId for uniqueness
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        val notification =
            NotificationCompat
                .Builder(this, CHANNEL_ID_ALERT)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(getString(R.string.sms_alert_notification_title))
                .setContentText(getString(R.string.sms_alert_notification_text, parsed.amountToman.toString()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(pendingIntent, true)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(transactionId.toInt(), notification)
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int = START_STICKY

    override fun onDestroy() {
        smsOverlayManager.dismiss()
        unregisterReceiver(smsReceiver)
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)

        // Persistent channel for the foreground service
        val serviceChannel =
            NotificationChannel(
                CHANNEL_ID_SERVICE,
                getString(R.string.sms_service_channel_name),
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = getString(R.string.sms_service_channel_description)
            }
        manager.createNotificationChannel(serviceChannel)

        // High importance channel for the confirmation popup
        val alertChannel =
            NotificationChannel(
                CHANNEL_ID_ALERT,
                getString(R.string.sms_alert_channel_name),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = getString(R.string.sms_alert_channel_description)
                enableVibration(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
        manager.createNotificationChannel(alertChannel)
    }

    private fun buildForegroundNotification(): Notification =
        NotificationCompat
            .Builder(this, CHANNEL_ID_SERVICE)
            .setContentTitle(getString(R.string.sms_service_notification_title))
            .setContentText(getString(R.string.sms_service_notification_text))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

    companion object {
        private const val CHANNEL_ID_SERVICE = "sms_monitor"
        private const val CHANNEL_ID_ALERT = "sms_alerts"
        private const val NOTIFICATION_ID = 1001
        private const val NOTIFICATION_ID_ALERT = 1002

        fun start(context: Context) {
            val intent = Intent(context, SmsForegroundService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, SmsForegroundService::class.java))
        }
    }
}
