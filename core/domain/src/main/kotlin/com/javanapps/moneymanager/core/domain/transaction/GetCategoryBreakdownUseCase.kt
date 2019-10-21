package com.javanapps.moneymanager.core.domain.transaction

import com.javanapps.moneymanager.core.data.repository.TransactionRepository
import com.javanapps.moneymanager.core.model.CategoryAmount
import com.javanapps.moneymanager.core.model.MonthKey
import com.javanapps.moneymanager.core.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetCategoryBreakdownUseCase
    @Inject
    constructor(
        private val repository: TransactionRepository,
    ) {
        operator fun invoke(
            monthKey: MonthKey,
            type: TransactionType,
        ): Flow<List<CategoryAmount>> =
            repository.observeCategoryBreakdown(monthKey, type).map { amounts ->
                val total = amounts.sumOf { it.amountToman }
                if (total <= 0) {
                    amounts
                } else {
                    amounts.map { it.copy(percent = ((it.amountToman * 100) / total).toInt()) }
                }
            }
    }
