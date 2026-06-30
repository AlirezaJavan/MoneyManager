package com.javanapps.moneymanager.ui

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.javanapps.moneymanager.core.data.repository.PreferencesRepository
import com.javanapps.moneymanager.core.model.DarkThemeConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

sealed interface AppUiState {
    data object Loading : AppUiState

    data class Ready(
        val isAppActive: Boolean,
        val loggedIn: Boolean,
        val migrationDone: Boolean,
        val darkThemeConfig: DarkThemeConfig =
            DarkThemeConfig.FOLLOW_SYSTEM,
    ) : AppUiState
}

@HiltViewModel
class AppViewModel
    @Inject
    constructor(
        preferencesRepository: PreferencesRepository,
    ) : ViewModel() {
        private val loggedIn = MutableStateFlow(false)
        private var lockJob: Job? = null

        private val backgroundLockObserver =
            object : DefaultLifecycleObserver {
                override fun onStop(owner: LifecycleOwner) {
                    lockJob =
                        viewModelScope.launch {
                            delay(BACKGROUND_LOCK_GRACE_PERIOD_MS.milliseconds)
                            loggedIn.value = false
                        }
                }

                override fun onStart(owner: LifecycleOwner) {
                    lockJob?.cancel()
                }
            }

        init {
            ProcessLifecycleOwner.get().lifecycle.addObserver(backgroundLockObserver)
        }

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

        override fun onCleared() {
            ProcessLifecycleOwner.get().lifecycle.removeObserver(backgroundLockObserver)
        }

        private companion object {
            const val BACKGROUND_LOCK_GRACE_PERIOD_MS = 10_000L
        }
    }
