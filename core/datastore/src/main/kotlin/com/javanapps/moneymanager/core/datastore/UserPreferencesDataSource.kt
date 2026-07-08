package com.javanapps.moneymanager.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.javanapps.moneymanager.core.model.DarkThemeConfig
import com.javanapps.moneymanager.core.model.UserData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Reads/writes app preferences from a Preferences DataStore. Commands and queries are separated. */
class UserPreferencesDataSource
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
        val userData: Flow<UserData> =
            dataStore.data.map { prefs ->
                UserData(
                    migrationDone = prefs[Keys.MIGRATION_DONE] ?: false,
                    isAppActive = prefs[Keys.IS_APP_ACTIVE] ?: true,
                    biometricEnabled = prefs[Keys.BIOMETRIC_ENABLED] ?: false,
                    smsServiceEnabled = prefs[Keys.SMS_SERVICE_ENABLED] ?: true,
                    smsOverlayEnabled = prefs[Keys.SMS_OVERLAY_ENABLED] ?: true,
                    darkThemeConfig =
                        prefs[Keys.DARK_THEME]?.let(DarkThemeConfig::valueOf)
                            ?: DarkThemeConfig.FOLLOW_SYSTEM,
                )
            }

        val lastSelectedMonth: Flow<Pair<Int, Int>?> =
            dataStore.data.map { prefs ->
                val year = prefs[Keys.LAST_YEAR]
                val month = prefs[Keys.LAST_MONTH]
                if (year != null && month != null) year to month else null
            }

        suspend fun setMigrationDone(done: Boolean) = edit(Keys.MIGRATION_DONE, done)

        suspend fun setAppActive(active: Boolean) = edit(Keys.IS_APP_ACTIVE, active)

        suspend fun setBiometricEnabled(enabled: Boolean) = edit(Keys.BIOMETRIC_ENABLED, enabled)

        suspend fun setSmsServiceEnabled(enabled: Boolean) = edit(Keys.SMS_SERVICE_ENABLED, enabled)

        suspend fun setSmsOverlayEnabled(enabled: Boolean) = edit(Keys.SMS_OVERLAY_ENABLED, enabled)

        suspend fun setDarkThemeConfig(config: DarkThemeConfig) {
            dataStore.edit { it[Keys.DARK_THEME] = config.name }
        }

        suspend fun setLastSelectedMonth(
            year: Int,
            month: Int,
        ) {
            dataStore.edit {
                it[Keys.LAST_YEAR] = year
                it[Keys.LAST_MONTH] = month
            }
        }

        private suspend fun edit(
            key: Preferences.Key<Boolean>,
            value: Boolean,
        ) {
            dataStore.edit { it[key] = value }
        }

        private object Keys {
            val MIGRATION_DONE = booleanPreferencesKey("migration_done")
            val IS_APP_ACTIVE = booleanPreferencesKey("is_app_active")
            val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
            val SMS_SERVICE_ENABLED = booleanPreferencesKey("sms_service_enabled")
            val SMS_OVERLAY_ENABLED = booleanPreferencesKey("sms_overlay_enabled")
            val DARK_THEME = stringPreferencesKey("dark_theme_config")
            val LAST_YEAR = intPreferencesKey("last_selected_year")
            val LAST_MONTH = intPreferencesKey("last_selected_month")
        }
    }
