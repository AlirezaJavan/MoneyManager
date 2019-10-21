package com.javanapps.moneymanager.feature.sms.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.javanapps.moneymanager.core.model.BankSmsRule
import org.junit.Rule
import org.junit.Test

class SmsScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsTeachSection() {
        composeTestRule.setContent {
            SmsScreen(
                uiState = SmsUiState(),
                onBodyChange = {},
                onSenderChange = {},
                onBankNameChange = {},
                onPreview = {},
                onSaveRule = {},
                onResetTeach = {},
                onDeleteRule = {},
                onToggleRule = {},
            )
        }

        composeTestRule.onNodeWithText("مدیریت پیامک‌های بانکی").assertIsDisplayed()
    }

    @Test
    fun withRules_showsRulesList() {
        val rule =
            BankSmsRule(
                id = 1L,
                bankName = "ملت",
                senderPattern = "Melat",
                incomeKeywords = emptyList(),
                expenseKeywords = emptyList(),
                amountInRial = false,
                defaultCategory = "",
                sampleBody = "",
                enabled = true,
            )
        composeTestRule.setContent {
            SmsScreen(
                uiState = SmsUiState(rules = listOf(rule)),
                onBodyChange = {},
                onSenderChange = {},
                onBankNameChange = {},
                onPreview = {},
                onSaveRule = {},
                onResetTeach = {},
                onDeleteRule = {},
                onToggleRule = {},
            )
        }

        composeTestRule.onNodeWithText("ملت").assertIsDisplayed()
    }
}
