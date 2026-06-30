package com.javanapps.moneymanager.core.model

import io.github.alirezajavan.shamsipicker.model.MonthKey

/** Income vs expense totals for a period; balance is derived (مانده). */
data class MonthlySummary(
    val incomeToman: Long,
    val expenseToman: Long,
) {
    val balanceToman: Long get() = incomeToman - expenseToman

    companion object {
        val Empty = MonthlySummary(incomeToman = 0, expenseToman = 0)
    }
}

/** A category's total spend/income, optionally with its share [percent] of the period total. */
data class CategoryAmount(
    val categoryName: String,
    val amountToman: Long,
    val percent: Int = 0,
)

/** Total for a single Shamsi day (for the day-to-day chart). */
data class DayAmount(
    val day: Int,
    val amountToman: Long,
)

/** Total for a single Shamsi month (for the month-to-month chart). */
data class MonthAmount(
    val monthKey: MonthKey,
    val amountToman: Long,
)
