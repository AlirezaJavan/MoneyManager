package com.javanapps.moneymanager.feature.home.impl

import com.javanapps.moneymanager.core.model.MonthlySummary
import com.javanapps.moneymanager.core.model.Transaction
import io.github.alirezajavan.shamsipicker.model.MonthKey

/** Which transactions are shown (همه / درآمد / مخارج). */
enum class HomeFilter { ALL, INCOME, EXPENSE }

/** Income vs expense totals for a single Shamsi day of the month. */
data class DaySummary(
    val day: Int,
    val incomeToman: Long,
    val expenseToman: Long,
) {
    val netToman: Long get() = incomeToman - expenseToman
}

data class HomeUiState(
    val monthKey: MonthKey,
    val monthTitle: String,
    val summary: MonthlySummary,
    val filter: HomeFilter,
    val transactions: List<Transaction>,
    val daySummaries: List<DaySummary>,
    val selectedDay: Int?,
    val isLoading: Boolean,
)
