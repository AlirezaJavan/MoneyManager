package com.javanapps.moneymanager.feature.auth.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class AuthScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun signUpMode_showsUsernameAndPasswordFields() {
        composeTestRule.setContent {
            AuthScreen(
                uiState = AuthUiState(mode = AuthMode.SIGN_UP),
                onUsernameChange = {},
                onPasswordChange = {},
                onConfirmPasswordChange = {},
                onSubmit = {},
                onBiometric = {},
            )
        }

        composeTestRule.onNodeWithText("نام کاربری").assertIsDisplayed()
        composeTestRule.onNodeWithText("رمز عبور").assertIsDisplayed()
        composeTestRule.onNodeWithText("تکرار رمز عبور").assertIsDisplayed()
        composeTestRule.onNodeWithText("ثبت و ورود").assertIsDisplayed()
    }

    @Test
    fun loginMode_showsOnlyPasswordField() {
        composeTestRule.setContent {
            AuthScreen(
                uiState = AuthUiState(mode = AuthMode.LOGIN),
                onUsernameChange = {},
                onPasswordChange = {},
                onConfirmPasswordChange = {},
                onSubmit = {},
                onBiometric = {},
            )
        }

        composeTestRule.onNodeWithText("رمز عبور").assertIsDisplayed()
        composeTestRule.onNodeWithText("ورود").assertIsDisplayed()
    }

    @Test
    fun loginMode_withBiometric_showsBiometricButton() {
        composeTestRule.setContent {
            AuthScreen(
                uiState = AuthUiState(mode = AuthMode.LOGIN, biometricEnabled = true),
                onUsernameChange = {},
                onPasswordChange = {},
                onConfirmPasswordChange = {},
                onSubmit = {},
                onBiometric = {},
            )
        }

        composeTestRule.onNodeWithText("ورود با اثر انگشت").assertIsDisplayed()
    }

    @Test
    fun passwordError_isDisplayed() {
        composeTestRule.setContent {
            AuthScreen(
                uiState = AuthUiState(mode = AuthMode.LOGIN, passwordError = R.string.feature_auth_impl_auth_error_password_wrong),
                onUsernameChange = {},
                onPasswordChange = {},
                onConfirmPasswordChange = {},
                onSubmit = {},
                onBiometric = {},
            )
        }

        composeTestRule.onNodeWithText("رمز عبور اشتباه است").assertIsDisplayed()
    }
}
