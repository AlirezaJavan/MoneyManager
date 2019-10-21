package com.javanapps.moneymanager.feature.categories.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.javanapps.moneymanager.core.model.Category
import com.javanapps.moneymanager.core.model.Transaction
import com.javanapps.moneymanager.core.model.TransactionType
import com.javanapps.moneymanager.core.ui.format.PersianNumber
import com.javanapps.moneymanager.core.ui.format.ShamsiDateFormatter

@Composable
fun CategoriesRoute(viewModel: CategoriesViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val deletionRequest by viewModel.deletionRequest.collectAsStateWithLifecycle()
    CategoriesScreen(
        uiState = uiState,
        deletionRequest = deletionRequest,
        onTypeChange = viewModel::onTypeChange,
        onAdd = viewModel::addCategory,
        onRename = viewModel::renameCategory,
        onDelete = viewModel::onDeleteClicked,
        onReassignTransaction = viewModel::reassignTransaction,
        onConfirmDelete = viewModel::confirmDelete,
        onCancelDeletion = viewModel::cancelDeletion,
    )
}

@Composable
internal fun CategoriesScreen(
    uiState: CategoriesUiState,
    deletionRequest: CategoryDeletionRequest?,
    onTypeChange: (TransactionType) -> Unit,
    onAdd: (String) -> Unit,
    onRename: (Category, String) -> Unit,
    onDelete: (Category) -> Unit,
    onReassignTransaction: (Transaction, String) -> Unit,
    onConfirmDelete: () -> Unit,
    onCancelDeletion: () -> Unit,
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<Category?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.feature_categories_impl_categories_add_desc))
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            val selectedIndex = if (uiState.type == TransactionType.EXPENSE) 0 else 1
            PrimaryTabRow(selectedTabIndex = selectedIndex) {
                Tab(
                    selected = selectedIndex == 0,
                    onClick = { onTypeChange(TransactionType.EXPENSE) },
                    text = { Text(stringResource(R.string.feature_categories_impl_categories_tab_expense)) },
                )
                Tab(
                    selected = selectedIndex == 1,
                    onClick = { onTypeChange(TransactionType.INCOME) },
                    text = { Text(stringResource(R.string.feature_categories_impl_categories_tab_income)) },
                )
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding =
                    PaddingValues(
                        start = 16.dp,
                        top = 16.dp,
                        end = 16.dp,
                        bottom = 80.dp,
                    ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(items = uiState.categories, key = { it.id }) { category ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(category.name, style = MaterialTheme.typography.bodyLarge)
                            Row {
                                IconButton(onClick = { renameTarget = category }) {
                                    Icon(
                                        Icons.Filled.Edit,
                                        contentDescription = stringResource(R.string.feature_categories_impl_categories_edit_desc),
                                    )
                                }
                                IconButton(onClick = { onDelete(category) }) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = stringResource(R.string.feature_categories_impl_categories_delete_desc),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        CategoryNameDialog(
            title = stringResource(R.string.feature_categories_impl_categories_dialog_add_title),
            initial = "",
            onConfirm = {
                onAdd(it)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false },
        )
    }

    renameTarget?.let { target ->
        CategoryNameDialog(
            title = stringResource(R.string.feature_categories_impl_categories_dialog_rename_title),
            initial = target.name,
            onConfirm = {
                onRename(target, it)
                renameTarget = null
            },
            onDismiss = { renameTarget = null },
        )
    }

    if (deletionRequest != null) {
        CategoryDeletionDialog(
            request = deletionRequest,
            onReassignTransaction = onReassignTransaction,
            onDelete = onConfirmDelete,
            onDismiss = onCancelDeletion,
        )
    }
}

@Composable
private fun CategoryNameDialog(
    title: String,
    initial: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.feature_categories_impl_categories_label_name)) },
            )
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name.trim()) }) {
                Text(stringResource(R.string.feature_categories_impl_categories_action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.feature_categories_impl_categories_action_cancel)) }
        },
    )
}

@Composable
private fun CategoryDeletionDialog(
    request: CategoryDeletionRequest,
    onReassignTransaction: (Transaction, String) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.feature_categories_impl_categories_deletion_title, request.category.name)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (request.linkedTransactions.isEmpty()) {
                    Text(stringResource(R.string.feature_categories_impl_categories_deletion_ready))
                } else {
                    Text(
                        stringResource(
                            R.string.feature_categories_impl_categories_deletion_message,
                            PersianNumber.toPersianDigits(request.linkedTransactions.size.toLong()),
                        ),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                        items(items = request.linkedTransactions, key = { it.id }) { tx ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(tx.title, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        "${PersianNumber.toman(tx.amountToman)} · ${ShamsiDateFormatter.short(tx.date)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                IconButton(onClick = { editingTransaction = tx }) {
                                    Icon(
                                        Icons.Filled.Edit,
                                        contentDescription = stringResource(R.string.feature_categories_impl_categories_deletion_edit_desc),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDelete,
                enabled = request.linkedTransactions.isEmpty(),
            ) {
                Text(stringResource(R.string.feature_categories_impl_categories_deletion_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.feature_categories_impl_categories_action_cancel))
            }
        },
    )

    editingTransaction?.let { tx ->
        CategoryPickerDialog(
            categoryNames = request.otherCategoryNames,
            onSelect = { name ->
                onReassignTransaction(tx, name)
                editingTransaction = null
            },
            onDismiss = { editingTransaction = null },
        )
    }
}

@Composable
private fun CategoryPickerDialog(
    categoryNames: List<String>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.feature_categories_impl_categories_picker_title)) },
        text = {
            LazyColumn {
                items(categoryNames) { name ->
                    TextButton(
                        onClick = { onSelect(name) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(name)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.feature_categories_impl_categories_action_cancel))
            }
        },
    )
}
