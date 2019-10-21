package com.javanapps.moneymanager.core.domain.transaction

import com.javanapps.moneymanager.core.data.repository.TransactionRepository
import com.javanapps.moneymanager.core.model.Transaction
import javax.inject.Inject

/** Adds a new transaction, returning its generated id. */
class AddTransactionUseCase
    @Inject
    constructor(
        private val repository: TransactionRepository,
    ) {
        suspend operator fun invoke(transaction: Transaction): Long = repository.add(transaction)
    }

/** Updates an existing transaction. */
class UpdateTransactionUseCase
    @Inject
    constructor(
        private val repository: TransactionRepository,
    ) {
        suspend operator fun invoke(transaction: Transaction) = repository.update(transaction)
    }

/** Deletes a transaction by id. */
class DeleteTransactionUseCase
    @Inject
    constructor(
        private val repository: TransactionRepository,
    ) {
        suspend operator fun invoke(id: Long) = repository.delete(id)
    }
