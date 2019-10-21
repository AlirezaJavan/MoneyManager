package com.javanapps.moneymanager.feature.home.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.javanapps.moneymanager.core.common.calendar.ShamsiCalendar
import com.javanapps.moneymanager.core.domain.transaction.GetMonthTransactionsUseCase
import com.javanapps.moneymanager.core.domain.transaction.GetMonthlySummaryUseCase
import com.javanapps.moneymanager.core.model.MonthKey
import com.javanapps.moneymanager.core.model.MonthlySummary
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
class HomeViewModel
    @Inject
    constructor(
        private val getMonthlySummary: GetMonthlySummaryUseCase,
        private val getMonthTransactions: GetMonthTransactionsUseCase,
    ) : ViewModel() {
        private val monthKey = MutableStateFlow(ShamsiCalendar.now().monthKey)
        private val filter = MutableStateFlow(HomeFilter.ALL)

        val uiState: StateFlow<HomeUiState> =
            monthKey
                .flatMapLatest { key ->
                    combine(
                        getMonthlySummary(key),
                        getMonthTransactions(key),
                        filter,
                    ) { summary, transactions, activeFilter ->
                        HomeUiState(
                            monthKey = key,
                            monthTitle = monthTitle(key),
                            summary = summary,
                            filter = activeFilter,
                            transactions =
                                transactions.filter { tx ->
                                    when (activeFilter) {
                                        HomeFilter.ALL -> true
                                        HomeFilter.INCOME -> tx.type == TransactionType.INCOME
                                        HomeFilter.EXPENSE -> tx.type == TransactionType.EXPENSE
                                    }
                                },
                            isLoading = false,
                        )
                    }
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue =
                        HomeUiState(
                            monthKey = monthKey.value,
                            monthTitle = monthTitle(monthKey.value),
                            summary = MonthlySummary.Empty,
                            filter = HomeFilter.ALL,
                            transactions = emptyList(),
                            isLoading = true,
                        ),
                )

        fun onNextMonth() = monthKey.update { it.next() }

        fun onPreviousMonth() = monthKey.update { it.previous() }

        fun onFilterChange(newFilter: HomeFilter) = filter.update { newFilter }

        private fun monthTitle(key: MonthKey): String {
            val month = ShamsiCalendar.monthName(key.month)
            return "$month ${key.year}"
        }
    }
