package com.javanapps.moneymanager.feature.settings.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.javanapps.moneymanager.core.data.repository.PreferencesRepository
import com.javanapps.moneymanager.core.domain.auth.ChangePasswordUseCase
import com.javanapps.moneymanager.core.model.DarkThemeConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val preferencesRepository: PreferencesRepository,
        private val changePasswordUseCase: ChangePasswordUseCase,
    ) : ViewModel() {
        private val message = MutableStateFlow<SettingsMessage?>(null)

        val uiState: StateFlow<SettingsUiState> =
            combine(preferencesRepository.userData, message) { data, msg ->
                SettingsUiState(
                    biometricEnabled = data.biometricEnabled,
                    smsServiceEnabled = data.smsServiceEnabled,
                    smsOverlayEnabled = data.smsOverlayEnabled,
                    darkThemeConfig = data.darkThemeConfig,
                    message = msg,
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

        fun setBiometricEnabled(enabled: Boolean) {
            viewModelScope.launch { preferencesRepository.setBiometricEnabled(enabled) }
        }

        fun setSmsServiceEnabled(enabled: Boolean) {
            viewModelScope.launch { preferencesRepository.setSmsServiceEnabled(enabled) }
        }

        fun setSmsOverlayEnabled(enabled: Boolean) {
            viewModelScope.launch { preferencesRepository.setSmsOverlayEnabled(enabled) }
        }

        fun setDarkThemeConfig(config: DarkThemeConfig) {
            viewModelScope.launch { preferencesRepository.setDarkThemeConfig(config) }
        }

        fun changePassword(
            oldPassword: String,
            newPassword: String,
        ) {
            message.value =
                if (changePasswordUseCase(oldPassword, newPassword)) {
                    SettingsMessage.PasswordChanged
                } else {
                    SettingsMessage.PasswordWrong
                }
        }

        fun consumeMessage() {
            message.value = null
        }
    }
