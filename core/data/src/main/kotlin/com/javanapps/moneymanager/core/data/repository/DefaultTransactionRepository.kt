package com.javanapps.moneymanager.core.data.repository

import com.javanapps.moneymanager.core.database.dao.CategoryDao
import com.javanapps.moneymanager.core.database.dao.TransactionDao
import com.javanapps.moneymanager.core.database.model.asEntity
import com.javanapps.moneymanager.core.database.model.asExternalModel
import com.javanapps.moneymanager.core.model.CategoryAmount
import com.javanapps.moneymanager.core.model.DayAmount
import com.javanapps.moneymanager.core.model.MonthAmount
import com.javanapps.moneymanager.core.model.MonthKey
import com.javanapps.moneymanager.core.model.MonthlySummary
import com.javanapps.moneymanager.core.model.Transaction
import com.javanapps.moneymanager.core.model.TransactionFilter
import com.javanapps.moneymanager.core.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class DefaultTransactionRepository
    @Inject
    constructor(
        private val dao: TransactionDao,
        private val categoryDao: CategoryDao,
    ) : TransactionRepository {
        private suspend fun categoryId(
            name: String,
            type: TransactionType,
        ): Long =
            categoryDao.getIdByNameAndType(name, type.name)
                ?: error("Category not found: '$name' (${type.name})")

        override fun observeMonth(monthKey: MonthKey): Flow<List<Transaction>> =
            dao
                .observeByMonth(monthKey.year, monthKey.month)
                .map { rows -> rows.map { it.asExternalModel() } }

        override fun observeMonthlySummary(monthKey: MonthKey): Flow<MonthlySummary> =
            combine(
                dao.observeMonthTotal(monthKey.year, monthKey.month, TransactionType.INCOME.name),
                dao.observeMonthTotal(monthKey.year, monthKey.month, TransactionType.EXPENSE.name),
            ) { income, expense -> MonthlySummary(incomeToman = income, expenseToman = expense) }

        override fun observeCategoryBreakdown(
            monthKey: MonthKey,
            type: TransactionType,
        ): Flow<List<CategoryAmount>> =
            dao
                .observeCategoryBreakdown(monthKey.year, monthKey.month, type.name)
                .map { rows -> rows.map { CategoryAmount(it.categoryName, it.total) } }

        override fun observeDailyTotals(
            monthKey: MonthKey,
            type: TransactionType,
        ): Flow<List<DayAmount>> =
            dao
                .observeDailyTotals(monthKey.year, monthKey.month, type.name)
                .map { rows -> rows.map { DayAmount(day = it.day, amountToman = it.total) } }

        override fun observeMonthlyTotals(
            type: TransactionType,
            from: MonthKey,
            to: MonthKey,
        ): Flow<List<MonthAmount>> =
            dao
                .observeMonthlyTotals(type.name, from.ordinal, to.ordinal)
                .map { rows -> rows.map { MonthAmount(MonthKey(it.year, it.month), it.total) } }

        override fun observeFiltered(filter: TransactionFilter): Flow<List<Transaction>> =
            dao
                .observeFiltered(
                    fromOrdinal = filter.from?.ordinal,
                    toOrdinal = filter.to?.ordinal,
                    type = filter.type?.name,
                    titleQuery = filter.titleQuery?.takeIf { it.isNotBlank() },
                    categoryQuery = filter.categoryQuery?.takeIf { it.isNotBlank() },
                ).map { rows -> rows.map { it.asExternalModel() } }

        override fun observePending(): Flow<List<Transaction>> =
            dao
                .observePending()
                .map { rows -> rows.map { it.asExternalModel() } }

        override fun observeCountInCategory(
            categoryName: String,
            type: TransactionType,
        ): Flow<Int> = dao.observeCountByCategory(categoryName, type.name)

        override suspend fun countInCategory(
            categoryName: String,
            type: TransactionType,
        ): Int = dao.countByCategory(categoryName, type.name)

        override suspend fun get(id: Long): Transaction? = dao.getById(id)?.asExternalModel()

        override suspend fun add(transaction: Transaction): Long {
            val catId = categoryId(transaction.categoryName, transaction.type)
            return dao.upsert(transaction.asEntity(catId))
        }

        override suspend fun addAll(transactions: List<Transaction>) {
            val entities =
                transactions.mapNotNull { tx ->
                    val catId =
                        categoryDao.getIdByNameAndType(tx.categoryName, tx.type.name)
                            ?: categoryDao.getFirstIdByType(tx.type.name) // fallback: oldest category of same type
                            ?: return@mapNotNull null // no categories at all — skip
                    tx.asEntity(catId)
                }
            dao.upsertAll(entities)
        }

        override suspend fun update(transaction: Transaction) {
            val catId = categoryId(transaction.categoryName, transaction.type)
            dao.upsert(transaction.asEntity(catId))
        }

        override suspend fun delete(id: Long) = dao.deleteById(id)

        override suspend fun reassignCategory(
            oldName: String,
            newName: String,
            type: TransactionType,
        ) = dao.reassignCategory(oldName, newName, type.name)

        override suspend fun deleteByCategory(
            categoryName: String,
            type: TransactionType,
        ) = dao.deleteByCategory(categoryName, type.name)
    }
