package com.javanapps.moneymanager.core.domain.transaction

import com.javanapps.moneymanager.core.data.repository.TransactionRepository
import com.javanapps.moneymanager.core.model.MonthlySummary
import io.github.alirezajavan.shamsipicker.model.MonthKey
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Observes income/expense/balance totals for a month (مخارج/درآمد/مانده). */
class GetMonthlySummaryUseCase
    @Inject
    constructor(
        private val repository: TransactionRepository,
    ) {
        operator fun invoke(monthKey: MonthKey): Flow<MonthlySummary> = repository.observeMonthlySummary(monthKey)
    }
