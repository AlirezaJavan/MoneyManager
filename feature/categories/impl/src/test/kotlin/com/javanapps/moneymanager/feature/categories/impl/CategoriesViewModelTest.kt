package com.javanapps.moneymanager.feature.categories.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.javanapps.moneymanager.core.domain.category.AddCategoryUseCase
import com.javanapps.moneymanager.core.domain.category.DeleteCategoryUseCase
import com.javanapps.moneymanager.core.domain.category.GetCategoriesUseCase
import com.javanapps.moneymanager.core.domain.category.RenameCategoryUseCase
import com.javanapps.moneymanager.core.domain.transaction.SearchTransactionsUseCase
import com.javanapps.moneymanager.core.domain.transaction.UpdateTransactionUseCase
import com.javanapps.moneymanager.core.model.Category
import com.javanapps.moneymanager.core.model.ShamsiDate
import com.javanapps.moneymanager.core.model.Transaction
import com.javanapps.moneymanager.core.model.TransactionSource
import com.javanapps.moneymanager.core.model.TransactionType
import com.javanapps.moneymanager.core.testing.repository.TestCategoryRepository
import com.javanapps.moneymanager.core.testing.repository.TestTransactionRepository
import com.javanapps.moneymanager.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CategoriesViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val categoryRepository = TestCategoryRepository()
    private val transactionRepository = TestTransactionRepository()
    private lateinit var viewModel: CategoriesViewModel

    @Before
    fun setup() {
        viewModel =
            CategoriesViewModel(
                getCategories = GetCategoriesUseCase(categoryRepository),
                addCategory = AddCategoryUseCase(categoryRepository),
                renameCategory = RenameCategoryUseCase(categoryRepository),
                deleteCategory = DeleteCategoryUseCase(categoryRepository, transactionRepository),
                searchTransactions = SearchTransactionsUseCase(transactionRepository),
                updateTransaction = UpdateTransactionUseCase(transactionRepository),
            )
    }

    @Test
    fun uiState_reflectsCategories() =
        runTest {
            categoryRepository.add("Food", TransactionType.EXPENSE)

            viewModel.uiState.test {
                val state = expectMostRecentItem()
                assertThat(state.categories).hasSize(1)
                assertThat(state.categories[0].name).isEqualTo("Food")
            }
        }

    @Test
    fun deleteCategory_showsRequest_whenHasTransactions() =
        runTest {
            val cat = Category(id = 1, name = "Food", type = TransactionType.EXPENSE)
            categoryRepository.setCategories(listOf(cat))

            // Add a linked transaction
            transactionRepository.add(
                Transaction(1, 1000, TransactionType.EXPENSE, "Food", "Lunch", "", ShamsiDate(1403, 1, 1), 0, TransactionSource.MANUAL),
            )

            viewModel.onDeleteClicked(cat)

            viewModel.deletionRequest.test {
                val request = awaitItem()
                assertThat(request).isNotNull()
                assertThat(request?.linkedTransactions).hasSize(1)
            }
        }

    @Test
    fun deleteCategory_showsRequest_evenWhenEmpty() =
        runTest {
            val cat = Category(id = 1, name = "Food", type = TransactionType.EXPENSE)
            categoryRepository.setCategories(listOf(cat))

            viewModel.onDeleteClicked(cat)

            viewModel.deletionRequest.test {
                val request = awaitItem()
                assertThat(request).isNotNull()
                assertThat(request?.linkedTransactions).isEmpty()
            }

            // Should not be deleted yet
            assertThat(categoryRepository.exists("Food", TransactionType.EXPENSE)).isTrue()

            // Now confirm
            viewModel.confirmDelete()

            // Wait for deletion to propagate if necessary (it's a launch in ViewModel)
            // But here the repository is fake and synchronous in its suspend methods.

            assertThat(categoryRepository.exists("Food", TransactionType.EXPENSE)).isFalse()
        }

    @Test
    fun addCategory_updatesRepository() =
        runTest {
            viewModel.addCategory("Rent")

            viewModel.uiState.test {
                assertThat(expectMostRecentItem().categories.map { it.name }).contains("Rent")
            }
        }
}
