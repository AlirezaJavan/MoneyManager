package com.javanapps.moneymanager.feature.reports.impl

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.javanapps.moneymanager.core.designsystem.theme.ExpenseRed
import com.javanapps.moneymanager.core.designsystem.theme.IncomeGreen
import com.javanapps.moneymanager.core.model.Category
import com.javanapps.moneymanager.core.model.CategoryAmount
import com.javanapps.moneymanager.core.model.DayAmount
import com.javanapps.moneymanager.core.model.MonthAmount
import com.javanapps.moneymanager.core.model.MonthlySummary
import com.javanapps.moneymanager.core.model.Transaction
import com.javanapps.moneymanager.core.model.TransactionType
import io.github.alirezajavan.shamsipicker.calendar.ShamsiCalendar
import io.github.alirezajavan.shamsipicker.format.PersianNumber
import io.github.alirezajavan.shamsipicker.format.ShamsiDateFormatter
import io.github.alirezajavan.shamsipicker.model.MonthKey
import kotlin.math.min

// Chart palette — enough contrast for up to 8 categories
private val ChartColors =
    listOf(
        Color(0xFF4E97D1),
        Color(0xFF56AB91),
        Color(0xFFE67E55),
        Color(0xFFB45B8C),
        Color(0xFF8E6DBF),
        Color(0xFFE8A838),
        Color(0xFF5B9E5B),
        Color(0xFFD95B5B),
    )

@Composable
fun ReportsRoute(
    viewModel: ReportsViewModel = hiltViewModel(),
    onEditTransaction: (Long) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ReportsScreen(
        uiState = uiState,
        onTabSelected = viewModel::setTab,
        onPeriodSelected = viewModel::setChartPeriod,
        onMonthSelected = viewModel::setSelectedMonth,
        onTrendFromSelected = viewModel::setTrendFrom,
        onTrendToSelected = viewModel::setTrendTo,
        onTitleQueryChange = viewModel::setTitleQuery,
        onCategorySelected = viewModel::setSelectedCategory,
        onFilterTypeChange = viewModel::setFilterType,
        onFilterFromChange = viewModel::setFilterFrom,
        onFilterToChange = viewModel::setFilterTo,
        onClearFilters = viewModel::clearFilters,
        onEditTransaction = onEditTransaction,
    )
}

@Composable
internal fun ReportsScreen(
    uiState: ReportsUiState,
    onTabSelected: (ReportsTab) -> Unit,
    onPeriodSelected: (ChartPeriod) -> Unit,
    onMonthSelected: (MonthKey) -> Unit,
    onTrendFromSelected: (MonthKey) -> Unit,
    onTrendToSelected: (MonthKey) -> Unit,
    onTitleQueryChange: (String) -> Unit,
    onCategorySelected: (Category?) -> Unit,
    onFilterTypeChange: (TransactionType?) -> Unit,
    onFilterFromChange: (MonthKey?) -> Unit,
    onFilterToChange: (MonthKey?) -> Unit,
    onClearFilters: () -> Unit,
    onEditTransaction: (Long) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        PrimaryScrollableTabRow(selectedTabIndex = uiState.activeTab.ordinal) {
            Tab(
                selected = uiState.activeTab == ReportsTab.CHARTS,
                onClick = { onTabSelected(ReportsTab.CHARTS) },
                text = { Text(stringResource(R.string.feature_reports_impl_reports_tab_charts)) },
            )
            Tab(
                selected = uiState.activeTab == ReportsTab.SEARCH,
                onClick = { onTabSelected(ReportsTab.SEARCH) },
                text = { Text(stringResource(R.string.feature_reports_impl_reports_tab_search)) },
            )
        }

        when (uiState.activeTab) {
            ReportsTab.CHARTS ->
                ChartsTab(
                    uiState = uiState,
                    onPeriodSelected = onPeriodSelected,
                    onMonthSelected = onMonthSelected,
                    onTrendFromSelected = onTrendFromSelected,
                    onTrendToSelected = onTrendToSelected,
                )
            ReportsTab.SEARCH ->
                SearchTab(
                    uiState = uiState,
                    onTitleQueryChange = onTitleQueryChange,
                    onCategorySelected = onCategorySelected,
                    onFilterTypeChange = onFilterTypeChange,
                    onFilterFromChange = onFilterFromChange,
                    onFilterToChange = onFilterToChange,
                    onClearFilters = onClearFilters,
                    onEditTransaction = onEditTransaction,
                )
        }
    }
}

