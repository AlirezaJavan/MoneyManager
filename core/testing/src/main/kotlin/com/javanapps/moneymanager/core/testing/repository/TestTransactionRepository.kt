package com.javanapps.moneymanager.core.testing.repository

import com.javanapps.moneymanager.core.data.repository.TransactionRepository
import com.javanapps.moneymanager.core.model.CategoryAmount
import com.javanapps.moneymanager.core.model.DayAmount
import com.javanapps.moneymanager.core.model.MonthAmount
import com.javanapps.moneymanager.core.model.MonthKey
import com.javanapps.moneymanager.core.model.MonthlySummary
import com.javanapps.moneymanager.core.model.Transaction
import com.javanapps.moneymanager.core.model.TransactionFilter
import com.javanapps.moneymanager.core.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory [TransactionRepository] fake with the same aggregation semantics as the real one. */
class TestTransactionRepository : TransactionRepository {
    private val state = MutableStateFlow<List<Transaction>>(emptyList())
    private var nextId = 1L

    fun setTransactions(transactions: List<Transaction>) {
        state.value = transactions
        nextId = (transactions.maxOfOrNull { it.id } ?: 0L) + 1
    }

    private fun List<Transaction>.inMonth(monthKey: MonthKey) = filter { it.date.year == monthKey.year && it.date.month == monthKey.month }

    override fun observeMonth(monthKey: MonthKey): Flow<List<Transaction>> =
        state.map {
            it.inMonth(monthKey).sortedWith(
                compareByDescending<Transaction> { t -> t.date.day }.thenByDescending { t -> t.createdAtEpochMillis },
            )
        }

    override fun observeMonthlySummary(monthKey: MonthKey): Flow<MonthlySummary> =
        state.map { list ->
            val month = list.inMonth(monthKey)
            MonthlySummary(
                incomeToman = month.filter { it.type == TransactionType.INCOME }.sumOf { it.amountToman },
                expenseToman = month.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountToman },
            )
        }

    override fun observeCategoryBreakdown(
        monthKey: MonthKey,
        type: TransactionType,
    ): Flow<List<CategoryAmount>> =
        state.map { list ->
            list
                .inMonth(monthKey)
                .filter { it.type == type }
                .groupBy { it.categoryName }
                .map { (name, txs) -> CategoryAmount(name, txs.sumOf { it.amountToman }) }
                .sortedByDescending { it.amountToman }
        }

    override fun observeDailyTotals(
        monthKey: MonthKey,
        type: TransactionType,
    ): Flow<List<DayAmount>> =
        state.map { list ->
            list
                .inMonth(monthKey)
                .filter { it.type == type }
                .groupBy { it.date.day }
                .map { (day, txs) -> DayAmount(day, txs.sumOf { it.amountToman }) }
                .sortedBy { it.day }
        }

    override fun observeMonthlyTotals(
        type: TransactionType,
        from: MonthKey,
        to: MonthKey,
    ): Flow<List<MonthAmount>> =
        state.map { list ->
            list
                .filter { it.type == type && it.date.monthKey.ordinal in from.ordinal..to.ordinal }
                .groupBy { it.date.monthKey }
                .map { (key, txs) -> MonthAmount(key, txs.sumOf { it.amountToman }) }
                .sortedBy { it.monthKey.ordinal }
        }

    override fun observeFiltered(filter: TransactionFilter): Flow<List<Transaction>> =
        state.map { list ->
            list.filter { tx ->
                (filter.from == null || tx.date.monthKey.ordinal >= filter.from!!.ordinal) &&
                    (filter.to == null || tx.date.monthKey.ordinal <= filter.to!!.ordinal) &&
                    (filter.type == null || tx.type == filter.type) &&
                    (filter.titleQuery.isNullOrBlank() || tx.title.contains(filter.titleQuery!!)) &&
                    (filter.categoryQuery.isNullOrBlank() || tx.categoryName.contains(filter.categoryQuery!!))
            }
        }

    override fun observePending(): Flow<List<Transaction>> = state.map { it.filter { it.isPending } }

    override fun observeCountInCategory(
        categoryName: String,
        type: TransactionType,
    ): Flow<Int> = state.map { list -> list.count { it.categoryName == categoryName && it.type == type } }

    override suspend fun countInCategory(
        categoryName: String,
        type: TransactionType,
    ): Int = state.value.count { it.categoryName == categoryName && it.type == type }

    override suspend fun get(id: Long): Transaction? = state.value.find { it.id == id }

    override suspend fun add(transaction: Transaction): Long {
        val id = nextId++
        state.value = state.value + transaction.copy(id = id)
        return id
    }

    override suspend fun addAll(transactions: List<Transaction>) {
        state.value = state.value + transactions.map { it.copy(id = nextId++) }
    }

    override suspend fun update(transaction: Transaction) {
        state.value = state.value.map { if (it.id == transaction.id) transaction else it }
    }

    override suspend fun delete(id: Long) {
        state.value = state.value.filterNot { it.id == id }
    }

    override suspend fun reassignCategory(
        oldName: String,
        newName: String,
        type: TransactionType,
    ) {
        state.value =
            state.value.map {
                if (it.categoryName == oldName && it.type == type) it.copy(categoryName = newName) else it
            }
    }

    override suspend fun deleteByCategory(
        categoryName: String,
        type: TransactionType,
    ) {
        state.value = state.value.filterNot { it.categoryName == categoryName && it.type == type }
    }
}
