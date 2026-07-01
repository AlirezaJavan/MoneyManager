package com.javanapps.moneymanager.core.domain.category

import com.google.common.truth.Truth.assertThat
import com.javanapps.moneymanager.core.model.Category
import com.javanapps.moneymanager.core.model.Transaction
import com.javanapps.moneymanager.core.model.TransactionSource
import com.javanapps.moneymanager.core.model.TransactionType
import com.javanapps.moneymanager.core.testing.repository.TestCategoryRepository
import com.javanapps.moneymanager.core.testing.repository.TestTransactionRepository
import io.github.alirezajavan.shamsipicker.model.ShamsiDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DeleteCategoryUseCaseTest {
    private val categoryRepository = TestCategoryRepository()
    private val transactionRepository = TestTransactionRepository()
    private val useCase = DeleteCategoryUseCase(categoryRepository, transactionRepository)

    private val category = Category(id = 1, name = "خوراک", type = TransactionType.EXPENSE)

    private fun tx() =
        Transaction(
            id = 0,
            amountToman = 1000,
            type = TransactionType.EXPENSE,
            categoryName = "خوراک",
            title = "",
            note = "",
            date = ShamsiDate(1403, 5, 1),
            createdAtEpochMillis = 1,
            source = TransactionSource.MANUAL,
        )

    @Test
    fun `rejects deletion when category has linked transactions`() =
        runTest {
            categoryRepository.setCategories(listOf(category))
            transactionRepository.setTransactions(listOf(tx()))

            val result = useCase(category)

            assertThat(result).isInstanceOf(DeleteCategoryUseCase.Result.HasTransactions::class.java)
            assertThat((result as DeleteCategoryUseCase.Result.HasTransactions).count).isEqualTo(1)
            assertThat(categoryRepository.observeAll().first()).contains(category)
        }

    @Test
    fun `reassigns transactions then deletes`() =
        runTest {
            categoryRepository.setCategories(listOf(category))
            transactionRepository.setTransactions(listOf(tx()))

            val result = useCase(category, reassignTo = "متفرقه")

            assertThat(result).isEqualTo(DeleteCategoryUseCase.Result.Deleted)
            assertThat(categoryRepository.observeAll().first()).isEmpty()
            assertThat(transactionRepository.observeCountInCategory("متفرقه", TransactionType.EXPENSE).first())
                .isEqualTo(1)
        }

    @Test
    fun `deletes linked transactions when requested`() =
        runTest {
            categoryRepository.setCategories(listOf(category))
            transactionRepository.setTransactions(listOf(tx(), tx()))

            val result = useCase(category, deleteTransactions = true)

            assertThat(result).isEqualTo(DeleteCategoryUseCase.Result.Deleted)
            assertThat(transactionRepository.observeCountInCategory("خوراک", TransactionType.EXPENSE).first())
                .isEqualTo(0)
        }
}
