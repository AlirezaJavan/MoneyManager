package com.javanapps.moneymanager.feature.home.impl

import com.javanapps.moneymanager.core.model.MonthKey
import com.javanapps.moneymanager.core.model.MonthlySummary
import com.javanapps.moneymanager.core.model.Transaction

/** Which transactions are shown (همه / درآمد / مخارج). */
enum class HomeFilter { ALL, INCOME, EXPENSE }

data class HomeUiState(
    val monthKey: MonthKey,
    val monthTitle: String,
    val summary: MonthlySummary,
    val filter: HomeFilter,
    val transactions: List<Transaction>,
    val isLoading: Boolean,
)
