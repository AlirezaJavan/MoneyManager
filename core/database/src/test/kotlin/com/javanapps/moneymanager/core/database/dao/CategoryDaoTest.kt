package com.javanapps.moneymanager.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.javanapps.moneymanager.core.database.MoneyManagerDatabase
import com.javanapps.moneymanager.core.database.model.CategoryEntity
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
class CategoryDaoTest {
    private lateinit var db: MoneyManagerDatabase
    private lateinit var dao: CategoryDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db =
            Room
                .inMemoryDatabaseBuilder(context, MoneyManagerDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        dao = db.categoryDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun upsertAndObserveByType() =
        runTest {
            val c1 = CategoryEntity(name = "A", type = TransactionType.EXPENSE)
            val c2 = CategoryEntity(name = "B", type = TransactionType.INCOME)
            dao.upsert(c1)
            dao.upsert(c2)

            val expenses = dao.observeByType(TransactionType.EXPENSE.name).first()
            assertEquals(1, expenses.size)
            assertEquals("A", expenses[0].name)
        }

    @Test
    fun rename() =
        runTest {
            val id = dao.upsert(CategoryEntity(name = "Old", type = TransactionType.EXPENSE))
            dao.rename(id, "New")

            val all = dao.observeAll().first()
            assertEquals("New", all[0].name)
        }

    @Test
    fun deleteById() =
        runTest {
            val id = dao.upsert(CategoryEntity(name = "To Delete", type = TransactionType.EXPENSE))
            dao.deleteById(id)

            val count = dao.count()
            assertEquals(0, count)
        }

    @Test
    fun insertAllIgnoringConflicts() =
        runTest {
            val list =
                listOf(
                    CategoryEntity(name = "Food", type = TransactionType.EXPENSE),
                    CategoryEntity(name = "Food", type = TransactionType.EXPENSE), // Conflict
                )
            dao.insertAllIgnoringConflicts(list)
            assertEquals(1, dao.count())
        }
}
