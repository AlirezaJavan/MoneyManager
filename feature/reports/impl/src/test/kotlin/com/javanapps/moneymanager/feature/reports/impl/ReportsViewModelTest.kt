package com.javanapps.moneymanager.feature.reports.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.javanapps.moneymanager.core.domain.category.GetCategoriesUseCase
import com.javanapps.moneymanager.core.domain.chart.GetDailyTotalsUseCase
import com.javanapps.moneymanager.core.domain.chart.GetMonthlyTotalsUseCase
import com.javanapps.moneymanager.core.domain.transaction.GetCategoryBreakdownUseCase
import com.javanapps.moneymanager.core.domain.transaction.GetMonthlySummaryUseCase
import com.javanapps.moneymanager.core.domain.transaction.SearchTransactionsUseCase
import com.javanapps.moneymanager.core.model.Transaction
import com.javanapps.moneymanager.core.model.TransactionSource
import com.javanapps.moneymanager.core.model.TransactionType
import com.javanapps.moneymanager.core.testing.repository.TestCategoryRepository
import com.javanapps.moneymanager.core.testing.repository.TestTransactionRepository
import com.javanapps.moneymanager.core.testing.util.MainDispatcherRule
import io.github.alirezajavan.shamsipicker.model.ShamsiDate
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ReportsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = TestTransactionRepository()
    private val categoryRepository = TestCategoryRepository()
    private lateinit var viewModel: ReportsViewModel

    @Before
    fun setup() {
        viewModel =
            ReportsViewModel(
                getCategoryBreakdown = GetCategoryBreakdownUseCase(repository),
                getDailyTotals = GetDailyTotalsUseCase(repository),
                getMonthlyTotals = GetMonthlyTotalsUseCase(repository),
                getMonthlySummary = GetMonthlySummaryUseCase(repository),
                getCategories = GetCategoriesUseCase(categoryRepository),
                searchTransactions = SearchTransactionsUseCase(repository),
            )
    }

    @Test
    fun initialState_isChartsTab() =
        runTest {
            assertThat(viewModel.uiState.value.activeTab).isEqualTo(ReportsTab.CHARTS)
        }

    @Test
    fun setTab_updatesActiveTab() =
        runTest {
            viewModel.uiState.test {
                awaitItem()
                viewModel.setTab(ReportsTab.SEARCH)
                assertThat(awaitItem().activeTab).isEqualTo(ReportsTab.SEARCH)
            }
        }

    @Test
    fun setChartPeriod_updatesState() =
        runTest {
            viewModel.uiState.test {
                awaitItem()
                viewModel.setChartPeriod(ChartPeriod.INCOME)
                assertThat(awaitItem().chartPeriod).isEqualTo(ChartPeriod.INCOME)
            }
        }

    @Test
    fun setTitleQuery_updatesSearchResults() =
        runTest {
            val month = viewModel.uiState.value.selectedMonth
            repository.setTransactions(
                listOf(
                    Transaction(
                        id = 1L,
                        amountToman = 500L,
                        type = TransactionType.EXPENSE,
                        categoryName = "خوراک",
                        title = "ناهار",
                        note = "",
                        date = ShamsiDate(month.year, month.month, 1),
                        createdAtEpochMillis = 0L,
                        source = TransactionSource.MANUAL,
                    ),
                ),
            )
            viewModel.setTab(ReportsTab.SEARCH)
            viewModel.setTitleQuery("ناهار")

            viewModel.uiState.test {
                var state = awaitItem()
                while (state.searchResults.isEmpty()) state = awaitItem()
                assertThat(state.searchResults).hasSize(1)
                assertThat(state.searchResults[0].title).isEqualTo("ناهار")
            }
        }

    @Test
    fun clearFilters_resetsSearch() =
        runTest {
            viewModel.setTitleQuery("test")
            viewModel.setFilterType(TransactionType.EXPENSE)

            viewModel.clearFilters()

            viewModel.uiState.test {
                val state = expectMostRecentItem()
                assertThat(state.titleQuery).isEmpty()
                assertThat(state.selectedCategory).isNull()
                assertThat(state.filterType).isNull()
            }
        }

    @Test
    fun categoryBreakdown_reflectsTransactions() =
        runTest {
            val month = viewModel.uiState.value.selectedMonth
            repository.setTransactions(
                listOf(
                    Transaction(
                        id = 1L,
                        amountToman = 1000L,
                        type = TransactionType.EXPENSE,
                        categoryName = "خوراک",
                        title = "",
                        note = "",
                        date = ShamsiDate(month.year, month.month, 5),
                        createdAtEpochMillis = 0L,
                        source = TransactionSource.MANUAL,
                    ),
                    Transaction(
                        id = 2L,
                        amountToman = 500L,
                        type = TransactionType.EXPENSE,
                        categoryName = "حمل‌ونقل",
                        title = "",
                        note = "",
                        date = ShamsiDate(month.year, month.month, 10),
                        createdAtEpochMillis = 0L,
                        source = TransactionSource.MANUAL,
                    ),
                ),
            )

            viewModel.uiState.test {
                var state = awaitItem()
                while (state.categoryBreakdown.isEmpty()) state = awaitItem()
                assertThat(state.categoryBreakdown).hasSize(2)
                assertThat(state.categoryBreakdown.first().amountToman).isEqualTo(1000L)
            }
        }
}
