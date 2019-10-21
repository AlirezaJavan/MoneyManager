package com.javanapps.moneymanager.feature.reports.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.javanapps.moneymanager.core.model.MonthKey
import org.junit.Rule
import org.junit.Test

class ReportsScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val defaultMonth = MonthKey(1403, 1)
    private val emptyState =
        ReportsUiState(
            activeTab = ReportsTab.CHARTS,
            chartPeriod = ChartPeriod.EXPENSE,
            selectedMonth = defaultMonth,
            trendFromMonth = defaultMonth,
            trendToMonth = defaultMonth,
        )

    @Test
    fun tabs_areDisplayed() {
        composeTestRule.setContent {
            ReportsScreen(
                uiState = emptyState,
                onTabSelected = {},
                onPeriodSelected = {},
                onMonthSelected = {},
                onTrendFromSelected = {},
                onTrendToSelected = {},
                onTitleQueryChange = {},
                onCategoryQueryChange = {},
                onFilterTypeChange = {},
                onFilterFromChange = {},
                onFilterToChange = {},
                onClearFilters = {},
            )
        }

        composeTestRule.onNodeWithText("نمودارها").assertIsDisplayed()
        composeTestRule.onNodeWithText("جستجو").assertIsDisplayed()
    }

    @Test
    fun expenseIncomeChips_areDisplayed_inChartsTab() {
        composeTestRule.setContent {
            ReportsScreen(
                uiState = emptyState,
                onTabSelected = {},
                onPeriodSelected = {},
                onMonthSelected = {},
                onTrendFromSelected = {},
                onTrendToSelected = {},
                onTitleQueryChange = {},
                onCategoryQueryChange = {},
                onFilterTypeChange = {},
                onFilterFromChange = {},
                onFilterToChange = {},
                onClearFilters = {},
            )
        }

        composeTestRule.onNodeWithText("مخارج").assertIsDisplayed()
        composeTestRule.onNodeWithText("درآمد").assertIsDisplayed()
    }
}
