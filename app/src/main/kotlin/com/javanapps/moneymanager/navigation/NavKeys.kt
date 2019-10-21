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
) : NavKey

@Serializable
data object ExportNavKey : NavKey
