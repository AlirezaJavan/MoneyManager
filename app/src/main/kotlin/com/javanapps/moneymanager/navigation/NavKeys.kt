package com.javanapps.moneymanager.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object HomeNavKey : NavKey

@Serializable
data object ReportsNavKey : NavKey

@Serializable
data object CategoriesNavKey : NavKey

@Serializable
data object SettingsNavKey : NavKey

@Serializable
data object SmsNavKey : NavKey

@Serializable
data class AddEditTransactionNavKey(
    val transactionId: Long? = null,
    // Distinguishes separate "add new" pushes (which all have transactionId == null) so each
    // gets its own back stack entry / ViewModel instead of colliding on data class equality and
    // reusing stale form state from a previous add session.
    val sessionId: Long = System.nanoTime(),
) : NavKey

@Serializable
data object ExportNavKey : NavKey
