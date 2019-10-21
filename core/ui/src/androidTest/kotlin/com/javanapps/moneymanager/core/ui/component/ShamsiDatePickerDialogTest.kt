package com.javanapps.moneymanager.core.ui.component

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.javanapps.moneymanager.core.model.ShamsiDate
import org.junit.Rule
import org.junit.Test

class ShamsiDatePickerDialogTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun dialog_isDisplayed() {
        composeTestRule.setContent {
            ShamsiDatePickerDialog(
                initialDate = ShamsiDate(1403, 6, 15),
                onConfirm = {},
                onDismiss = {},
            )
        }

        composeTestRule.onNodeWithText("تأیید").assertIsDisplayed()
        composeTestRule.onNodeWithText("انصراف").assertIsDisplayed()
    }
}