// ─── Charts Tab ──────────────────────────────────────────────────────────────

@Composable
private fun ChartsTab(
    uiState: ReportsUiState,
    onPeriodSelected: (ChartPeriod) -> Unit,
    onMonthSelected: (MonthKey) -> Unit,
    onTrendFromSelected: (MonthKey) -> Unit,
    onTrendToSelected: (MonthKey) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // Monthly summary card (always visible at the top)
        MonthlySummaryCard(
            month = uiState.selectedMonth,
            summary = uiState.monthlySummary,
        )

        // Period selector
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = uiState.chartPeriod == ChartPeriod.EXPENSE,
                onClick = { onPeriodSelected(ChartPeriod.EXPENSE) },
                label = { Text(stringResource(R.string.feature_reports_impl_reports_period_expense)) },
            )
            FilterChip(
                selected = uiState.chartPeriod == ChartPeriod.INCOME,
                onClick = { onPeriodSelected(ChartPeriod.INCOME) },
                label = { Text(stringResource(R.string.feature_reports_impl_reports_period_income)) },
            )
        }

        // Category breakdown (donut chart)
        Text(
            stringResource(
                R.string.feature_reports_impl_reports_category_breakdown_title,
                ShamsiDateFormatter.monthTitle(uiState.selectedMonth),
            ),
            style = MaterialTheme.typography.titleMedium,
        )
        MonthNavigator(month = uiState.selectedMonth, onMonthChange = onMonthSelected)
        if (uiState.categoryBreakdown.isEmpty()) {
            EmptyChartPlaceholder(stringResource(R.string.feature_reports_impl_reports_chart_empty_month))
        } else {
            CategoryDonutChart(data = uiState.categoryBreakdown)
            Spacer(Modifier.height(8.dp))
            CategoryLegend(data = uiState.categoryBreakdown)
        }

        HorizontalDivider()

        // Day-to-day bar chart
        Text(
            stringResource(R.string.feature_reports_impl_reports_daily_chart_title, ShamsiDateFormatter.monthTitle(uiState.selectedMonth)),
            style = MaterialTheme.typography.titleMedium,
        )
        if (uiState.dailyTotals.isEmpty()) {
            EmptyChartPlaceholder(stringResource(R.string.feature_reports_impl_reports_chart_empty_month))
        } else {
            DailyBarChart(data = uiState.dailyTotals)
        }

        HorizontalDivider()

        // Income vs expense monthly comparison chart
        Text(
            stringResource(R.string.feature_reports_impl_reports_monthly_comparison_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.feature_reports_impl_reports_trend_from_label), style = MaterialTheme.typography.bodySmall)
            MonthNavigator(month = uiState.trendFromMonth, onMonthChange = onTrendFromSelected, compact = true)
            Spacer(Modifier.weight(1f))
            Text(stringResource(R.string.feature_reports_impl_reports_trend_to_label), style = MaterialTheme.typography.bodySmall)
            MonthNavigator(month = uiState.trendToMonth, onMonthChange = onTrendToSelected, compact = true)
        }
        if (uiState.monthlyIncomeTotals.isEmpty() && uiState.monthlyExpenseTotals.isEmpty()) {
            EmptyChartPlaceholder(stringResource(R.string.feature_reports_impl_reports_chart_empty_range))
        } else {
            CombinedMonthlyBarChart(
                incomeData = uiState.monthlyIncomeTotals,
                expenseData = uiState.monthlyExpenseTotals,
            )
        }
    }
}

// ─── Monthly Summary Card ────────────────────────────────────────────────────

