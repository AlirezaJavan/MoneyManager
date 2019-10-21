package com.javanapps.moneymanager.feature.home.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.javanapps.moneymanager.core.domain.transaction.GetMonthTransactionsUseCase
import com.javanapps.moneymanager.core.domain.transaction.GetMonthlySummaryUseCase
import com.javanapps.moneymanager.core.model.ShamsiDate
import com.javanapps.moneymanager.core.model.Transaction
import com.javanapps.moneymanager.core.model.TransactionSource
import com.javanapps.moneymanager.core.model.TransactionType
import com.javanapps.moneymanager.core.testing.repository.TestTransactionRepository
import com.javanapps.moneymanager.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = TestTransactionRepository()
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        viewModel =
            HomeViewModel(
                getMonthlySummary = GetMonthlySummaryUseCase(repository),
                getMonthTransactions = GetMonthTransactionsUseCase(repository),
            )
    }

    @Test
    fun uiState_reflectsTransactionsAndSummary() =
        runTest {
            val month = viewModel.uiState.value.monthKey
            val transactions =
                listOf(
                    Transaction(
                        id = 1,
                        amountToman = 1000,
                        type = TransactionType.EXPENSE,
                        categoryName = "Food",
                        title = "Lunch",
                        note = "",
                        date = ShamsiDate(month.year, month.month, 1),
                        createdAtEpochMillis = 0,
                        source = TransactionSource.MANUAL,
                    ),
                )

            viewModel.uiState.test {
                // 1. Initial state might be loading
                var state = awaitItem()

                // 2. Load data
                repository.setTransactions(transactions)

                // 3. Wait for data-loaded state
                while (state.isLoading || state.transactions.isEmpty()) {
                    state = awaitItem()
                }

                assertThat(state.transactions).hasSize(1)
                assertThat(state.transactions[0].amountToman).isEqualTo(1000)
                assertThat(state.summary.expenseToman).isEqualTo(1000)
            }
        }

    @Test
    fun filterChange_updatesUiState() =
        runTest {
            viewModel.uiState.test {
                awaitItem() // Skip current
                viewModel.onFilterChange(HomeFilter.EXPENSE)
                assertThat(awaitItem().filter).isEqualTo(HomeFilter.EXPENSE)
            }
        }
}
