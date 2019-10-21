package com.javanapps.moneymanager.feature.migration.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class MigrationScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun idleState_withAutoDetect_showsAutoButton() {
        composeTestRule.setContent {
            MigrationScreen(
                uiState = MigrationUiState.Idle(autoDetectAvailable = true),
                onAutoDetect = {},
                onPickFile = {},
                onSkip = {},
            )
        }

        composeTestRule.onNodeWithText("انتقال اطلاعات نسخه قبلی").assertIsDisplayed()
        composeTestRule.onNodeWithText("تبدیل خودکار اطلاعات").assertIsDisplayed()
        composeTestRule.onNodeWithText("انتخاب فایل دیتابیس قدیمی").assertIsDisplayed()
        composeTestRule.onNodeWithText("بدون انتقال، ادامه بده").assertIsDisplayed()
    }

    @Test
    fun idleState_withoutAutoDetect_hidesAutoButton() {
        composeTestRule.setContent {
            MigrationScreen(
                uiState = MigrationUiState.Idle(autoDetectAvailable = false),
                onAutoDetect = {},
                onPickFile = {},
                onSkip = {},
            )
        }

        composeTestRule.onNodeWithText("انتخاب فایل دیتابیس قدیمی").assertIsDisplayed()
        composeTestRule.onNodeWithText("بدون انتقال، ادامه بده").assertIsDisplayed()
    }

    @Test
    fun successState_showsSuccessMessage() {
        composeTestRule.setContent {
            MigrationScreen(
                uiState = MigrationUiState.Success(transactions = 10, categories = 3),
                onAutoDetect = {},
                onPickFile = {},
                onSkip = {},
            )
        }

        composeTestRule.onNodeWithText("انتقال با موفقیت انجام شد").assertIsDisplayed()
    }

    @Test
    fun errorState_showsErrorMessage() {
        composeTestRule.setContent {
            MigrationScreen(
                uiState = MigrationUiState.Error("فایل پیدا نشد"),
                onAutoDetect = {},
                onPickFile = {},
                onSkip = {},
            )
        }

        composeTestRule.onNodeWithText("فایل پیدا نشد").assertIsDisplayed()
    }
}
