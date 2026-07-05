package com.javanapps.moneymanager.feature.transaction.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.javanapps.moneymanager.core.domain.category.AddCategoryUseCase
import com.javanapps.moneymanager.core.domain.category.GetCategoriesUseCase
import com.javanapps.moneymanager.core.domain.category.RenameCategoryUseCase
import com.javanapps.moneymanager.core.domain.transaction.AddTransactionUseCase
import com.javanapps.moneymanager.core.domain.transaction.DeleteTransactionUseCase
import com.javanapps.moneymanager.core.domain.transaction.GetTransactionUseCase
import com.javanapps.moneymanager.core.domain.transaction.UpdateTransactionUseCase
import com.javanapps.moneymanager.core.model.Category
import com.javanapps.moneymanager.core.model.Transaction
import com.javanapps.moneymanager.core.model.TransactionSource
import com.javanapps.moneymanager.core.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.alirezajavan.shamsipicker.calendar.ShamsiCalendar
import io.github.alirezajavan.shamsipicker.format.PersianNumber
import io.github.alirezajavan.shamsipicker.model.ShamsiDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private data class FormState(
    val editingId: Long? = null,
    val type: TransactionType = TransactionType.EXPENSE,
    val amountText: String = "",
    val title: String = "",
    val note: String = "",
    val date: ShamsiDate,
    val selectedCategory: String? = null,
    val source: TransactionSource = TransactionSource.MANUAL,
    val amountError: Boolean = false,
    val saved: Boolean = false,
)

sealed interface AddEditTransactionEvent {
    data object SaveSuccess : AddEditTransactionEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AddEditTransactionViewModel
    @Inject
    constructor(
        private val getCategories: GetCategoriesUseCase,
        private val getTransaction: GetTransactionUseCase,
        private val addTransaction: AddTransactionUseCase,
        private val updateTransaction: UpdateTransactionUseCase,
        private val deleteTransaction: DeleteTransactionUseCase,
        private val addCategory: AddCategoryUseCase,
        private val renameCategory: RenameCategoryUseCase,
    ) : ViewModel() {
        private val form = MutableStateFlow(FormState(date = ShamsiCalendar.now()))

        private val _events = Channel<AddEditTransactionEvent>(Channel.BUFFERED)
        val events = _events.receiveAsFlow()

        private val categories =
            form
                .map { it.type }
                .distinctUntilChanged()
                .flatMapLatest { getCategories(it) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

        val uiState: StateFlow<AddEditTransactionUiState> =
            combine(form, categories) { f, cats ->
                AddEditTransactionUiState(
                    editingId = f.editingId,
                    type = f.type,
                    amountText =
                        if (f.amountText.isEmpty()) {
                            ""
                        } else {
                            f.amountText.toLongOrNull()?.let { PersianNumber.grouped(it) } ?: f.amountText
                        },
                    title = f.title,
                    note = f.note,
                    date = f.date,
                    categories = cats,
                    selectedCategory = f.selectedCategory ?: cats.firstOrNull { it.name == "متفرقه" }?.name ?: cats.firstOrNull()?.name,
                    amountError = f.amountError,
                    saved = f.saved,
                )
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                AddEditTransactionUiState(date = form.value.date),
            )

        fun load(transactionId: Long?) {
            // If we are already editing this transaction, don't reload.
            // This prevents losing user input if the screen recomposes.
            val current = form.value
            if (!current.saved &&
                current.editingId == transactionId &&
                (transactionId != null || current.amountText.isNotEmpty() || current.title.isNotEmpty())
            ) {
                return
            }

            // Immediately reset the form state for the new session
            if (transactionId == null) {
                form.value = FormState(date = ShamsiCalendar.now())
            } else {
                form.value = FormState(editingId = transactionId, date = ShamsiCalendar.now())
                viewModelScope.launch {
                    getTransaction(transactionId)?.let { tx ->
                        form.value =
                            FormState(
                                editingId = tx.id,
                                type = tx.type,
                                amountText = tx.amountToman.toString(),
                                title = tx.title,
                                note = tx.note,
                                date = tx.date,
                                selectedCategory = tx.categoryName,
                                source = tx.source,
                            )
                    }
                }
            }
        }

        fun onTypeChange(type: TransactionType) = form.update { it.copy(type = type, selectedCategory = null) }

        fun onAmountChange(value: String) {
            val clean =
                value
                    .replace("٬", "")
                    .replace(",", "")
                    .map { ch ->
                        when (ch) {
                            in '۰'..'۹' -> '0' + (ch - '۰')
                            in '٠'..'٩' -> '0' + (ch - '٠')
                            else -> ch
                        }
                    }.joinToString("")
                    .filter { it.isDigit() }
            form.update { it.copy(amountText = clean, amountError = false) }
        }

        fun onTitleChange(value: String) = form.update { it.copy(title = value) }

        fun onNoteChange(value: String) = form.update { it.copy(note = value) }

        fun onCategorySelected(name: String) = form.update { it.copy(selectedCategory = name) }

        fun onAddCategory(name: String) {
            val type = form.value.type
            viewModelScope.launch {
                addCategory(name, type)
                form.update { it.copy(selectedCategory = name) }
            }
        }

        fun onRenameCategory(
            category: Category,
            newName: String,
        ) {
            viewModelScope.launch {
                renameCategory(category.id, newName)
                if (form.value.selectedCategory == category.name) {
                    form.update { it.copy(selectedCategory = newName) }
                }
            }
        }

        fun onDateChange(date: ShamsiDate) =
            form.update { it.copy(date = it.date.copy(year = date.year, month = date.month, day = date.day)) }

        fun onTimeChange(
            hour: Int,
            minute: Int,
        ) = form.update { it.copy(date = it.date.copy(hour = hour, minute = minute)) }

        fun save() {
            val current = form.value
            val amount = current.amountText.toLongOrNull()
            if (amount == null || amount <= 0) {
                form.update { it.copy(amountError = true) }
                return
            }
            val category = uiState.value.selectedCategory ?: return
            viewModelScope.launch {
                val transaction =
                    Transaction(
                        id = current.editingId ?: Transaction.NO_ID,
                        amountToman = amount,
                        type = current.type,
                        categoryName = category,
                        title = current.title.trim(),
                        note = current.note.trim(),
                        date = current.date,
                        createdAtEpochMillis = ShamsiCalendar.toEpochMillis(current.date),
                        source = current.source,
                    )
                if (current.editingId == null) addTransaction(transaction) else updateTransaction(transaction)
                form.update { it.copy(saved = true) }
                _events.send(AddEditTransactionEvent.SaveSuccess)
            }
        }

        fun delete() {
            val id = form.value.editingId ?: return
            viewModelScope.launch {
                deleteTransaction(id)
                form.update { it.copy(saved = true) }
                _events.send(AddEditTransactionEvent.SaveSuccess)
            }
        }
    }
