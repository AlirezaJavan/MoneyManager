package com.javanapps.moneymanager.core.domain.transaction

import com.javanapps.moneymanager.core.data.repository.TransactionRepository
import com.javanapps.moneymanager.core.model.MonthKey
import com.javanapps.moneymanager.core.model.Transaction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMonthTransactionsUseCase
    @Inject
    constructor(
        private val repository: TransactionRepository,
    ) {
        operator fun invoke(monthKey: MonthKey): Flow<List<Transaction>> = repository.observeMonth(monthKey)
    }
