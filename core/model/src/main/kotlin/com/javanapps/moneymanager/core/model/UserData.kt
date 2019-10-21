package com.javanapps.moneymanager.core.model

/** User/app preferences surfaced from DataStore. */
data class UserData(
    val migrationDone: Boolean,
    val isAppActive: Boolean,
    val biometricEnabled: Boolean,
    val smsServiceEnabled: Boolean,
    val darkThemeConfig: DarkThemeConfig,
)

enum class DarkThemeConfig {
    FOLLOW_SYSTEM,
    LIGHT,
    DARK,
}
