package com.javanapps.moneymanager.core.domain.transaction

import com.google.common.truth.Truth.assertThat
import com.javanapps.moneymanager.core.model.Transaction
import com.javanapps.moneymanager.core.model.TransactionSource
import com.javanapps.moneymanager.core.model.TransactionType
import com.javanapps.moneymanager.core.testing.repository.TestTransactionRepository
import io.github.alirezajavan.shamsipicker.model.MonthKey
import io.github.alirezajavan.shamsipicker.model.ShamsiDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetCategoryBreakdownUseCaseTest {
    private val repository = TestTransactionRepository()
    private val useCase = GetCategoryBreakdownUseCase(repository)

    private fun expense(
        category: String,
        amount: Long,
        day: Int = 1,
    ) = Transaction(
        id = 0,
        amountToman = amount,
        type = TransactionType.EXPENSE,
        categoryName = category,
        title = "",
        note = "",
        date = ShamsiDate(1403, 5, day),
        createdAtEpochMillis = day.toLong(),
        source = TransactionSource.MANUAL,
    )

    @Test
    fun `computes per-category totals and percents`() =
        runTest {
            repository.setTransactions(
                listOf(
                    expense("خوراک", 60_000),
                    expense("خوراک", 20_000),
                    expense("حمل و نقل", 20_000),
                ),
            )

            val breakdown = useCase(MonthKey(1403, 5), TransactionType.EXPENSE).first()

            assertThat(breakdown.map { it.categoryName }).containsExactly("خوراک", "حمل و نقل").inOrder()
            assertThat(breakdown[0].amountToman).isEqualTo(80_000)
            assertThat(breakdown[0].percent).isEqualTo(80)
            assertThat(breakdown[1].percent).isEqualTo(20)
        }

    @Test
    fun `ignores other month and other type`() =
        runTest {
            repository.setTransactions(
                listOf(
                    expense("خوراک", 50_000),
                    expense("خوراک", 10_000, day = 1).copy(date = ShamsiDate(1403, 6, 1)),
                ),
            )

            val breakdown = useCase(MonthKey(1403, 5), TransactionType.EXPENSE).first()

            assertThat(breakdown).hasSize(1)
            assertThat(breakdown[0].amountToman).isEqualTo(50_000)
        }
}
