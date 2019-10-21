package com.javanapps.moneymanager.feature.categories.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.javanapps.moneymanager.core.model.Category
import com.javanapps.moneymanager.core.model.TransactionType
import org.junit.Rule
import org.junit.Test

class CategoriesScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsCategories() {
        val state =
            CategoriesUiState(
                type = TransactionType.EXPENSE,
                categories =
                    listOf(
                        Category(1L, "خوراک", TransactionType.EXPENSE),
                        Category(2L, "حمل‌ونقل", TransactionType.EXPENSE),
                    ),
            )

        composeTestRule.setContent {
            CategoriesScreen(
                uiState = state,
                deletionRequest = null,
                onTypeChange = {},
                onAdd = {},
                onRename = { _, _ -> },
                onDelete = {},
                onReassignTransaction = { _, _ -> },
                onConfirmDelete = {},
                onCancelDeletion = {},
            )
        }

        composeTestRule.onNodeWithText("خوراک").assertIsDisplayed()
        composeTestRule.onNodeWithText("حمل‌ونقل").assertIsDisplayed()
    }

    @Test
    fun tabRow_isDisplayed() {
        val state =
            CategoriesUiState(
                type = TransactionType.EXPENSE,
                categories = emptyList(),
            )

        composeTestRule.setContent {
            CategoriesScreen(
                uiState = state,
                deletionRequest = null,
                onTypeChange = {},
                onAdd = {},
                onRename = { _, _ -> },
                onDelete = {},
                onReassignTransaction = { _, _ -> },
                onConfirmDelete = {},
                onCancelDeletion = {},
            )
        }

        composeTestRule.onNodeWithText("مخارج").assertIsDisplayed()
        composeTestRule.onNodeWithText("درآمد").assertIsDisplayed()
    }
}
