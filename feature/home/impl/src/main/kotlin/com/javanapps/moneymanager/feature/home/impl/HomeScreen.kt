package com.javanapps.moneymanager.feature.home.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.javanapps.moneymanager.core.designsystem.theme.ExpenseRed
import com.javanapps.moneymanager.core.designsystem.theme.IncomeGreen
import com.javanapps.moneymanager.core.model.Transaction
import com.javanapps.moneymanager.core.model.TransactionType
import io.github.alirezajavan.shamsipicker.calendar.ShamsiCalendar
import io.github.alirezajavan.shamsipicker.format.PersianNumber
import io.github.alirezajavan.shamsipicker.format.ShamsiDateFormatter
import io.github.alirezajavan.shamsipicker.model.MonthKey

@Composable
fun HomeRoute(
    onAddTransaction: () -> Unit,
    onEditTransaction: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        uiState = uiState,
        onNextMonth = viewModel::onNextMonth,
        onPreviousMonth = viewModel::onPreviousMonth,
        onFilterChange = viewModel::onFilterChange,
        onSelectDay = viewModel::onSelectDay,
        onBackToDaySummary = viewModel::onBackToDaySummary,
        onAddTransaction = onAddTransaction,
        onEditTransaction = onEditTransaction,
    )
}

@Composable
internal fun HomeScreen(
    uiState: HomeUiState,
    onNextMonth: () -> Unit,
    onPreviousMonth: () -> Unit,
    onFilterChange: (HomeFilter) -> Unit,
    onSelectDay: (Int) -> Unit,
    onBackToDaySummary: () -> Unit,
    onAddTransaction: () -> Unit,
    onEditTransaction: (Long) -> Unit,
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTransaction) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.feature_home_impl_home_add_transaction_desc))
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            MonthHeader(
                title = uiState.monthTitle,
                onPrevious = onPreviousMonth,
                onNext = onNextMonth,
            )
            SummaryCard(
                incomeToman = uiState.summary.incomeToman,
                expenseToman = uiState.summary.expenseToman,
                balanceToman = uiState.summary.balanceToman,
            )
            FilterRow(selected = uiState.filter, onFilterChange = onFilterChange)
            val selectedDay = uiState.selectedDay
            if (selectedDay == null) {
                if (uiState.daySummaries.isEmpty()) {
                    EmptyState()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding =
                            androidx.compose.foundation.layout
                                .PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(items = uiState.daySummaries, key = { it.day }) { daySummary ->
                            DaySummaryRow(
                                daySummary = daySummary,
                                monthKey = uiState.monthKey,
                                onClick = { onSelectDay(daySummary.day) },
                            )
                        }
                    }
                }
            } else {
                val dayTransactions = uiState.transactions.filter { it.date.day == selectedDay }
                DaySelectionHeader(
                    title = dayTitle(selectedDay, uiState.monthKey),
                    onBack = onBackToDaySummary,
                )
                if (dayTransactions.isEmpty()) {
                    EmptyState(message = stringResource(R.string.feature_home_impl_home_day_empty_state))
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding =
                            androidx.compose.foundation.layout
                                .PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(items = dayTransactions, key = { it.id }) { transaction ->
                            TransactionRow(
                                transaction = transaction,
                                onClick = { onEditTransaction(transaction.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun dayTitle(
    day: Int,
    monthKey: MonthKey,
): String {
    val dayText = PersianNumber.toPersianDigits(day.toLong())
    val monthName = ShamsiCalendar.monthName(monthKey.month)
    return "$dayText $monthName"
}

@Composable
private fun MonthHeader(
    title: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.feature_home_impl_home_prev_month_desc),
            )
        }
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        IconButton(onClick = onNext) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.feature_home_impl_home_next_month_desc),
            )
        }
    }
}

@Composable
private fun SummaryCard(
    incomeToman: Long,
    expenseToman: Long,
    balanceToman: Long,
) {
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SummaryItem(
                stringResource(R.string.feature_home_impl_home_label_income),
                incomeToman,
                IncomeGreen,
                Modifier.weight(1f),
            )
            SummaryItem(
                stringResource(R.string.feature_home_impl_home_label_expense),
                expenseToman,
                ExpenseRed,
                Modifier.weight(1f),
            )
            SummaryItem(
                stringResource(R.string.feature_home_impl_home_label_balance),
                balanceToman,
                MaterialTheme.colorScheme.onSurface,
                Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    amount: Long,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = PersianNumber.toman(amount),
            style = MaterialTheme.typography.titleSmall,
            color = color,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun FilterRow(
    selected: HomeFilter,
    onFilterChange: (HomeFilter) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(selected == HomeFilter.ALL, {
            onFilterChange(HomeFilter.ALL)
        }, { Text(stringResource(R.string.feature_home_impl_home_filter_all)) })
        FilterChip(
            selected == HomeFilter.INCOME,
            { onFilterChange(HomeFilter.INCOME) },
            { Text(stringResource(R.string.feature_home_impl_home_filter_income)) },
        )
        FilterChip(
            selected == HomeFilter.EXPENSE,
            { onFilterChange(HomeFilter.EXPENSE) },
            { Text(stringResource(R.string.feature_home_impl_home_filter_expense)) },
        )
    }
}

@Composable
private fun DaySelectionHeader(
    title: String,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.feature_home_impl_home_back_to_days_desc),
            )
        }
        Text(text = title, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun DaySummaryRow(
    daySummary: DaySummary,
    monthKey: MonthKey,
    onClick: () -> Unit,
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = dayTitle(daySummary.day, monthKey),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = PersianNumber.toman(daySummary.netToman),
                style = MaterialTheme.typography.titleSmall,
                color = if (daySummary.netToman < 0) ExpenseRed else IncomeGreen,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun TransactionRow(
    transaction: Transaction,
    onClick: () -> Unit,
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = transaction.title.ifBlank { transaction.categoryName }, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "${transaction.categoryName} • ${ShamsiDateFormatter.short(transaction.date)}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Text(
                text = PersianNumber.toman(transaction.amountToman),
                style = MaterialTheme.typography.titleSmall,
                color = if (transaction.type == TransactionType.EXPENSE) ExpenseRed else IncomeGreen,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun EmptyState(message: String = stringResource(R.string.feature_home_impl_home_empty_state)) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}
