package com.javanapps.moneymanager.sms

import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.javanapps.moneymanager.R
import com.javanapps.moneymanager.core.common.calendar.ShamsiCalendar
import com.javanapps.moneymanager.core.data.DefaultCategories
import com.javanapps.moneymanager.core.data.repository.CategoryRepository
import com.javanapps.moneymanager.core.data.repository.TransactionRepository
import com.javanapps.moneymanager.core.designsystem.theme.ExpenseRed
import com.javanapps.moneymanager.core.designsystem.theme.IncomeGreen
import com.javanapps.moneymanager.core.designsystem.theme.MoneyManagerTheme
import com.javanapps.moneymanager.core.model.ParsedSms
import com.javanapps.moneymanager.core.model.Transaction
import com.javanapps.moneymanager.core.model.TransactionSource
import com.javanapps.moneymanager.core.model.TransactionType
import com.javanapps.moneymanager.core.ui.format.PersianNumber
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SmsConfirmActivity : ComponentActivity() {
    @Inject lateinit var transactionRepository: TransactionRepository

    @Inject lateinit var categoryRepository: CategoryRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show on lockscreen and turn screen on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
            )
        }
        val keyguard = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        keyguard.requestDismissKeyguard(this, null)

        val txId = extractTransactionId(intent)
        val parsed =
            extractParsedSms(intent) ?: run {
                finish()
                return
            }

        setContent {
            MoneyManagerTheme(darkThemeConfig = com.javanapps.moneymanager.core.model.DarkThemeConfig.FOLLOW_SYSTEM) {
                androidx.compose.runtime.CompositionLocalProvider(
                    LocalLayoutDirection provides LayoutDirection.Rtl,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .background(Color.Transparent),
                        contentAlignment = Alignment.Center,
                    ) {
                        SmsConfirmScreen(
                            parsed = parsed,
                            categoryRepository = categoryRepository,
                            onSave = { tx ->
                                lifecycleScope.launch {
                                    // Update the existing pending transaction and mark as confirmed
                                    transactionRepository.update(
                                        tx.copy(id = txId, isPending = false),
                                    )
                                    dismissNotification(txId)
                                    finish()
                                }
                            },
                            onDismiss = {
                                lifecycleScope.launch {
                                    // Delete the pending transaction if dismissed
                                    transactionRepository.delete(txId)
                                    dismissNotification(txId)
                                    finish()
                                }
                            },
                        )
                    }
                }
            }
        }
    }

    private fun dismissNotification(transactionId: Long) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.cancel(transactionId.toInt())
    }

    companion object {
        private const val EXTRA_ID = "transaction_id"
        private const val EXTRA_AMOUNT = "amount"
        private const val EXTRA_TYPE = "type"
        private const val EXTRA_BANK = "bank"
        private const val EXTRA_BODY = "body"
        private const val EXTRA_SENDER = "sender"
        private const val CONFIDENCE_MANUAL_CONFIRM = 80

        fun createIntent(
            context: Context,
            transactionId: Long,
            parsed: ParsedSms,
        ): Intent =
            Intent(context, SmsConfirmActivity::class.java).apply {
                putExtra(EXTRA_ID, transactionId)
                putExtra(EXTRA_AMOUNT, parsed.amountToman)
                putExtra(EXTRA_TYPE, parsed.type.name)
                putExtra(EXTRA_BANK, parsed.bankName)
                putExtra(EXTRA_BODY, parsed.rawBody)
                putExtra(EXTRA_SENDER, parsed.sender)
            }

        private fun extractTransactionId(intent: Intent): Long = intent.getLongExtra(EXTRA_ID, -1L)

        private fun extractParsedSms(intent: Intent): ParsedSms? {
            val amount = intent.getLongExtra(EXTRA_AMOUNT, -1L).takeIf { it > 0 } ?: return null
            val type = TransactionType.valueOf(intent.getStringExtra(EXTRA_TYPE) ?: return null)
            return ParsedSms(
                amountToman = amount,
                type = type,
                bankName = intent.getStringExtra(EXTRA_BANK) ?: "",
                confidence = CONFIDENCE_MANUAL_CONFIRM,
                rawBody = intent.getStringExtra(EXTRA_BODY) ?: "",
                sender = intent.getStringExtra(EXTRA_SENDER) ?: "",
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SmsConfirmScreen(
    parsed: ParsedSms,
    categoryRepository: CategoryRepository,
    onSave: (Transaction) -> Unit,
    onDismiss: () -> Unit,
) {
    val categories by categoryRepository.observeAll().collectAsState(initial = emptyList())
    val typeColor = if (parsed.type == TransactionType.INCOME) IncomeGreen else ExpenseRed

    var selectedCategory by remember {
        mutableStateOf(DefaultCategories.MISC)
    }
    var note by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.sms_confirm_title), style = MaterialTheme.typography.titleLarge)

            val typeLabel =
                if (parsed.type == TransactionType.INCOME) {
                    stringResource(R.string.sms_confirm_income)
                } else {
                    stringResource(R.string.sms_confirm_expense)
                }
            Text(
                stringResource(R.string.sms_confirm_source, typeLabel, parsed.bankName),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                PersianNumber.toman(parsed.amountToman),
                style = MaterialTheme.typography.headlineMedium,
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
                    categories.filter { it.type == parsed.type }.forEach { cat ->
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.sms_confirm_dismiss)) }
                TextButton(
                    onClick = {
                        val now = ShamsiCalendar.now()
                        onSave(
                            Transaction(
                                id = Transaction.NO_ID,
                                amountToman = parsed.amountToman,
                                type = parsed.type,
                                categoryName = selectedCategory,
                                title = parsed.bankName,
                                note = note,
                                date = now,
                                createdAtEpochMillis = System.currentTimeMillis(),
                                source = TransactionSource.SMS,
                            ),
                        )
                    },
                ) {
                    Text(stringResource(R.string.sms_confirm_save))
                }
            }
        }
    }
}
