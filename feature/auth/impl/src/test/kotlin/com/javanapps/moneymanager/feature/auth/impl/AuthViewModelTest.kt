package com.javanapps.moneymanager.feature.auth.impl

import com.google.common.truth.Truth.assertThat
import com.javanapps.moneymanager.core.domain.auth.HasCredentialsUseCase
import com.javanapps.moneymanager.core.domain.auth.RegisterCredentialsUseCase
import com.javanapps.moneymanager.core.domain.auth.VerifyPasswordUseCase
import com.javanapps.moneymanager.core.testing.repository.TestAuthRepository
import com.javanapps.moneymanager.core.testing.repository.TestPreferencesRepository
import com.javanapps.moneymanager.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class AuthViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = TestAuthRepository()
    private val preferencesRepository = TestPreferencesRepository()
    private lateinit var viewModel: AuthViewModel

    private fun createViewModel() {
        viewModel =
            AuthViewModel(
                hasCredentials = HasCredentialsUseCase(authRepository),
                registerCredentials = RegisterCredentialsUseCase(authRepository),
                verifyPassword = VerifyPasswordUseCase(authRepository),
                preferencesRepository = preferencesRepository,
            )
    }

    @Test
    fun mode_isSignUp_whenNoCredentials() =
        runTest {
            authRepository.setHasCredentials(false)
            createViewModel()
            assertThat(viewModel.uiState.value.mode).isEqualTo(AuthMode.SIGN_UP)
        }

    @Test
    fun mode_isLogin_whenHasCredentials() =
        runTest {
            authRepository.setHasCredentials(true)
            createViewModel()
            assertThat(viewModel.uiState.value.mode).isEqualTo(AuthMode.LOGIN)
        }

    @Test
    fun signUp_succeeds_withValidInput() =
        runTest {
            authRepository.setHasCredentials(false)
            createViewModel()

            viewModel.onUsernameChange("user")
            viewModel.onPasswordChange("1234")
            viewModel.onConfirmPasswordChange("1234")
            viewModel.submit()

            assertThat(viewModel.uiState.value.authenticated).isTrue()
            assertThat(authRepository.hasCredentials()).isTrue()
        }

    @Test
    fun login_fails_withWrongPassword() =
        runTest {
            authRepository.setHasCredentials(true)
            authRepository.register("user", "1234")
            createViewModel()

            viewModel.onPasswordChange("wrong")
            viewModel.submit()

            assertThat(viewModel.uiState.value.authenticated).isFalse()
            assertThat(viewModel.uiState.value.passwordError).isNotNull()
        }
}