@Composable
private fun MonthlySummaryCard(
    month: MonthKey,
    summary: MonthlySummary,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                ShamsiDateFormatter.monthTitle(month),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SummaryItem(
                    label = stringResource(R.string.feature_reports_impl_reports_period_income),
                    amount = summary.incomeToman,
                    color = IncomeGreen,
                    modifier = Modifier.weight(1f),
                )
                SummaryItem(
                    label = stringResource(R.string.feature_reports_impl_reports_period_expense),
                    amount = summary.expenseToman,
                    color = ExpenseRed,
                    modifier = Modifier.weight(1f),
                )
                SummaryItem(
                    label = stringResource(R.string.feature_reports_impl_reports_summary_balance),
                    amount = summary.balanceToman,
                    color = if (summary.balanceToman >= 0) IncomeGreen else ExpenseRed,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    amount: Long,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(2.dp))
        Text(
            PersianNumber.toman(amount),
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// ─── Month Navigator ─────────────────────────────────────────────────────────

@Composable
private fun MonthNavigator(
    month: MonthKey,
    onMonthChange: (MonthKey) -> Unit,
    compact: Boolean = false,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick = { onMonthChange(month.previous()) },
            modifier = if (compact) Modifier.size(28.dp) else Modifier,
        ) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.feature_reports_impl_reports_prev_month_desc),
                modifier = if (compact) Modifier.size(16.dp) else Modifier,
            )
        }
        Text(
            ShamsiDateFormatter.monthTitle(month),
            style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = if (compact) 2.dp else 4.dp),
        )
        IconButton(
            onClick = { onMonthChange(month.next()) },
            modifier = if (compact) Modifier.size(28.dp) else Modifier,
        ) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.feature_reports_impl_reports_next_month_desc),
                modifier = if (compact) Modifier.size(16.dp) else Modifier,
            )
        }
    }
}

