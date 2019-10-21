package com.javanapps.moneymanager.feature.reports.impl

import com.javanapps.moneymanager.core.model.Category
import com.javanapps.moneymanager.core.model.CategoryAmount
import com.javanapps.moneymanager.core.model.DayAmount
import com.javanapps.moneymanager.core.model.MonthAmount
import com.javanapps.moneymanager.core.model.MonthKey
import com.javanapps.moneymanager.core.model.MonthlySummary
import com.javanapps.moneymanager.core.model.Transaction
import com.javanapps.moneymanager.core.model.TransactionType

enum class ReportsTab { CHARTS, SEARCH }

enum class ChartPeriod { EXPENSE, INCOME }

data class ReportsUiState(
    val activeTab: ReportsTab = ReportsTab.CHARTS,
    // Chart controls
    val chartPeriod: ChartPeriod = ChartPeriod.EXPENSE,
    val selectedMonth: MonthKey = MonthKey(1403, 1),
    val trendFromMonth: MonthKey = MonthKey(1403, 1),
    val trendToMonth: MonthKey = MonthKey(1403, 1),
    // Chart data
    val categoryBreakdown: List<CategoryAmount> = emptyList(),
    val dailyTotals: List<DayAmount> = emptyList(),
    val monthlyIncomeTotals: List<MonthAmount> = emptyList(),
    val monthlyExpenseTotals: List<MonthAmount> = emptyList(),
    val monthlySummary: MonthlySummary = MonthlySummary.Empty,
    // Search / filter
    val searchResults: List<Transaction> = emptyList(),
    val titleQuery: String = "",
    val availableCategories: List<Category> = emptyList(),
    val selectedCategory: Category? = null,
    val filterType: TransactionType? = null,
    val filterFromMonth: MonthKey? = null,
    val filterToMonth: MonthKey? = null,
)
