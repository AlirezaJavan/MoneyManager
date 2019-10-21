package com.javanapps.moneymanager.core.domain.transaction

import com.javanapps.moneymanager.core.data.repository.TransactionRepository
import com.javanapps.moneymanager.core.model.Transaction
import javax.inject.Inject

/** Loads a single transaction by id (for editing). */
class GetTransactionUseCase
    @Inject
    constructor(
        private val repository: TransactionRepository,
    ) {
        suspend operator fun invoke(id: Long): Transaction? = repository.get(id)
    }
