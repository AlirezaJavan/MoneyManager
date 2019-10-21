package com.javanapps.moneymanager.feature.transaction.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.javanapps.moneymanager.core.model.ShamsiDate
import com.javanapps.moneymanager.core.model.TransactionType
import org.junit.Rule
import org.junit.Test

class AddEditTransactionScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val defaultDate = ShamsiDate(1403, 1, 1)

    @Test
    fun addMode_showsAddTitle() {
        composeTestRule.setContent {
            AddEditTransactionScreen(
                uiState = AddEditTransactionUiState(date = defaultDate),
                onTypeChange = {},
                onAmountChange = {},
                onTitleChange = {},
                onNoteChange = {},
                onCategorySelected = {},
                onDateChange = {},
                onTimeChange = { _, _ -> },
                onSave = {},
                onDelete = {},
                onBack = {},
            )
        }

        composeTestRule.onNodeWithText("افزودن تراکنش").assertIsDisplayed()
        composeTestRule.onNodeWithText("ذخیره").assertIsDisplayed()
    }

    @Test
    fun editMode_showsEditTitle() {
        composeTestRule.setContent {
            AddEditTransactionScreen(
                uiState = AddEditTransactionUiState(editingId = 1L, date = defaultDate),
                onTypeChange = {},
                onAmountChange = {},
                onTitleChange = {},
                onNoteChange = {},
                onCategorySelected = {},
                onDateChange = {},
                onTimeChange = { _, _ -> },
                onSave = {},
                onDelete = {},
                onBack = {},
            )
        }

        composeTestRule.onNodeWithText("ویرایش تراکنش").assertIsDisplayed()
    }

    @Test
    fun amountField_isDisplayed() {
        composeTestRule.setContent {
            AddEditTransactionScreen(
                uiState = AddEditTransactionUiState(date = defaultDate, amountText = "۱٬۵۰۰"),
                onTypeChange = {},
                onAmountChange = {},
                onTitleChange = {},
                onNoteChange = {},
                onCategorySelected = {},
                onDateChange = {},
                onTimeChange = { _, _ -> },
                onSave = {},
                onDelete = {},
                onBack = {},
            )
        }

        composeTestRule.onNodeWithText("مبلغ (تومان)").assertIsDisplayed()
    }

    @Test
    fun amountError_isDisplayed() {
        composeTestRule.setContent {
            AddEditTransactionScreen(
                uiState = AddEditTransactionUiState(date = defaultDate, amountError = true),
                onTypeChange = {},
                onAmountChange = {},
                onTitleChange = {},
                onNoteChange = {},
                onCategorySelected = {},
                onDateChange = {},
                onTimeChange = { _, _ -> },
                onSave = {},
                onDelete = {},
                onBack = {},
            )
        }

        composeTestRule.onNodeWithText("مبلغ معتبر وارد کنید").assertIsDisplayed()
    }

    @Test
    fun typeChips_areDisplayed() {
        composeTestRule.setContent {
            AddEditTransactionScreen(
                uiState =
                    AddEditTransactionUiState(
                        date = defaultDate,
                        type = TransactionType.EXPENSE,
                    ),
                onTypeChange = {},
                onAmountChange = {},
                onTitleChange = {},
                onNoteChange = {},
                onCategorySelected = {},
                onDateChange = {},
                onTimeChange = { _, _ -> },
                onSave = {},
                onDelete = {},
                onBack = {},
            )
        }

        composeTestRule.onNodeWithText("مخارج").assertIsDisplayed()
        composeTestRule.onNodeWithText("درآمد").assertIsDisplayed()
    }
}
