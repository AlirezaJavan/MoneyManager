package com.javanapps.moneymanager.sms

import android.app.NotificationManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.javanapps.moneymanager.R
import com.javanapps.moneymanager.core.data.repository.CategoryRepository
import com.javanapps.moneymanager.core.designsystem.theme.ExpenseRed
import com.javanapps.moneymanager.core.designsystem.theme.IncomeGreen
import com.javanapps.moneymanager.core.model.Transaction
import com.javanapps.moneymanager.core.model.TransactionType
import io.github.alirezajavan.shamsipicker.format.PersianNumber
import io.github.alirezajavan.shamsipicker.format.ShamsiDateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingTransactionsScreen(
    transactions: List<Transaction>,
    categoryRepository: CategoryRepository,
    onConfirm: (Transaction) -> Unit,
    onRemove: (Long) -> Unit,
) {
    val context = LocalContext.current
    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        topBar = {
            MediumTopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.pending_transactions_title),
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = stringResource(R.string.pending_transactions_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding =
                androidx.compose.foundation.layout
                    .PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(items = transactions, key = { it.id }) { tx ->
                PendingTransactionCard(
                    transaction = tx,
                    categoryRepository = categoryRepository,
                    onSave = { updated ->
                        onConfirm(updated)
                        cancelNotification(context, tx.id)
                    },
                    onDelete = {
                        onRemove(tx.id)
                        cancelNotification(context, tx.id)
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PendingTransactionCard(
    transaction: Transaction,
    categoryRepository: CategoryRepository,
    onSave: (Transaction) -> Unit,
    onDelete: () -> Unit,
) {
    val categories by categoryRepository.observeAll().collectAsState(initial = emptyList())
    val typeColor = if (transaction.type == TransactionType.INCOME) IncomeGreen else ExpenseRed

    var selectedCategory by remember { mutableStateOf(transaction.categoryName) }
    var note by remember { mutableStateOf(transaction.note) }
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column {
                    Text(
                        text = transaction.title,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = "${ShamsiDateFormatter.long(transaction.date)} · ${ShamsiDateFormatter.time(transaction.date)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.pending_transactions_delete),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }

            Text(
                PersianNumber.toman(transaction.amountToman),
                style = MaterialTheme.typography.headlineSmall,
                color = typeColor,
            )

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.sms_confirm_category_label)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    categories.filter { it.type == transaction.type }.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name) },
                            onClick = {
                                selectedCategory = cat.name
                                expanded = false
                            },
                        )
                    }
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(stringResource(R.string.sms_confirm_note_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            TextButton(
                onClick = {
                    onSave(transaction.copy(categoryName = selectedCategory, note = note))
                },
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(stringResource(R.string.sms_confirm_save))
            }
        }
    }
}

private fun cancelNotification(
    context: Context,
    transactionId: Long,
) {
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.cancel(transactionId.toInt())
}
