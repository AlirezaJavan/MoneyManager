package com.javanapps.moneymanager.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.javanapps.moneymanager.core.database.MoneyManagerDatabase
import com.javanapps.moneymanager.core.database.model.BankSmsRuleEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BankSmsRuleDaoTest {
    private lateinit var db: MoneyManagerDatabase
    private lateinit var dao: BankSmsRuleDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db =
            Room
                .inMemoryDatabaseBuilder(context, MoneyManagerDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        dao = db.bankSmsRuleDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun upsertAndObserveAll() =
        runTest {
            val rule =
                BankSmsRuleEntity(
                    senderPattern = "BANK1",
                    bankName = "Bank One",
                    incomeKeywords = "A",
                    expenseKeywords = "B",
                    amountInRial = true,
                    defaultCategory = "Misc",
                    sampleBody = "Sample",
                )
            dao.upsert(rule)

            val all = dao.observeAll().first()
            assertEquals(1, all.size)
            assertEquals("Bank One", all[0].bankName)
        }

    @Test
    fun getEnabled() =
        runTest {
            dao.upsert(createRule(pattern = "P1", enabled = true))
            dao.upsert(createRule(pattern = "P2", enabled = false))

            val enabled = dao.getEnabled()
            assertEquals(1, enabled.size)
            assertEquals("P1", enabled[0].senderPattern)
        }

    @Test
    fun deleteById() =
        runTest {
            val id = dao.upsert(createRule(pattern = "DeleteMe"))
            dao.deleteById(id)

            val all = dao.observeAll().first()
            assertEquals(0, all.size)
        }

    private fun createRule(
        pattern: String,
        enabled: Boolean = true,
    ) = BankSmsRuleEntity(
        senderPattern = pattern,
        bankName = "Bank",
        incomeKeywords = "A",
        expenseKeywords = "B",
        amountInRial = false,
        defaultCategory = "Misc",
        sampleBody = "Sample",
        enabled = enabled,
    )
}
