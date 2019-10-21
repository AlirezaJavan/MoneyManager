package com.javanapps.moneymanager.feature.reports.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.javanapps.moneymanager.core.common.calendar.ShamsiCalendar
import com.javanapps.moneymanager.core.domain.category.GetCategoriesUseCase
import com.javanapps.moneymanager.core.domain.chart.GetDailyTotalsUseCase
import com.javanapps.moneymanager.core.domain.chart.GetMonthlyTotalsUseCase
import com.javanapps.moneymanager.core.domain.transaction.GetCategoryBreakdownUseCase
import com.javanapps.moneymanager.core.domain.transaction.GetMonthlySummaryUseCase
import com.javanapps.moneymanager.core.domain.transaction.SearchTransactionsUseCase
import com.javanapps.moneymanager.core.model.Category
import com.javanapps.moneymanager.core.model.CategoryAmount
import com.javanapps.moneymanager.core.model.DayAmount
import com.javanapps.moneymanager.core.model.MonthAmount
import com.javanapps.moneymanager.core.model.MonthKey
import com.javanapps.moneymanager.core.model.MonthlySummary
import com.javanapps.moneymanager.core.model.Transaction
import com.javanapps.moneymanager.core.model.TransactionFilter
import com.javanapps.moneymanager.core.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ReportsViewModel
    @Inject
    constructor(
        getCategoryBreakdown: GetCategoryBreakdownUseCase,
        getDailyTotals: GetDailyTotalsUseCase,
        getMonthlyTotals: GetMonthlyTotalsUseCase,
        getMonthlySummary: GetMonthlySummaryUseCase,
        getCategories: GetCategoriesUseCase,
        searchTransactions: SearchTransactionsUseCase,
    ) : ViewModel() {
        private data class ChartParams(
            val month: MonthKey,
            val trendFrom: MonthKey,
            val trendTo: MonthKey,
            val period: ChartPeriod,
        )

        private data class SearchParams(
            val title: String = "",
            val selectedCategory: Category? = null,
            val type: TransactionType? = null,
            val from: MonthKey? = null,
            val to: MonthKey? = null,
        )

        private data class ChartState(
            val params: ChartParams,
            val breakdown: List<CategoryAmount>,
            val daily: List<DayAmount>,
            val monthlyIncome: List<MonthAmount>,
            val monthlyExpense: List<MonthAmount>,
            val summary: MonthlySummary,
        )

        private data class SearchState(
            val params: SearchParams,
            val categories: List<Category>,
            val results: List<Transaction>,
        )

        val activeTab = MutableStateFlow(ReportsTab.CHARTS)

        private val chartParams =
            MutableStateFlow(
                run {
                    val now = ShamsiCalendar.now().monthKey
                    ChartParams(month = now, trendFrom = now.plusMonths(-5), trendTo = now, period = ChartPeriod.EXPENSE)
                },
            )

        private val searchParams = MutableStateFlow(SearchParams())

        private val categoryBreakdown: StateFlow<List<CategoryAmount>> =
            chartParams
                .flatMapLatest { p -> getCategoryBreakdown(p.month, p.period.toType()) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

        private val dailyTotals: StateFlow<List<DayAmount>> =
            chartParams
                .flatMapLatest { p -> getDailyTotals(p.month, p.period.toType()) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

        private val monthlyIncomeTotals: StateFlow<List<MonthAmount>> =
            chartParams
                .flatMapLatest { p -> getMonthlyTotals(TransactionType.INCOME, p.trendFrom, p.trendTo) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

        private val monthlyExpenseTotals: StateFlow<List<MonthAmount>> =
            chartParams
                .flatMapLatest { p -> getMonthlyTotals(TransactionType.EXPENSE, p.trendFrom, p.trendTo) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

        private val monthlySummary: StateFlow<MonthlySummary> =
            chartParams
                .flatMapLatest { p -> getMonthlySummary(p.month) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MonthlySummary.Empty)

        private val availableCategories: StateFlow<List<Category>> =
            searchParams
                .flatMapLatest { sp ->
                    when (sp.type) {
                        TransactionType.INCOME -> getCategories(TransactionType.INCOME)
                        TransactionType.EXPENSE -> getCategories(TransactionType.EXPENSE)
                        null ->
                            combine(
                                getCategories(TransactionType.INCOME),
                                getCategories(TransactionType.EXPENSE),
                            ) { income, expense -> income + expense }
                    }
                }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

        private val searchResults: StateFlow<List<Transaction>> =
            searchParams
                .flatMapLatest { p ->
                    searchTransactions(
                        TransactionFilter(
                            from = p.from,
                            to = p.to,
                            type = p.type,
                            titleQuery = p.title.takeIf { it.isNotBlank() },
                            categoryQuery = p.selectedCategory?.name,
                        ),
                    )
                }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

        val uiState: StateFlow<ReportsUiState> =
            combine(
                activeTab,
                combine(
                    combine(chartParams, categoryBreakdown, dailyTotals) { cp, bd, daily ->
                        Triple(cp, bd, daily)
                    },
                    combine(monthlyIncomeTotals, monthlyExpenseTotals, monthlySummary) { inc, exp, sum ->
                        Triple(inc, exp, sum)
                    },
                ) { chartBase, chartTrend ->
                    val (cp, bd, daily) = chartBase
                    val (inc, exp, sum) = chartTrend
                    ChartState(cp, bd, daily, inc, exp, sum)
                },
                combine(searchParams, availableCategories, searchResults) { sp, cats, results ->
                    SearchState(sp, cats, results)
                },
            ) { tab, chart, search ->
                ReportsUiState(
                    activeTab = tab,
                    chartPeriod = chart.params.period,
                    selectedMonth = chart.params.month,
                    trendFromMonth = chart.params.trendFrom,
                    trendToMonth = chart.params.trendTo,
                    categoryBreakdown = chart.breakdown,
                    dailyTotals = chart.daily,
                    monthlyIncomeTotals = chart.monthlyIncome,
                    monthlyExpenseTotals = chart.monthlyExpense,
                    monthlySummary = chart.summary,
                    searchResults = search.results,
                    titleQuery = search.params.title,
                    availableCategories = search.categories,
                    selectedCategory = search.params.selectedCategory,
                    filterType = search.params.type,
                    filterFromMonth = search.params.from,
                    filterToMonth = search.params.to,
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportsUiState())

        fun setTab(tab: ReportsTab) {
            activeTab.value = tab
        }

        fun setChartPeriod(period: ChartPeriod) {
            chartParams.update { it.copy(period = period) }
        }

        fun setSelectedMonth(month: MonthKey) {
            chartParams.update { it.copy(month = month) }
        }

        fun setTrendFrom(month: MonthKey) {
            chartParams.update { it.copy(trendFrom = month) }
        }

        fun setTrendTo(month: MonthKey) {
            chartParams.update { it.copy(trendTo = month) }
        }

        fun setTitleQuery(query: String) {
            searchParams.update { it.copy(title = query) }
        }

        fun setSelectedCategory(category: Category?) {
            searchParams.update { it.copy(selectedCategory = category) }
        }

        fun setFilterType(type: TransactionType?) {
            // clear the selected category when type changes — income/expense have disjoint lists
            searchParams.update { it.copy(type = type, selectedCategory = null) }
        }

        fun setFilterFrom(month: MonthKey?) {
            searchParams.update { it.copy(from = month) }
        }

        fun setFilterTo(month: MonthKey?) {
            searchParams.update { it.copy(to = month) }
        }

        fun clearFilters() {
            searchParams.value = SearchParams()
        }

        private fun ChartPeriod.toType() =
            when (this) {
                ChartPeriod.EXPENSE -> TransactionType.EXPENSE
                ChartPeriod.INCOME -> TransactionType.INCOME
            }
    }
