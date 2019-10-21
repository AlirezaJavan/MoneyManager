package com.javanapps.moneymanager.sms

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.net.toUri

/**
 * Helpers for requesting battery optimization whitelist and launching manufacturer-specific
 * "keep app alive" settings pages (Xiaomi AutoStart, Huawei Protected Apps, Samsung Sleeping Apps, etc.).
 */
object BatteryOptimizationHelper {
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    /** Launches the system dialog to request battery optimization whitelist. */
    @SuppressLint("BatteryLife")
    fun requestIgnoreBatteryOptimizations(context: Context) {
        val intent =
            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = "package:${context.packageName}".toUri()
            }
        context.startActivity(intent)
    }

    /**
     * Attempts to open the manufacturer-specific battery / AutoStart settings.
     * Returns true if a matching intent was found and launched.
     */
    fun openManufacturerBatterySettings(context: Context): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase()

        val intents =
            when {
                manufacturer.contains("xiaomi") || manufacturer.contains("redmi") ->
                    listOf(
                        Intent("miui.intent.action.APP_PERM_EDITOR").apply {
                            setClassName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
                        },
                        Intent("miui.intent.action.POWER_HIDE_MODE_APP_LIST").apply {
                            addCategory(Intent.CATEGORY_DEFAULT)
                        },
                    )
                manufacturer.contains("huawei") || manufacturer.contains("honor") ->
                    listOf(
                        Intent().apply {
                            setClassName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")
                        },
                        Intent().apply {
                            setClassName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")
                        },
                    )
                manufacturer.contains("samsung") ->
                    listOf(
                        Intent().apply {
                            setClassName("com.samsung.android.lool", "com.samsung.android.sm.battery.ui.BatteryActivity")
                        },
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = "package:${context.packageName}".toUri()
                        },
                    )
                manufacturer.contains("oppo") ->
                    listOf(
                        Intent().apply {
                            setClassName("com.coloros.safecenter", "com.coloros.privacypermissionsentry.PermissionTopActivity")
                        },
                    )
                manufacturer.contains("vivo") ->
                    listOf(
                        Intent().apply {
                            setClassName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")
                        },
                    )
                manufacturer.contains("oneplus") ->
                    listOf(
                        Intent().apply {
                            setClassName("com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity")
                        },
                    )
                else -> emptyList()
            }

        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (context.packageManager.resolveActivity(intent, 0) != null) {
                    context.startActivity(intent)
                    return true
                }
            } catch (_: Exception) {
            }
        }
        return false
    }
}
