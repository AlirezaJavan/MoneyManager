package com.javanapps.moneymanager.core.domain.transaction

import com.javanapps.moneymanager.core.data.repository.TransactionRepository
import com.javanapps.moneymanager.core.model.Transaction
import com.javanapps.moneymanager.core.model.TransactionFilter
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Observes transactions matching the legacy-equivalent [TransactionFilter]. */
class SearchTransactionsUseCase
    @Inject
    constructor(
        private val repository: TransactionRepository,
    ) {
        operator fun invoke(filter: TransactionFilter): Flow<List<Transaction>> = repository.observeFiltered(filter)
    }
