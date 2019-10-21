package com.javanapps.moneymanager.feature.auth.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.javanapps.moneymanager.core.data.repository.PreferencesRepository
import com.javanapps.moneymanager.core.domain.auth.HasCredentialsUseCase
import com.javanapps.moneymanager.core.domain.auth.RegisterCredentialsUseCase
import com.javanapps.moneymanager.core.domain.auth.VerifyPasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val MIN_PASSWORD_LENGTH = 4

@HiltViewModel
class AuthViewModel
    @Inject
    constructor(
        hasCredentials: HasCredentialsUseCase,
        private val registerCredentials: RegisterCredentialsUseCase,
        private val verifyPassword: VerifyPasswordUseCase,
        private val preferencesRepository: PreferencesRepository,
    ) : ViewModel() {
        private val _uiState =
            MutableStateFlow(
                AuthUiState(mode = if (hasCredentials()) AuthMode.LOGIN else AuthMode.SIGN_UP),
            )
        val uiState = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                preferencesRepository.userData.collect { data ->
                    _uiState.update { it.copy(biometricEnabled = data.biometricEnabled) }
                }
            }
        }

        fun onUsernameChange(value: String) = _uiState.update { it.copy(username = value, usernameError = null) }

        fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, passwordError = null) }

        fun onConfirmPasswordChange(value: String) = _uiState.update { it.copy(confirmPassword = value, confirmError = null) }

        fun submit() {
            val state = _uiState.value
            when (state.mode) {
                AuthMode.SIGN_UP -> submitSignUp(state)
                AuthMode.LOGIN -> submitLogin(state)
            }
        }

        private fun submitSignUp(state: AuthUiState) {
            val usernameError =
                if (state.username.isBlank()) R.string.feature_auth_impl_auth_error_username_blank else null
            val passwordError = passwordError(state.password)
            val confirmError =
                when {
                    state.confirmPassword.isBlank() -> R.string.feature_auth_impl_auth_error_confirm_blank
                    state.confirmPassword != state.password -> R.string.feature_auth_impl_auth_error_confirm_mismatch
                    else -> null
                }
            if (usernameError != null || passwordError != null || confirmError != null) {
                _uiState.update {
                    it.copy(
                        usernameError = usernameError,
                        passwordError = passwordError,
                        confirmError = confirmError,
                    )
                }
                return
            }
            registerCredentials(state.username, state.password)
            _uiState.update { it.copy(authenticated = true) }
        }

        private fun submitLogin(state: AuthUiState) {
            val passwordError = passwordError(state.password)
            if (passwordError != null) {
                _uiState.update { it.copy(passwordError = passwordError) }
                return
            }
            if (verifyPassword(state.password)) {
                _uiState.update { it.copy(authenticated = true) }
            } else {
                _uiState.update { it.copy(passwordError = R.string.feature_auth_impl_auth_error_password_wrong) }
            }
        }

        private fun passwordError(password: String): Int? =
            when {
                password.isBlank() -> R.string.feature_auth_impl_auth_error_password_blank
                password.length < MIN_PASSWORD_LENGTH -> R.string.feature_auth_impl_auth_error_password_short
                else -> null
            }
    }