@Composable
private fun EmptyChartPlaceholder(message: String) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(120.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ─── Donut Chart ─────────────────────────────────────────────────────────────

@Composable
private fun CategoryDonutChart(data: List<CategoryAmount>) {
    val totalAmount = data.sumOf { it.amountToman }
    val totalFloat = totalAmount.toFloat().coerceAtLeast(1f)

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier =
                Modifier
                    .fillMaxWidth(0.75f)
                    .aspectRatio(1f),
        ) {
            val radius = min(size.width, size.height) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            val innerRadius = radius * 0.52f

            var startAngle = -90f
            data.forEachIndexed { index, item ->
                val sweep = (item.amountToman / totalFloat) * 360f
                drawArcSegment(center, innerRadius, radius, startAngle, sweep, ChartColors[index % ChartColors.size])
                startAngle += sweep
            }
        }
        // Center total
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                PersianNumber.toman(totalAmount),
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun DrawScope.drawArcSegment(
    center: Offset,
    innerRadius: Float,
    outerRadius: Float,
    startAngle: Float,
    sweepAngle: Float,
    color: Color,
) {
    drawArc(
        color = color,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = false,
        topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
        size = Size(outerRadius * 2, outerRadius * 2),
        style =
            androidx.compose.ui.graphics.drawscope
                .Stroke(width = outerRadius - innerRadius),
    )
}

@Composable
private fun CategoryLegend(data: List<CategoryAmount>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        data.forEachIndexed { index, item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(12.dp)
                            .background(ChartColors[index % ChartColors.size], CircleShape),
                )
                Text(item.categoryName, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                Text(
                    "${PersianNumber.toman(item.amountToman)}  (${PersianNumber.toPersianDigits(item.percent.toLong())}٪)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ─── Bar Charts ──────────────────────────────────────────────────────────────

@Composable
private fun DailyBarChart(data: List<DayAmount>) {
    val max = data.maxOf { it.amountToman }.toFloat().coerceAtLeast(1f)
    val barColor = ChartColors[0]

    Column {
        Canvas(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(horizontal = 4.dp),
        ) {
            val barCount = data.size
            val barWidth = size.width / (barCount * 1.5f)
            val gap = barWidth * 0.5f

            data.forEachIndexed { index, item ->
                val barHeight = (item.amountToman / max) * size.height
                val left = index * (barWidth + gap) + gap / 2f
                drawRect(
                    color = barColor,
                    topLeft = Offset(left, size.height - barHeight),
                    size = Size(barWidth, barHeight),
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
            data.forEachIndexed { _, item ->
                Text(
                    PersianNumber.toPersianDigits(item.day.toLong()),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun CombinedMonthlyBarChart(
    incomeData: List<MonthAmount>,
    expenseData: List<MonthAmount>,
) {
    val allMonths =
        (incomeData.map { it.monthKey } + expenseData.map { it.monthKey })
            .distinct()
            .sorted()

    val incomeMap = incomeData.associateBy { it.monthKey }
    val expenseMap = expenseData.associateBy { it.monthKey }

    val maxVal =
        (incomeData.maxOfOrNull { it.amountToman } ?: 0L)
            .coerceAtLeast(expenseData.maxOfOrNull { it.amountToman } ?: 0L)
            .toFloat()
            .coerceAtLeast(1f)

    Column {
        Canvas(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .padding(horizontal = 4.dp),
        ) {
            val slotCount = allMonths.size
            if (slotCount == 0) return@Canvas
            val slotWidth = size.width / slotCount
            val barWidth = slotWidth * 0.35f
            val barGap = slotWidth * 0.04f

            allMonths.forEachIndexed { index, monthKey ->
                val incomeAmt = incomeMap[monthKey]?.amountToman ?: 0L
                val expenseAmt = expenseMap[monthKey]?.amountToman ?: 0L
                val slotLeft = index * slotWidth + (slotWidth - barWidth * 2 - barGap) / 2f

                val incomeHeight = (incomeAmt / maxVal) * size.height
                drawRect(
                    color = IncomeGreen,
                    topLeft = Offset(slotLeft, size.height - incomeHeight),
                    size = Size(barWidth, incomeHeight),
                )

                val expenseHeight = (expenseAmt / maxVal) * size.height
                drawRect(
                    color = ExpenseRed,
                    topLeft = Offset(slotLeft + barWidth + barGap, size.height - expenseHeight),
                    size = Size(barWidth, expenseHeight),
                )
            }
        }
        // Month labels
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
            allMonths.forEach { monthKey ->
                Text(
                    ShamsiDateFormatter.monthTitle(monthKey).substringBefore(' '),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
            }
        }
        // Legend
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(10.dp).background(IncomeGreen, CircleShape))
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.feature_reports_impl_reports_period_income), style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.width(16.dp))
            Box(Modifier.size(10.dp).background(ExpenseRed, CircleShape))
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.feature_reports_impl_reports_period_expense), style = MaterialTheme.typography.labelSmall)
        }
    }
}

// ─── Search Tab ──────────────────────────────────────────────────────────────

@Composable
private fun SearchTab(
    uiState: ReportsUiState,
    onTitleQueryChange: (String) -> Unit,
    onCategorySelected: (Category?) -> Unit,
    onFilterTypeChange: (TransactionType?) -> Unit,
    onFilterFromChange: (MonthKey?) -> Unit,
    onFilterToChange: (MonthKey?) -> Unit,
    onClearFilters: () -> Unit,
    onEditTransaction: (Long) -> Unit,
) {
    val incomeTotal = uiState.searchResults.filter { it.type == TransactionType.INCOME }.sumOf { it.amountToman }
    val expenseTotal = uiState.searchResults.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountToman }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Title search
            OutlinedTextField(
                value = uiState.titleQuery,
                onValueChange = onTitleQueryChange,
                label = { Text(stringResource(R.string.feature_reports_impl_reports_search_title_hint)) },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (uiState.titleQuery.isNotBlank()) {
                        IconButton(onClick = { onTitleQueryChange("") }) {
                            Icon(Icons.Default.Clear, null)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            // Type filter chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = uiState.filterType == null,
                    onClick = { onFilterTypeChange(null) },
                    label = { Text(stringResource(R.string.feature_reports_impl_reports_filter_all)) },
                )
                FilterChip(
                    selected = uiState.filterType == TransactionType.EXPENSE,
                    onClick = { onFilterTypeChange(TransactionType.EXPENSE) },
                    label = { Text(stringResource(R.string.feature_reports_impl_reports_filter_expense)) },
                )
                FilterChip(
                    selected = uiState.filterType == TransactionType.INCOME,
                    onClick = { onFilterTypeChange(TransactionType.INCOME) },
                    label = { Text(stringResource(R.string.feature_reports_impl_reports_filter_income)) },
                )
            }

            // Category dropdown
            CategoryDropdown(
                availableCategories = uiState.availableCategories,
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = onCategorySelected,
            )

            // Month range
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MonthRangePicker(
                    label = stringResource(R.string.feature_reports_impl_reports_trend_from),
                    selected = uiState.filterFromMonth,
                    onSelect = onFilterFromChange,
                    modifier = Modifier.weight(1f),
                )
                MonthRangePicker(
                    label = stringResource(R.string.feature_reports_impl_reports_trend_to),
                    selected = uiState.filterToMonth,
                    onSelect = onFilterToChange,
                    modifier = Modifier.weight(1f),
                )
            }

            // Clear filters button
            val hasFilters =
                uiState.titleQuery.isNotBlank() ||
                    uiState.selectedCategory != null ||
                    uiState.filterType != null ||
                    uiState.filterFromMonth != null ||
                    uiState.filterToMonth != null
            if (hasFilters) {
                TextButton(onClick = onClearFilters) {
                    Text(stringResource(R.string.feature_reports_impl_reports_clear_filters))
                }
            }
        }

        HorizontalDivider()

        if (uiState.searchResults.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.feature_reports_impl_reports_search_empty),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    SearchResultsSummary(
                        count = uiState.searchResults.size,
                        incomeTotal = incomeTotal,
                        expenseTotal = expenseTotal,
                    )
                }
                items(uiState.searchResults, key = { it.id }) { tx ->
                    SearchResultRow(tx = tx, onClick = { onEditTransaction(tx.id) })
                    HorizontalDivider()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    availableCategories: List<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val displayText = selectedCategory?.name ?: stringResource(R.string.feature_reports_impl_reports_category_all)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.feature_reports_impl_reports_search_category_label)) },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (selectedCategory != null) {
                        IconButton(
                            onClick = {
                                onCategorySelected(null)
                                expanded = false
                            },
                            modifier = Modifier.size(24.dp),
                        ) {
                            Icon(Icons.Default.Clear, null, modifier = Modifier.size(16.dp))
                        }
                    }
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.feature_reports_impl_reports_category_all)) },
                onClick = {
                    onCategorySelected(null)
                    expanded = false
                },
                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
            )
            availableCategories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@Composable
