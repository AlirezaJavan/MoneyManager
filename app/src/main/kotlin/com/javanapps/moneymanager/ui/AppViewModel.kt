package com.javanapps.moneymanager.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.javanapps.moneymanager.core.data.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface AppUiState {
    data object Loading : AppUiState

    data class Ready(
        val isAppActive: Boolean,
        val loggedIn: Boolean,
        val migrationDone: Boolean,
        val darkThemeConfig: com.javanapps.moneymanager.core.model.DarkThemeConfig =
            com.javanapps.moneymanager.core.model.DarkThemeConfig.FOLLOW_SYSTEM,
    ) : AppUiState
}

@HiltViewModel
class AppViewModel
    @Inject
    constructor(
        preferencesRepository: PreferencesRepository,
    ) : ViewModel() {
        private val loggedIn = MutableStateFlow(false)

        val uiState: StateFlow<AppUiState> =
            combine(preferencesRepository.userData, loggedIn) { data, logged ->
                AppUiState.Ready(
                    isAppActive = data.isAppActive,
                    loggedIn = logged,
                    migrationDone = data.migrationDone,
                    darkThemeConfig = data.darkThemeConfig,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = AppUiState.Loading,
            )

        fun onLoggedIn() {
            loggedIn.value = true
        }
    }
