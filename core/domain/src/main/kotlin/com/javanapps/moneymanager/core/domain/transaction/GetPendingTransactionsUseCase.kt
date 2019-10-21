package com.javanapps.moneymanager.core.domain.transaction

import com.javanapps.moneymanager.core.data.repository.TransactionRepository
import com.javanapps.moneymanager.core.model.Transaction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Observes transactions that are waiting for user confirmation (isPending = true). */
class GetPendingTransactionsUseCase
    @Inject
    constructor(
        private val repository: TransactionRepository,
    ) {
        operator fun invoke(): Flow<List<Transaction>> = repository.observePending()
    }
