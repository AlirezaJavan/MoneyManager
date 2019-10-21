package com.javanapps.moneymanager.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.javanapps.moneymanager.core.database.MoneyManagerDatabase
import com.javanapps.moneymanager.core.database.model.CategoryEntity
import com.javanapps.moneymanager.core.database.model.TransactionEntity
import com.javanapps.moneymanager.core.model.TransactionSource
import com.javanapps.moneymanager.core.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TransactionDaoTest {
    private lateinit var db: MoneyManagerDatabase
    private lateinit var dao: TransactionDao
    private lateinit var categoryDao: CategoryDao

    // Pre-inserted category IDs
    private var foodExpenseId = 0L
    private var rentExpenseId = 0L
    private var oldExpenseId = 0L
    private var newExpenseId = 0L
    private var oldIncomeId = 0L

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db =
            Room
                .inMemoryDatabaseBuilder(context, MoneyManagerDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        dao = db.transactionDao()
        categoryDao = db.categoryDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    private suspend fun seedCategory(
        name: String,
        type: TransactionType,
    ): Long = categoryDao.upsert(CategoryEntity(name = name, type = type))

    @Test
    fun upsertAndGetById() =
        runTest {
            val catId = seedCategory("Test", TransactionType.EXPENSE)
            val transaction = createTransaction(id = 1, amount = 1000, categoryId = catId)
            dao.upsert(transaction)
            val loaded = dao.getById(1)
            assertEquals(transaction.amountToman, loaded?.amountToman)
            assertEquals("Test", loaded?.categoryName)
        }

    @Test
    fun deleteById() =
        runTest {
            val catId = seedCategory("Test", TransactionType.EXPENSE)
            val transaction = createTransaction(id = 1, categoryId = catId)
            dao.upsert(transaction)
            dao.deleteById(1)
            val loaded = dao.getById(1)
            assertEquals(null, loaded)
        }

    @Test
    fun observeMonthTotal() =
        runTest {
            val expCat = seedCategory("Misc", TransactionType.EXPENSE)
            val incCat = seedCategory("Salary", TransactionType.INCOME)
            dao.upsertAll(
                listOf(
                    createTransaction(amount = 1000, type = TransactionType.EXPENSE, year = 1403, month = 1, categoryId = expCat),
                    createTransaction(amount = 2000, type = TransactionType.EXPENSE, year = 1403, month = 1, categoryId = expCat),
                    createTransaction(amount = 5000, type = TransactionType.INCOME, year = 1403, month = 1, categoryId = incCat),
                    createTransaction(amount = 3000, type = TransactionType.EXPENSE, year = 1403, month = 2, categoryId = expCat),
                ),
            )

            val total = dao.observeMonthTotal(1403, 1, TransactionType.EXPENSE.name).first()
            assertEquals(3000L, total)
        }

    @Test
    fun observeCategoryBreakdown() =
        runTest {
            val foodId = seedCategory("Food", TransactionType.EXPENSE)
            val rentId = seedCategory("Rent", TransactionType.EXPENSE)
            dao.upsertAll(
                listOf(
                    createTransaction(amount = 1000, categoryId = foodId, type = TransactionType.EXPENSE, year = 1403, month = 1),
                    createTransaction(amount = 2000, categoryId = foodId, type = TransactionType.EXPENSE, year = 1403, month = 1),
                    createTransaction(amount = 1500, categoryId = rentId, type = TransactionType.EXPENSE, year = 1403, month = 1),
                ),
            )

            val breakdown = dao.observeCategoryBreakdown(1403, 1, TransactionType.EXPENSE.name).first()
            assertEquals(2, breakdown.size)
            assertEquals("Food", breakdown[0].categoryName)
            assertEquals(3000L, breakdown[0].total)
            assertEquals("Rent", breakdown[1].categoryName)
            assertEquals(1500L, breakdown[1].total)
        }

    @Test
    fun reassignCategory() =
        runTest {
            val oldExpId = seedCategory("Old", TransactionType.EXPENSE)
            val newExpId = seedCategory("New", TransactionType.EXPENSE)
            val oldIncId = seedCategory("Old", TransactionType.INCOME)
            dao.upsertAll(
                listOf(
                    createTransaction(categoryId = oldExpId, type = TransactionType.EXPENSE),
                    createTransaction(categoryId = oldExpId, type = TransactionType.EXPENSE),
                    createTransaction(categoryId = oldIncId, type = TransactionType.INCOME),
                ),
            )

            dao.reassignCategory("Old", "New", TransactionType.EXPENSE.name)

            val newCount = dao.countByCategory("New", TransactionType.EXPENSE.name)
            val oldExpCount = dao.countByCategory("Old", TransactionType.EXPENSE.name)
            val oldIncCount = dao.countByCategory("Old", TransactionType.INCOME.name)

            assertEquals(2, newCount)
            assertEquals(0, oldExpCount)
            assertEquals(1, oldIncCount)
        }

    @Test
    fun categoryNameReflectsRename() =
        runTest {
            val catId = seedCategory("OriginalName", TransactionType.EXPENSE)
            dao.upsert(createTransaction(id = 1, categoryId = catId))

            // Rename the category; transaction should immediately reflect the new name via JOIN
            categoryDao.rename(catId, "RenamedCategory")

            val loaded = dao.getById(1)
            assertEquals("RenamedCategory", loaded?.categoryName)
        }

    private fun createTransaction(
        id: Long = 0,
        amount: Long = 1000,
        categoryId: Long,
        type: TransactionType = TransactionType.EXPENSE,
        year: Int = 1403,
        month: Int = 1,
        day: Int = 1,
    ) = TransactionEntity(
        id = id,
        amountToman = amount,
        type = type,
        categoryId = categoryId,
        title = "Title",
        note = "Note",
        year = year,
        month = month,
        day = day,
        hour = 12,
        minute = 0,
        createdAtEpochMillis = System.currentTimeMillis(),
        source = TransactionSource.MANUAL,
    )
}
