package com.javanapps.moneymanager.feature.home.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.javanapps.moneymanager.core.model.MonthlySummary
import com.javanapps.moneymanager.core.model.Transaction
import com.javanapps.moneymanager.core.model.TransactionSource
import com.javanapps.moneymanager.core.model.TransactionType
import io.github.alirezajavan.shamsipicker.model.MonthKey
import io.github.alirezajavan.shamsipicker.model.ShamsiDate
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val testMonth = MonthKey(1403, 1)
    private val emptyState =
        HomeUiState(
            monthKey = testMonth,
            monthTitle = "فروردین ۱۴۰۳",
            transactions = emptyList(),
            daySummaries = emptyList(),
            selectedDay = null,
            summary = MonthlySummary(0L, 0L),
            filter = HomeFilter.ALL,
            isLoading = false,
        )

    @Test
    fun emptyState_showsEmptyMessage() {
        composeTestRule.setContent {
            HomeScreen(
                uiState = emptyState,
                onNextMonth = {},
                onPreviousMonth = {},
                onFilterChange = {},
                onSelectDay = {},
                onBackToDaySummary = {},
                onAddTransaction = {},
                onEditTransaction = {},
            )
        }

        composeTestRule.onNodeWithText("تراکنشی در این ماه ثبت نشده است").assertIsDisplayed()
    }

    @Test
    fun withTransactions_showsDaySummaryList() {
        val tx =
            Transaction(
                id = 1L,
                amountToman = 5000L,
                type = TransactionType.EXPENSE,
                categoryName = "خوراک",
                title = "ناهار",
                note = "",
                date = ShamsiDate(1403, 1, 10),
                createdAtEpochMillis = 0L,
                source = TransactionSource.MANUAL,
            )
        val state =
            emptyState.copy(
                transactions = listOf(tx),
                daySummaries = listOf(DaySummary(day = 10, incomeToman = 0L, expenseToman = 5000L)),
                summary = MonthlySummary(incomeToman = 0L, expenseToman = 5000L),
                isLoading = false,
            )

        composeTestRule.setContent {
            HomeScreen(
                uiState = state,
                onNextMonth = {},
                onPreviousMonth = {},
                onFilterChange = {},
                onSelectDay = {},
                onBackToDaySummary = {},
                onAddTransaction = {},
                onEditTransaction = {},
            )
        }

        composeTestRule.onNodeWithText("۱۰ فروردین").assertIsDisplayed()
    }

    @Test
    fun selectingDay_showsThatDaysTransactions() {
        val tx =
            Transaction(
                id = 1L,
                amountToman = 5000L,
                type = TransactionType.EXPENSE,
                categoryName = "خوراک",
                title = "ناهار",
                note = "",
                date = ShamsiDate(1403, 1, 10),
                createdAtEpochMillis = 0L,
                source = TransactionSource.MANUAL,
            )
        val state =
            emptyState.copy(
                transactions = listOf(tx),
                selectedDay = 10,
                summary = MonthlySummary(incomeToman = 0L, expenseToman = 5000L),
                isLoading = false,
            )

        composeTestRule.setContent {
            HomeScreen(
                uiState = state,
                onNextMonth = {},
                onPreviousMonth = {},
                onFilterChange = {},
                onSelectDay = {},
                onBackToDaySummary = {},
                onAddTransaction = {},
                onEditTransaction = {},
            )
        }

        composeTestRule.onNodeWithText("ناهار").assertIsDisplayed()
    }

    @Test
    fun filterChips_areDisplayed() {
        composeTestRule.setContent {
            HomeScreen(
                uiState = emptyState,
                onNextMonth = {},
                onPreviousMonth = {},
                onFilterChange = {},
                onSelectDay = {},
                onBackToDaySummary = {},
                onAddTransaction = {},
                onEditTransaction = {},
            )
        }

        composeTestRule.onNodeWithText("همه").assertIsDisplayed()
        composeTestRule.onNodeWithText("درآمد").assertIsDisplayed()
        composeTestRule.onNodeWithText("مخارج").assertIsDisplayed()
    }
}
