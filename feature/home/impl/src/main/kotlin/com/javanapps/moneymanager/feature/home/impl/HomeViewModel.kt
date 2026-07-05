package com.javanapps.moneymanager.feature.home.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.javanapps.moneymanager.core.domain.transaction.GetMonthTransactionsUseCase
import com.javanapps.moneymanager.core.domain.transaction.GetMonthlySummaryUseCase
import com.javanapps.moneymanager.core.model.MonthlySummary
import com.javanapps.moneymanager.core.model.Transaction
import com.javanapps.moneymanager.core.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.alirezajavan.shamsipicker.calendar.ShamsiCalendar
import io.github.alirezajavan.shamsipicker.format.PersianNumber
import io.github.alirezajavan.shamsipicker.model.MonthKey
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
        private val selectedDay = MutableStateFlow<Int?>(null)

        val uiState: StateFlow<HomeUiState> =
            monthKey
                .flatMapLatest { key ->
                    combine(
                        getMonthlySummary(key),
                        getMonthTransactions(key),
                        filter,
                        selectedDay,
                    ) { summary, transactions, activeFilter, day ->
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
                            daySummaries = transactions.toDaySummaries(),
                            selectedDay = day,
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
                            daySummaries = emptyList(),
                            selectedDay = null,
                            isLoading = true,
                        ),
                )

        fun onNextMonth() {
            monthKey.update { it.next() }
            selectedDay.update { null }
        }

        fun onPreviousMonth() {
            monthKey.update { it.previous() }
            selectedDay.update { null }
        }

        fun onFilterChange(newFilter: HomeFilter) = filter.update { newFilter }

        fun onSelectDay(day: Int) = selectedDay.update { day }

        fun onBackToDaySummary() = selectedDay.update { null }

        private fun List<Transaction>.toDaySummaries(): List<DaySummary> =
            groupBy { it.date.day }
                .map { (day, txs) ->
                    DaySummary(
                        day = day,
                        incomeToman = txs.filter { it.type == TransactionType.INCOME }.sumOf { it.amountToman },
                        expenseToman = txs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountToman },
                    )
                }.sortedByDescending { it.day }

        private fun monthTitle(key: MonthKey): String {
            val month = ShamsiCalendar.monthName(key.month)
            return "$month ${PersianNumber.toPersianDigits(key.year.toLong())}"
        }
    }
