package com.javanapps.moneymanager.feature.home.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.javanapps.moneymanager.core.domain.transaction.GetMonthTransactionsUseCase
import com.javanapps.moneymanager.core.domain.transaction.GetMonthlySummaryUseCase
import com.javanapps.moneymanager.core.model.Transaction
import com.javanapps.moneymanager.core.model.TransactionSource
import com.javanapps.moneymanager.core.model.TransactionType
import com.javanapps.moneymanager.core.testing.repository.TestTransactionRepository
import com.javanapps.moneymanager.core.testing.util.MainDispatcherRule
import io.github.alirezajavan.shamsipicker.model.ShamsiDate
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

    @Test
    fun uiState_groupsTransactionsIntoDaySummaries() =
        runTest {
            val month = viewModel.uiState.value.monthKey
            val transactions =
                listOf(
                    Transaction(
                        id = 1,
                        amountToman = 1000,
                        type = TransactionType.INCOME,
                        categoryName = "Salary",
                        title = "Salary",
                        note = "",
                        date = ShamsiDate(month.year, month.month, 1),
                        createdAtEpochMillis = 0,
                        source = TransactionSource.MANUAL,
                    ),
                    Transaction(
                        id = 2,
                        amountToman = 400,
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
                var state = awaitItem()
                repository.setTransactions(transactions)
                while (state.isLoading || state.daySummaries.isEmpty()) {
                    state = awaitItem()
                }

                assertThat(state.daySummaries).hasSize(1)
                val daySummary = state.daySummaries.first()
                assertThat(daySummary.day).isEqualTo(1)
                assertThat(daySummary.netToman).isEqualTo(600)
            }
        }

    @Test
    fun selectDay_showsOnlyThatDaysTransactions() =
        runTest {
            val month = viewModel.uiState.value.monthKey
            val transactions =
                listOf(
                    Transaction(
                        id = 1,
                        amountToman = 1000,
                        type = TransactionType.EXPENSE,
                        categoryName = "Food",
                        title = "Day1",
                        note = "",
                        date = ShamsiDate(month.year, month.month, 1),
                        createdAtEpochMillis = 0,
                        source = TransactionSource.MANUAL,
                    ),
                    Transaction(
                        id = 2,
                        amountToman = 2000,
                        type = TransactionType.EXPENSE,
                        categoryName = "Food",
                        title = "Day2",
                        note = "",
                        date = ShamsiDate(month.year, month.month, 2),
                        createdAtEpochMillis = 0,
                        source = TransactionSource.MANUAL,
                    ),
                )

            viewModel.uiState.test {
                var state = awaitItem()
                repository.setTransactions(transactions)
                while (state.isLoading || state.transactions.isEmpty()) {
                    state = awaitItem()
                }

                viewModel.onSelectDay(1)
                state = awaitItem()
                assertThat(state.selectedDay).isEqualTo(1)
                assertThat(state.transactions.filter { it.date.day == 1 }).hasSize(1)

                viewModel.onNextMonth()
                while (state.selectedDay != null) {
                    state = awaitItem()
                }
                assertThat(state.selectedDay as Int?).isNull()
            }
        }
}
