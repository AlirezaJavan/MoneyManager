package com.javanapps.moneymanager.feature.settings.impl

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.javanapps.moneymanager.core.common.calendar.ShamsiCalendar
import com.javanapps.moneymanager.core.model.MonthKey
import com.javanapps.moneymanager.core.model.TransactionType
import com.javanapps.moneymanager.core.ui.format.PersianNumber
import com.javanapps.moneymanager.core.ui.format.ShamsiDateFormatter

@Composable
fun ExportRoute(
    onBack: () -> Unit,
    viewModel: ExportViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ExportScreen(
        uiState = uiState,
        onBack = onBack,
        onSetFromMonth = viewModel::setFromMonth,
        onSetToMonth = viewModel::setToMonth,
        onSetFilterType = viewModel::setFilterType,
        onSetTitleQuery = viewModel::setTitleQuery,
        onSetCategoryQuery = viewModel::setCategoryQuery,
        onClearFilters = viewModel::clearFilters,
        onRequestDbExport = viewModel::requestDbExport,
        onDbPickerLaunched = viewModel::onDbPickerLaunched,
        onExportDatabase = viewModel::exportDatabase,
        onRequestCsvExport = viewModel::requestCsvExport,
        onCsvPickerLaunched = viewModel::onCsvPickerLaunched,
        onExportCsv = viewModel::exportCsv,
        onMessageShown = viewModel::consumeMessage,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExportScreen(
    uiState: ExportUiState,
    onBack: () -> Unit,
    onSetFromMonth: (MonthKey?) -> Unit,
    onSetToMonth: (MonthKey?) -> Unit,
    onSetFilterType: (TransactionType?) -> Unit,
    onSetTitleQuery: (String) -> Unit,
    onSetCategoryQuery: (String) -> Unit,
    onClearFilters: () -> Unit,
    onRequestDbExport: () -> Unit,
    onDbPickerLaunched: () -> Unit,
    onExportDatabase: (Uri) -> Unit,
    onRequestCsvExport: () -> Unit,
    onCsvPickerLaunched: () -> Unit,
    onExportCsv: (Uri) -> Unit,
    onMessageShown: () -> Unit,
) {
    val dbPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("application/octet-stream"),
            onResult = { uri -> uri?.let { onExportDatabase(it) } },
        )
    val csvPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("text/csv"),
            onResult = { uri -> uri?.let { onExportCsv(it) } },
        )

    LaunchedEffect(uiState.launchDbPicker) {
        if (uiState.launchDbPicker) {
            dbPickerLauncher.launch("moneymanager_backup.db")
            onDbPickerLaunched()
        }
    }
    LaunchedEffect(uiState.launchCsvPicker) {
        if (uiState.launchCsvPicker) {
            csvPickerLauncher.launch("moneymanager_transactions.csv")
            onCsvPickerLaunched()
        }
    }
    LaunchedEffect(uiState.exportMessage) {
        if (uiState.exportMessage != null) onMessageShown()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.feature_settings_impl_export_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.feature_settings_impl_export_back_desc),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // ─── Database Backup Section ────────────────────────────────
                ExportSectionHeader(
                    icon = Icons.Default.Storage,
                    title = stringResource(R.string.feature_settings_impl_export_db_section),
                )
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            stringResource(R.string.feature_settings_impl_export_db_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Button(
                            onClick = onRequestDbExport,
                            enabled = !uiState.isExporting,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(R.string.feature_settings_impl_export_db_button))
                        }
                    }
                }

                HorizontalDivider()

                // ─── CSV Export Section ─────────────────────────────────────
                ExportSectionHeader(
                    icon = Icons.Default.Description,
                    title = stringResource(R.string.feature_settings_impl_export_csv_section),
                )

                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        // Type filter chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            FilterChip(
                                selected = uiState.filterType == null,
                                onClick = { onSetFilterType(null) },
                                label = { Text(stringResource(R.string.feature_settings_impl_export_filter_all)) },
                            )
                            FilterChip(
                                selected = uiState.filterType == TransactionType.EXPENSE,
                                onClick = { onSetFilterType(TransactionType.EXPENSE) },
                                label = { Text(stringResource(R.string.feature_settings_impl_export_filter_expense)) },
                            )
                            FilterChip(
                                selected = uiState.filterType == TransactionType.INCOME,
                                onClick = { onSetFilterType(TransactionType.INCOME) },
                                label = { Text(stringResource(R.string.feature_settings_impl_export_filter_income)) },
                            )
                        }

                        // Month range
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            MonthRangePicker(
                                label = stringResource(R.string.feature_settings_impl_export_filter_from),
                                selected = uiState.filterFromMonth,
                                onSelect = onSetFromMonth,
                                modifier = Modifier.weight(1f),
                            )
                            MonthRangePicker(
                                label = stringResource(R.string.feature_settings_impl_export_filter_to),
                                selected = uiState.filterToMonth,
                                onSelect = onSetToMonth,
                                modifier = Modifier.weight(1f),
                            )
                        }

                        // Title search
                        OutlinedTextField(
                            value = uiState.titleQuery,
                            onValueChange = onSetTitleQuery,
                            label = { Text(stringResource(R.string.feature_settings_impl_export_filter_title)) },
                            trailingIcon = {
                                if (uiState.titleQuery.isNotBlank()) {
                                    IconButton(onClick = { onSetTitleQuery("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = null)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )

                        // Category search
                        OutlinedTextField(
                            value = uiState.categoryQuery,
                            onValueChange = onSetCategoryQuery,
                            label = { Text(stringResource(R.string.feature_settings_impl_export_filter_category)) },
                            trailingIcon = {
                                if (uiState.categoryQuery.isNotBlank()) {
                                    IconButton(onClick = { onSetCategoryQuery("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = null)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )

                        if (uiState.hasActiveFilters) {
                            TextButton(
                                onClick = onClearFilters,
                                modifier = Modifier.align(Alignment.End),
                            ) {
                                Text(stringResource(R.string.feature_settings_impl_export_clear_filters))
                            }
                        }

                        Button(
                            onClick = onRequestCsvExport,
                            enabled = !uiState.isExporting,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(R.string.feature_settings_impl_export_csv_button))
                        }
                    }
                }

                uiState.exportMessage?.let { msg ->
                    val text =
                        when (msg) {
                            ExportMessage.DatabaseExported ->
                                stringResource(R.string.feature_settings_impl_export_message_db_done)
                            ExportMessage.CsvExported ->
                                stringResource(R.string.feature_settings_impl_export_message_csv_done)
                            ExportMessage.ExportError ->
                                stringResource(R.string.feature_settings_impl_export_message_error)
                        }
                    Text(
                        text,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                }
            }

            if (uiState.isExporting) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun ExportSectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun MonthRangePicker(
    label: String,
    selected: MonthKey?,
    onSelect: (MonthKey?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDialog by remember { mutableStateOf(false) }

    OutlinedCard(
        onClick = { showDialog = true },
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
    ) {
        Row(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text =
                        if (selected !=
                            null
                        ) {
                            ShamsiDateFormatter.monthTitle(selected)
                        } else {
                            stringResource(R.string.feature_settings_impl_export_not_selected)
                        },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (selected != null) {
                IconButton(onClick = { onSelect(null) }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }

    if (showDialog) {
        MonthSelectorDialog(
            initial = selected ?: ShamsiCalendar.now().monthKey,
            onDismiss = { showDialog = false },
            onSelect = {
                onSelect(it)
                showDialog = false
            },
        )
    }
}

@Composable
private fun MonthSelectorDialog(
    initial: MonthKey,
    onDismiss: () -> Unit,
    onSelect: (MonthKey) -> Unit,
) {
    var year by remember { mutableIntStateOf(initial.year) }
    var month by remember { mutableIntStateOf(initial.month) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.feature_settings_impl_export_select_month)) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Year selection
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    IconButton(onClick = { year-- }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null)
                    }
                    Text(
                        PersianNumber.toPersianDigits(year.toLong()),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    IconButton(onClick = { year++ }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    }
                }
                Spacer(Modifier.height(16.dp))
                // Month selection grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.height(200.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(12) { i ->
                        val m = i + 1
                        val isSelected = m == month
                        FilterChip(
                            selected = isSelected,
                            onClick = { month = m },
                            label = {
                                Text(
                                    ShamsiCalendar.monthName(m),
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSelect(MonthKey(year, month)) }) {
                Text(stringResource(R.string.feature_settings_impl_export_dialog_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.feature_settings_impl_settings_action_cancel))
            }
        },
    )
}
