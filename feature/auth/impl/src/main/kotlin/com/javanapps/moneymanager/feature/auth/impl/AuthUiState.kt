package com.javanapps.moneymanager.feature.auth.impl

import androidx.annotation.StringRes

/** Whether the auth screen is creating credentials or logging in. */
enum class AuthMode { SIGN_UP, LOGIN }

data class AuthUiState(
    val mode: AuthMode = AuthMode.LOGIN,
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    @StringRes val usernameError: Int? = null,
    @StringRes val passwordError: Int? = null,
    @StringRes val confirmError: Int? = null,
    val biometricEnabled: Boolean = false,
    val authenticated: Boolean = false,
) {
    val canUseBiometric: Boolean get() = mode == AuthMode.LOGIN && biometricEnabled
}
