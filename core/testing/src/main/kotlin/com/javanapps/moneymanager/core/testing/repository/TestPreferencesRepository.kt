package com.javanapps.moneymanager.core.testing.repository

import com.javanapps.moneymanager.core.data.repository.PreferencesRepository
import com.javanapps.moneymanager.core.model.DarkThemeConfig
import com.javanapps.moneymanager.core.model.UserData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TestPreferencesRepository : PreferencesRepository {
    private val defaultUserData =
        UserData(
            migrationDone = false,
            isAppActive = true,
            biometricEnabled = false,
            smsServiceEnabled = true,
            smsOverlayEnabled = true,
            darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
        )

    private val state = MutableStateFlow(defaultUserData)
    private val lastMonth = MutableStateFlow<Pair<Int, Int>?>(null)

    override val userData: Flow<UserData> = state.asStateFlow()
    override val lastSelectedMonth: Flow<Pair<Int, Int>?> = lastMonth.asStateFlow()

    override suspend fun setMigrationDone(done: Boolean) {
        state.value = state.value.copy(migrationDone = done)
    }

    override suspend fun setAppActive(active: Boolean) {
        state.value = state.value.copy(isAppActive = active)
    }

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        state.value = state.value.copy(biometricEnabled = enabled)
    }

    override suspend fun setSmsServiceEnabled(enabled: Boolean) {
        state.value = state.value.copy(smsServiceEnabled = enabled)
    }

    override suspend fun setSmsOverlayEnabled(enabled: Boolean) {
        state.value = state.value.copy(smsOverlayEnabled = enabled)
    }

    override suspend fun setDarkThemeConfig(config: DarkThemeConfig) {
        state.value = state.value.copy(darkThemeConfig = config)
    }

    override suspend fun setLastSelectedMonth(
        year: Int,
        month: Int,
    ) {
        lastMonth.value = year to month
    }
}
