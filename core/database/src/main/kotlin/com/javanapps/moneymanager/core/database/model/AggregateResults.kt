package com.javanapps.moneymanager.core.database.model

/** Sum of amounts for a category (used by the breakdown pie charts). */
data class CategorySum(
    val categoryName: String,
    val total: Long,
)

/** Sum of amounts for a single Shamsi day (used by the day-to-day chart). */
data class DaySum(
    val day: Int,
    val total: Long,
)

/** Sum of amounts for a single Shamsi month (used by the month-to-month chart). */
data class MonthSum(
    val year: Int,
    val month: Int,
    val total: Long,
)
