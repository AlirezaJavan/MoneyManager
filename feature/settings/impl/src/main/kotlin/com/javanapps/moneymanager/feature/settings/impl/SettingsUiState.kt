package com.javanapps.moneymanager.feature.settings.impl

import com.javanapps.moneymanager.core.model.DarkThemeConfig

enum class SettingsMessage { PasswordChanged, PasswordWrong }

data class SettingsUiState(
    val biometricEnabled: Boolean = false,
    val smsServiceEnabled: Boolean = true,
    val darkThemeConfig: DarkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
    val message: SettingsMessage? = null,
)
