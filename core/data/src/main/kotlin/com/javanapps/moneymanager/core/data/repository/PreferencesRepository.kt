package com.javanapps.moneymanager.core.data.repository

import com.javanapps.moneymanager.core.datastore.UserPreferencesDataSource
import com.javanapps.moneymanager.core.model.DarkThemeConfig
import com.javanapps.moneymanager.core.model.UserData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface PreferencesRepository {
    val userData: Flow<UserData>
    val lastSelectedMonth: Flow<Pair<Int, Int>?>

    suspend fun setMigrationDone(done: Boolean)

    suspend fun setAppActive(active: Boolean)

    suspend fun setBiometricEnabled(enabled: Boolean)

    suspend fun setSmsServiceEnabled(enabled: Boolean)

    suspend fun setSmsOverlayEnabled(enabled: Boolean)

    suspend fun setDarkThemeConfig(config: DarkThemeConfig)

    suspend fun setLastSelectedMonth(
        year: Int,
        month: Int,
    )
}

@Singleton
internal class DefaultPreferencesRepository
    @Inject
    constructor(
        private val dataSource: UserPreferencesDataSource,
    ) : PreferencesRepository {
        override val userData: Flow<UserData> = dataSource.userData
        override val lastSelectedMonth: Flow<Pair<Int, Int>?> = dataSource.lastSelectedMonth

        override suspend fun setMigrationDone(done: Boolean) = dataSource.setMigrationDone(done)

        override suspend fun setAppActive(active: Boolean) = dataSource.setAppActive(active)

        override suspend fun setBiometricEnabled(enabled: Boolean) = dataSource.setBiometricEnabled(enabled)

        override suspend fun setSmsServiceEnabled(enabled: Boolean) = dataSource.setSmsServiceEnabled(enabled)

        override suspend fun setSmsOverlayEnabled(enabled: Boolean) = dataSource.setSmsOverlayEnabled(enabled)

        override suspend fun setDarkThemeConfig(config: DarkThemeConfig) = dataSource.setDarkThemeConfig(config)

        override suspend fun setLastSelectedMonth(
            year: Int,
            month: Int,
        ) = dataSource.setLastSelectedMonth(year, month)
    }