private fun SearchResultsSummary(
    count: Int,
    incomeTotal: Long,
    expenseTotal: Long,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            stringResource(
                R.string.feature_reports_impl_reports_search_results_count,
                PersianNumber.toPersianDigits(count.toLong()),
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (incomeTotal > 0) {
                Text(
                    stringResource(
                        R.string.feature_reports_impl_reports_search_income_total,
                        PersianNumber.toman(incomeTotal),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = IncomeGreen,
                )
            }
            if (expenseTotal > 0) {
                Text(
                    stringResource(
                        R.string.feature_reports_impl_reports_search_expense_total,
                        PersianNumber.toman(expenseTotal),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = ExpenseRed,
                )
            }
        }
    }
}

@Composable
private fun MonthRangePicker(
    label: String,
    selected: MonthKey?,
    onSelect: (MonthKey?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val current = selected ?: remember { ShamsiCalendar.now().monthKey }

    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (selected != null) ShamsiDateFormatter.monthTitle(selected) else "—",
                style = MaterialTheme.typography.bodySmall,
                modifier =
                    Modifier
                        .weight(1f)
                        .clickable { expanded = !expanded }
                        .padding(4.dp),
            )
            if (selected != null) {
                IconButton(onClick = { onSelect(null) }, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.Clear, null, modifier = Modifier.size(14.dp))
                }
            }
        }
        if (expanded) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onSelect(current.previous()) }) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = stringResource(R.string.feature_reports_impl_reports_prev_month_desc),
                    )
                }
                Text(
                    ShamsiDateFormatter.monthTitle(current),
                    modifier =
                        Modifier.clickable {
                            onSelect(current)
                            expanded = false
                        },
                    style = MaterialTheme.typography.bodySmall,
                )
                IconButton(onClick = { onSelect(current.next()) }) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.feature_reports_impl_reports_next_month_desc),
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    tx: Transaction,
    onClick: () -> Unit,
) {
    val amountColor = if (tx.type == TransactionType.INCOME) IncomeGreen else ExpenseRed

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                tx.title.ifBlank { tx.categoryName },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    tx.categoryName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text("•", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    ShamsiDateFormatter.short(tx.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (tx.note.isNotBlank()) {
                Text(
                    tx.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                PersianNumber.toman(tx.amountToman),
                style = MaterialTheme.typography.bodyMedium,
                color = amountColor,
                maxLines = 1,
                softWrap = false,
            )
            Icon(
                Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
