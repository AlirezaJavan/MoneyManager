package com.javanapps.moneymanager.feature.settings.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.javanapps.moneymanager.core.model.DarkThemeConfig
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val defaultState =
        SettingsUiState(
            biometricEnabled = false,
            smsServiceEnabled = true,
            darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
        )

    @Test
    fun showsSettingsSections() {
        composeTestRule.setContent {
            SettingsScreen(
                uiState = defaultState,
                onBiometricChange = {},
                onSmsServiceChange = {},
                onThemeChange = {},
                onChangePassword = { _, _ -> },
                onMessageShown = {},
                onOpenSmsSettings = {},
            )
        }

        composeTestRule.onNodeWithText("تنظیمات").assertIsDisplayed()
        composeTestRule.onNodeWithText("ورود با اثر انگشت").assertIsDisplayed()
        composeTestRule.onNodeWithText("تشخیص خودکار پیامک‌های بانکی").assertIsDisplayed()
        composeTestRule.onNodeWithText("پوسته برنامه").assertIsDisplayed()
    }

    @Test
    fun themeOptions_areDisplayed() {
        composeTestRule.setContent {
            SettingsScreen(
                uiState = defaultState,
                onBiometricChange = {},
                onSmsServiceChange = {},
                onThemeChange = {},
                onChangePassword = { _, _ -> },
                onMessageShown = {},
                onOpenSmsSettings = {},
            )
        }

        composeTestRule.onNodeWithText("هماهنگ با سیستم").assertIsDisplayed()
        composeTestRule.onNodeWithText("روشن").assertIsDisplayed()
        composeTestRule.onNodeWithText("تاریک").assertIsDisplayed()
    }

    @Test
    fun successMessage_isDisplayed() {
        composeTestRule.setContent {
            SettingsScreen(
                uiState = defaultState.copy(message = SettingsMessage.PasswordChanged),
                onBiometricChange = {},
                onSmsServiceChange = {},
                onThemeChange = {},
                onChangePassword = { _, _ -> },
                onMessageShown = {},
                onOpenSmsSettings = {},
            )
        }

        composeTestRule.onNodeWithText("رمز عبور با موفقیت تغییر یافت").assertIsDisplayed()
    }
}
