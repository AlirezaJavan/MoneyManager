package com.javanapps.moneymanager.feature.settings.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.javanapps.moneymanager.core.domain.auth.ChangePasswordUseCase
import com.javanapps.moneymanager.core.model.DarkThemeConfig
import com.javanapps.moneymanager.core.testing.repository.TestAuthRepository
import com.javanapps.moneymanager.core.testing.repository.TestPreferencesRepository
import com.javanapps.moneymanager.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val preferencesRepository = TestPreferencesRepository()
    private val authRepository = TestAuthRepository()
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        viewModel =
            SettingsViewModel(
                preferencesRepository = preferencesRepository,
                changePasswordUseCase = ChangePasswordUseCase(authRepository),
            )
    }

    @Test
    fun uiState_reflectsPreferences() =
        runTest {
            viewModel.uiState.test {
                val initialState = awaitItem()
                assertThat(initialState.biometricEnabled).isFalse()

                preferencesRepository.setBiometricEnabled(true)
                val updatedState = awaitItem()
                assertThat(updatedState.biometricEnabled).isTrue()
            }
        }

    @Test
    fun setDarkThemeConfig_updatesPreferences() =
        runTest {
            viewModel.setDarkThemeConfig(DarkThemeConfig.DARK)

            viewModel.uiState.test {
                assertThat(expectMostRecentItem().darkThemeConfig).isEqualTo(DarkThemeConfig.DARK)
            }
        }

    @Test
    fun changePassword_updatesMessage_onSuccess() =
        runTest {
            authRepository.register("user", "old_pass")

            viewModel.changePassword("old_pass", "new_pass")

            viewModel.uiState.test {
                assertThat(expectMostRecentItem().message).isEqualTo(SettingsMessage.PasswordChanged)
            }
            assertThat(authRepository.verifyPassword("new_pass")).isTrue()
        }

    @Test
    fun changePassword_updatesMessage_onFailure() =
        runTest {
            authRepository.register("user", "old_pass")

            viewModel.changePassword("wrong_pass", "new_pass")

            viewModel.uiState.test {
                assertThat(expectMostRecentItem().message).isEqualTo(SettingsMessage.PasswordWrong)
            }
        }
}
