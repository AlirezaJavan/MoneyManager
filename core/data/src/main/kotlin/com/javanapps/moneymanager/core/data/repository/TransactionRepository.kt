package com.javanapps.moneymanager.core.data.repository

import com.javanapps.moneymanager.core.model.CategoryAmount
import com.javanapps.moneymanager.core.model.DayAmount
import com.javanapps.moneymanager.core.model.MonthAmount
import com.javanapps.moneymanager.core.model.MonthlySummary
import com.javanapps.moneymanager.core.model.Transaction
import com.javanapps.moneymanager.core.model.TransactionFilter
import com.javanapps.moneymanager.core.model.TransactionType
import io.github.alirezajavan.shamsipicker.model.MonthKey
import kotlinx.coroutines.flow.Flow

/**
 * Reads and writes transactions. Follows CQS: every query returns a [Flow] (or a value for one-shot
 * reads); every command mutates and returns Unit or an id.
 */
interface TransactionRepository {
    // Queries
    fun observeMonth(monthKey: MonthKey): Flow<List<Transaction>>

    fun observeMonthlySummary(monthKey: MonthKey): Flow<MonthlySummary>

    /** Per-category totals for a month and [type]; `percent` is left at 0 for the use case to fill. */
    fun observeCategoryBreakdown(
        monthKey: MonthKey,
        type: TransactionType,
    ): Flow<List<CategoryAmount>>

    fun observeDailyTotals(
        monthKey: MonthKey,
        type: TransactionType,
    ): Flow<List<DayAmount>>

    fun observeMonthlyTotals(
        type: TransactionType,
        from: MonthKey,
        to: MonthKey,
    ): Flow<List<MonthAmount>>

    fun observeFiltered(filter: TransactionFilter): Flow<List<Transaction>>

    fun observePending(): Flow<List<Transaction>>

    fun observeCountInCategory(
        categoryName: String,
        type: TransactionType,
    ): Flow<Int>

    suspend fun get(id: Long): Transaction?

    suspend fun countInCategory(
        categoryName: String,
        type: TransactionType,
    ): Int

    // Commands
    suspend fun add(transaction: Transaction): Long

    suspend fun addAll(transactions: List<Transaction>)

    suspend fun update(transaction: Transaction)

    suspend fun delete(id: Long)

    suspend fun reassignCategory(
        oldName: String,
        newName: String,
        type: TransactionType,
    )

    suspend fun deleteByCategory(
        categoryName: String,
        type: TransactionType,
    )
}
