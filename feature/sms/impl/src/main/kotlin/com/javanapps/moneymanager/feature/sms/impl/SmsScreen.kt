package com.javanapps.moneymanager.feature.sms.impl

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import com.javanapps.moneymanager.core.model.BankSmsRule
import com.javanapps.moneymanager.core.ui.format.PersianNumber

@Composable
fun SmsRoute(viewModel: SmsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SmsScreen(
        uiState = uiState,
        onBodyChange = viewModel::setTeachBody,
        onSenderChange = viewModel::setTeachSender,
        onBankNameChange = viewModel::setTeachBankName,
        onPreview = viewModel::previewLearnedRule,
        onSaveRule = viewModel::saveLearnedRule,
        onResetTeach = viewModel::resetTeach,
        onDeleteRule = viewModel::deleteRule,
        onToggleRule = viewModel::toggleRule,
        onEditRule = viewModel::startEditRule,
    )
}

@Composable
internal fun SmsScreen(
    uiState: SmsUiState,
    onBodyChange: (String) -> Unit,
    onSenderChange: (String) -> Unit,
    onBankNameChange: (String) -> Unit,
    onPreview: () -> Unit,
    onSaveRule: () -> Unit,
    onResetTeach: () -> Unit,
    onDeleteRule: (Long) -> Unit,
    onToggleRule: (BankSmsRule) -> Unit,
    onEditRule: (BankSmsRule) -> Unit,
) {
    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(stringResource(R.string.feature_sms_impl_sms_title), style = MaterialTheme.typography.headlineSmall)
        }

        // Teach-a-rule section
        item {
            TeachRuleCard(
                uiState = uiState,
                onBodyChange = onBodyChange,
                onSenderChange = onSenderChange,
                onBankNameChange = onBankNameChange,
                onPreview = onPreview,
                onSaveRule = onSaveRule,
                onReset = onResetTeach,
            )
        }

        item {
            HorizontalDivider()
            Text(
                stringResource(R.string.feature_sms_impl_sms_rules_title, PersianNumber.toPersianDigits(uiState.rules.size.toLong())),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        if (uiState.rules.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.feature_sms_impl_sms_rules_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(uiState.rules, key = { it.id }) { rule ->
                RuleRow(
                    rule = rule,
                    onDelete = { onDeleteRule(rule.id) },
                    onToggle = { onToggleRule(rule) },
                    onEdit = { onEditRule(rule) },
                )
            }
        }
    }
}

@Composable
private fun TeachRuleCard(
    uiState: SmsUiState,
    onBodyChange: (String) -> Unit,
    onSenderChange: (String) -> Unit,
    onBankNameChange: (String) -> Unit,
    onPreview: () -> Unit,
    onSaveRule: () -> Unit,
    onReset: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val isEditing = uiState.editingRuleId != null
            Text(
                if (isEditing) {
                    stringResource(R.string.feature_sms_impl_sms_edit_section_title)
                } else {
                    stringResource(R.string.feature_sms_impl_sms_teach_section_title)
                },
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                stringResource(R.string.feature_sms_impl_sms_teach_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedTextField(
                value = uiState.teachSampleBody,
                onValueChange = onBodyChange,
                label = { Text(stringResource(R.string.feature_sms_impl_sms_teach_body_hint)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6,
            )
            OutlinedTextField(
                value = uiState.teachSender,
                onValueChange = onSenderChange,
                label = { Text(stringResource(R.string.feature_sms_impl_sms_teach_sender_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = uiState.teachBankName,
                onValueChange = onBankNameChange,
                label = { Text(stringResource(R.string.feature_sms_impl_sms_teach_bank_name_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            when (val result = uiState.teachResult) {
                is TeachResult.Idle -> {}
                is TeachResult.NoMatch ->
                    Text(
                        stringResource(R.string.feature_sms_impl_sms_teach_no_match),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                is TeachResult.Preview -> RulePreview(result.rule)
                is TeachResult.Saved ->
                    Text(
                        stringResource(R.string.feature_sms_impl_sms_teach_saved),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall,
                    )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (uiState.teachResult is TeachResult.Preview) {
                    TextButton(onClick = onSaveRule) {
                        Text(
                            if (isEditing) {
                                stringResource(R.string.feature_sms_impl_sms_teach_update)
                            } else {
                                stringResource(R.string.feature_sms_impl_sms_teach_save)
                            },
                        )
                    }
                    TextButton(onClick = onReset) { Text(stringResource(R.string.feature_sms_impl_sms_teach_cancel)) }
                } else {
                    TextButton(
                        onClick = onPreview,
                        enabled = uiState.teachSampleBody.isNotBlank() && uiState.teachSender.isNotBlank(),
                    ) {
                        Text(stringResource(R.string.feature_sms_impl_sms_teach_preview))
                    }
                    if (isEditing) {
                        TextButton(onClick = onReset) { Text(stringResource(R.string.feature_sms_impl_sms_teach_cancel)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun RulePreview(rule: BankSmsRule) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(R.string.feature_sms_impl_sms_teach_rule_detected), style = MaterialTheme.typography.labelMedium)
            Text(stringResource(R.string.feature_sms_impl_sms_teach_rule_bank, rule.bankName), style = MaterialTheme.typography.bodySmall)
            Text(
                stringResource(R.string.feature_sms_impl_sms_teach_rule_sender, rule.senderPattern),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                stringResource(R.string.feature_sms_impl_sms_teach_rule_income_keywords, rule.incomeKeywords.joinToString()),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                stringResource(R.string.feature_sms_impl_sms_teach_rule_expense_keywords, rule.expenseKeywords.joinToString()),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                if (rule.amountInRial) {
                    stringResource(R.string.feature_sms_impl_sms_teach_rule_unit_rial)
                } else {
                    stringResource(R.string.feature_sms_impl_sms_teach_rule_unit_toman)
                },
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun RuleRow(
    rule: BankSmsRule,
    onDelete: () -> Unit,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onEdit() }
                .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(rule.bankName.ifBlank { rule.senderPattern }, style = MaterialTheme.typography.bodyLarge)
            Text(rule.senderPattern, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = rule.enabled, onCheckedChange = { onToggle() })
        Button(
            onClick = onEdit,
            modifier = Modifier.padding(horizontal = 8.dp),
        ) {
            Text(stringResource(R.string.feature_sms_impl_sms_rule_edit))
        }
        IconButton(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.padding(end = 4.dp),
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = stringResource(R.string.feature_sms_impl_sms_rule_delete_desc),
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.feature_sms_impl_sms_rule_delete_title)) },
            text = {
                Text(
                    stringResource(R.string.feature_sms_impl_sms_rule_delete_message, rule.bankName.ifBlank { rule.senderPattern }),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text(stringResource(R.string.feature_sms_impl_sms_rule_delete_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.feature_sms_impl_sms_rule_delete_dismiss))
                }
            },
        )
    }
}
