package com.javanapps.moneymanager.core.domain.chart

import com.javanapps.moneymanager.core.data.repository.TransactionRepository
import com.javanapps.moneymanager.core.model.DayAmount
import com.javanapps.moneymanager.core.model.MonthAmount
import com.javanapps.moneymanager.core.model.TransactionType
import io.github.alirezajavan.shamsipicker.model.MonthKey
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Day-to-day totals within a Shamsi month (leap-aware boundaries handled by the calendar). */
class GetDailyTotalsUseCase
    @Inject
    constructor(
        private val repository: TransactionRepository,
    ) {
        operator fun invoke(
            monthKey: MonthKey,
            type: TransactionType,
        ): Flow<List<DayAmount>> = repository.observeDailyTotals(monthKey, type)
    }

/** Month-to-month totals across an inclusive Shamsi month range. */
class GetMonthlyTotalsUseCase
    @Inject
    constructor(
        private val repository: TransactionRepository,
    ) {
        operator fun invoke(
            type: TransactionType,
            from: MonthKey,
            to: MonthKey,
        ): Flow<List<MonthAmount>> = repository.observeMonthlyTotals(type, from, to)
    }
