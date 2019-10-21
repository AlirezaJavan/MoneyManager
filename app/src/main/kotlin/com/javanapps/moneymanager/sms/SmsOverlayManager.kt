package com.javanapps.moneymanager.sms

import android.content.Context
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.setViewTreeOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.javanapps.moneymanager.R
import com.javanapps.moneymanager.core.common.calendar.ShamsiCalendar
import com.javanapps.moneymanager.core.data.DefaultCategories
import com.javanapps.moneymanager.core.data.repository.CategoryRepository
import com.javanapps.moneymanager.core.data.repository.TransactionRepository
import com.javanapps.moneymanager.core.designsystem.theme.ExpenseRed
import com.javanapps.moneymanager.core.designsystem.theme.IncomeGreen
import com.javanapps.moneymanager.core.designsystem.theme.MoneyManagerTheme
import com.javanapps.moneymanager.core.model.DarkThemeConfig
import com.javanapps.moneymanager.core.model.ParsedSms
import com.javanapps.moneymanager.core.model.Transaction
import com.javanapps.moneymanager.core.model.TransactionSource
import com.javanapps.moneymanager.core.model.TransactionType
import com.javanapps.moneymanager.core.ui.format.PersianNumber
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsOverlayManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val transactionRepository: TransactionRepository,
        private val categoryRepository: CategoryRepository,
    ) {
        private val windowManager = context.getSystemService(WindowManager::class.java)
        private val mainHandler = Handler(Looper.getMainLooper())
        private var currentView: ComposeView? = null
        private var currentOwner: OverlayLifecycleOwner? = null

        fun canShow(): Boolean = Settings.canDrawOverlays(context)

        /** Must be called from the main thread. */
        fun show(
            transactionId: Long,
            parsed: ParsedSms,
            scope: CoroutineScope,
        ) {
            if (currentView != null) dismissInternal()

            val owner =
                OverlayLifecycleOwner(
                    onFallbackBack = {
                        scope.launch {
                            transactionRepository.delete(transactionId)
                            dismiss()
                        }
                    },
                ).also { currentOwner = it }
            owner.start()

            val view =
                ComposeView(context).apply {
                    setOnKeyListener { _, keyCode, event ->
                        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                            owner.onBackPressedDispatcher.onBackPressed()
                            true
                        } else {
                            false
                        }
                    }
                    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
                    setViewTreeLifecycleOwner(owner)
                    setViewTreeViewModelStoreOwner(owner)
                    setViewTreeSavedStateRegistryOwner(owner)
                    setViewTreeOnBackPressedDispatcherOwner(owner)
                    setContent {
                        CompositionLocalProvider(
                            LocalLifecycleOwner provides owner,
                            LocalOnBackPressedDispatcherOwner provides owner,
                            LocalLayoutDirection provides LayoutDirection.Rtl,
                        ) {
                            MoneyManagerTheme(darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM) {
                                SmsTransactionOverlay(
                                    parsed = parsed,
                                    categoryRepository = categoryRepository,
                                    onSave = { tx ->
                                        scope.launch {
                                            transactionRepository.update(tx.copy(id = transactionId, isPending = false))
                                            dismiss()
                                        }
                                    },
                                    onDismiss = {
                                        scope.launch {
                                            transactionRepository.delete(transactionId)
                                            dismiss()
                                        }
                                    },
                                )
                            }
                        }
                    }
                }

            val params =
                WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                    PixelFormat.TRANSLUCENT,
                )

            currentView = view
            windowManager.addView(view, params)
        }

        /** Safe to call from any thread. */
        fun dismiss() {
            mainHandler.post { dismissInternal() }
        }

        private fun dismissInternal() {
            currentOwner?.stop()
            currentOwner = null
            currentView?.let { view ->
                runCatching { windowManager.removeView(view) }
            }
            currentView = null
        }
    }

private class OverlayLifecycleOwner(
    private val onFallbackBack: () -> Unit,
) : LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner,
    OnBackPressedDispatcherOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val vmStore = ViewModelStore()
    private val savedStateController = SavedStateRegistryController.create(this)
    override val onBackPressedDispatcher = OnBackPressedDispatcher(onFallbackBack)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = vmStore
    override val savedStateRegistry: SavedStateRegistry get() = savedStateController.savedStateRegistry

    fun start() {
        savedStateController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    fun stop() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        vmStore.clear()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SmsTransactionOverlay(
    parsed: ParsedSms,
    categoryRepository: CategoryRepository,
    onSave: (Transaction) -> Unit,
    onDismiss: () -> Unit,
) {
    val categories by categoryRepository.observeAll().collectAsState(initial = emptyList())
    val isIncome = parsed.type == TransactionType.INCOME
    val typeColor = if (isIncome) IncomeGreen else ExpenseRed

    var selectedCategory by remember { mutableStateOf(DefaultCategories.MISC) }
    var note by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(52.dp)
                                .background(typeColor.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = if (isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = typeColor,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.sms_confirm_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text =
                                stringResource(
                                    R.string.sms_confirm_source,
                                    if (isIncome) {
                                        stringResource(R.string.sms_confirm_income)
                                    } else {
                                        stringResource(R.string.sms_confirm_expense)
                                    },
                                    parsed.bankName,
                                ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                HorizontalDivider()

                Text(
                    text = PersianNumber.toman(parsed.amountToman),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = typeColor,
                )

                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = it },
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.sms_confirm_category_label)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(dropdownExpanded) },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                    ) {
                        categories.filter { it.type == parsed.type }.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    selectedCategory = cat.name
                                    dropdownExpanded = false
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text(stringResource(R.string.sms_confirm_dismiss))
                    }
                    Button(
                        onClick = {
                            onSave(
                                Transaction(
                                    id = Transaction.NO_ID,
                                    amountToman = parsed.amountToman,
                                    type = parsed.type,
                                    categoryName = selectedCategory,
                                    title = parsed.bankName,
                                    note = note,
                                    date = ShamsiCalendar.now(),
                                    createdAtEpochMillis = System.currentTimeMillis(),
                                    source = TransactionSource.SMS,
                                    isPending = false,
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
}
