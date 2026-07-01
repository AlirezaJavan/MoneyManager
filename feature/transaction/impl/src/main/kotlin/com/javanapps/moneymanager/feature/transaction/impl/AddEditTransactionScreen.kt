package com.javanapps.moneymanager.feature.transaction.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.javanapps.moneymanager.core.model.TransactionType
import io.github.alirezajavan.shamsipicker.calendar.ShamsiCalendar
import io.github.alirezajavan.shamsipicker.format.ShamsiDateFormatter
import io.github.alirezajavan.shamsipicker.model.ShamsiDate
import io.github.alirezajavan.shamsipicker.model.ShamsiDatePickerConfig
import io.github.alirezajavan.shamsipicker.model.ShamsiTime
import io.github.alirezajavan.shamsipicker.model.ShamsiTimePickerConfig
import io.github.alirezajavan.shamsipicker.ui.ShamsiDatePickerDialog
import io.github.alirezajavan.shamsipicker.ui.ShamsiTimePickerDialog
import io.github.alirezajavan.shamsipicker.model.ShamsiDate as PickerDate

@Composable
fun AddEditTransactionRoute(
    transactionId: Long?,
    onDone: () -> Unit,
    viewModel: AddEditTransactionViewModel = hiltViewModel(),
) {
    LaunchedEffect(transactionId) { viewModel.load(transactionId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                AddEditTransactionEvent.SaveSuccess -> onDone()
            }
        }
    }

    AddEditTransactionScreen(
        uiState = uiState,
        onTypeChange = viewModel::onTypeChange,
        onAmountChange = viewModel::onAmountChange,
        onTitleChange = viewModel::onTitleChange,
        onNoteChange = viewModel::onNoteChange,
        onCategorySelected = viewModel::onCategorySelected,
        onDateChange = viewModel::onDateChange,
        onTimeChange = viewModel::onTimeChange,
        onSave = viewModel::save,
        onDelete = viewModel::delete,
        onBack = onDone,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddEditTransactionScreen(
    uiState: AddEditTransactionUiState,
    onTypeChange: (TransactionType) -> Unit,
    onAmountChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onDateChange: (PickerDate) -> Unit,
    onTimeChange: (Int, Int) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var amountFieldValue by remember(uiState.amountText) {
        mutableStateOf(TextFieldValue(text = uiState.amountText, selection = TextRange(uiState.amountText.length)))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.isEditing) {
                            stringResource(R.string.feature_transaction_impl_transaction_edit_title)
                        } else {
                            stringResource(R.string.feature_transaction_impl_transaction_add_title)
                        },
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.feature_transaction_impl_transaction_back_desc),
                        )
                    }
                },
                actions = {
                    if (uiState.isEditing) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = stringResource(R.string.feature_transaction_impl_transaction_delete_desc),
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = uiState.type == TransactionType.EXPENSE,
                    onClick = { onTypeChange(TransactionType.EXPENSE) },
                    label = { Text(stringResource(R.string.feature_transaction_impl_transaction_type_expense)) },
                )
                FilterChip(
                    selected = uiState.type == TransactionType.INCOME,
                    onClick = { onTypeChange(TransactionType.INCOME) },
                    label = { Text(stringResource(R.string.feature_transaction_impl_transaction_type_income)) },
                )
            }

            OutlinedTextField(
                value = amountFieldValue,
                onValueChange = { newValue ->
                    val filtered = newValue.text.filter { ch -> ch.isDigit() || ch == ',' || ch == '٬' }
                    val filteredValue =
                        if (filtered == newValue.text) {
                            newValue
                        } else {
                            TextFieldValue(text = filtered, selection = TextRange(filtered.length))
                        }
                    amountFieldValue = filteredValue
                    onAmountChange(filteredValue.text)
                },
                label = { Text(stringResource(R.string.feature_transaction_impl_transaction_label_amount)) },
                isError = uiState.amountError,
                supportingText =
                    if (uiState.amountError) {
                        { Text(stringResource(R.string.feature_transaction_impl_transaction_error_amount)) }
                    } else {
                        null
                    },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it },
            ) {
                OutlinedTextField(
                    value = uiState.selectedCategory.orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.feature_transaction_impl_transaction_label_category)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false },
                ) {
                    uiState.categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                onCategorySelected(category.name)
                                categoryExpanded = false
                            },
                        )
                    }
                }
            }

            OutlinedTextField(
                value = uiState.title,
                onValueChange = onTitleChange,
                label = { Text(stringResource(R.string.feature_transaction_impl_transaction_label_title)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = uiState.note,
                onValueChange = onNoteChange,
                label = { Text(stringResource(R.string.feature_transaction_impl_transaction_label_note)) },
                modifier = Modifier.fillMaxWidth(),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.weight(1f)) {
                    Text(ShamsiDateFormatter.short(uiState.date))
                }
                OutlinedButton(onClick = { showTimePicker = true }, modifier = Modifier.weight(1f)) {
                    Text(ShamsiDateFormatter.time(uiState.date))
                }
            }

            Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.feature_transaction_impl_transaction_action_save))
            }
        }
    }

    // A transaction can't be dated in the future: the upper bound is the moment this screen opened.
    val entryNow = remember { ShamsiCalendar.now() }
    if (showDatePicker) {
        ShamsiDatePickerDialog(
            onConfirm = { date ->
                onDateChange(
                    ShamsiDate(
                        date.year,
                        date.month,
                        date.day,
                        uiState.date.hour,
                        uiState.date.minute,
                    ),
                )
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
            config =
                ShamsiDatePickerConfig(
                    initialDate = PickerDate(uiState.date.year, uiState.date.month, uiState.date.day),
                    maxDate = PickerDate(entryNow.year, entryNow.month, entryNow.day),
                ),
        )
    }
    if (showTimePicker) {
        // The time ceiling only applies on the entry day itself; earlier days allow any time.
        val onEntryDay =
            uiState.date.year == entryNow.year &&
                uiState.date.month == entryNow.month &&
                uiState.date.day == entryNow.day
        ShamsiTimePickerDialog(
            onConfirm = { time ->
                onTimeChange(time.hour, time.minute)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false },
            config =
                ShamsiTimePickerConfig(
                    initialTime = ShamsiTime(uiState.date.hour, uiState.date.minute),
                    maxTime = if (onEntryDay) ShamsiTime(entryNow.hour, entryNow.minute) else null,
                ),
        )
    }
}
