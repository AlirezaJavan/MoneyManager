package com.javanapps.moneymanager.feature.categories.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.javanapps.moneymanager.core.domain.category.AddCategoryUseCase
import com.javanapps.moneymanager.core.domain.category.DeleteCategoryUseCase
import com.javanapps.moneymanager.core.domain.category.GetCategoriesUseCase
import com.javanapps.moneymanager.core.domain.category.RenameCategoryUseCase
import com.javanapps.moneymanager.core.domain.transaction.SearchTransactionsUseCase
import com.javanapps.moneymanager.core.domain.transaction.UpdateTransactionUseCase
import com.javanapps.moneymanager.core.model.Category
import com.javanapps.moneymanager.core.model.Transaction
import com.javanapps.moneymanager.core.model.TransactionFilter
import com.javanapps.moneymanager.core.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CategoriesViewModel
    @Inject
    constructor(
        private val getCategories: GetCategoriesUseCase,
        private val addCategory: AddCategoryUseCase,
        private val renameCategory: RenameCategoryUseCase,
        private val deleteCategory: DeleteCategoryUseCase,
        private val searchTransactions: SearchTransactionsUseCase,
        private val updateTransaction: UpdateTransactionUseCase,
    ) : ViewModel() {
        private val selectedType = MutableStateFlow(TransactionType.EXPENSE)

        val uiState: StateFlow<CategoriesUiState> =
            selectedType
                .flatMapLatest { type ->
                    getCategories(type).map { CategoriesUiState(type = type, categories = it) }
                }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CategoriesUiState())

        private val _deletionCategory = MutableStateFlow<Category?>(null)

        val deletionRequest: StateFlow<CategoryDeletionRequest?> =
            _deletionCategory
                .flatMapLatest { category ->
                    if (category == null) return@flatMapLatest flowOf(null)
                    combine(
                        searchTransactions(TransactionFilter(type = category.type, categoryQuery = category.name)),
                        uiState,
                    ) { allMatches, state ->
                        CategoryDeletionRequest(
                            category = category,
                            linkedTransactions = allMatches.filter { it.categoryName == category.name },
                            otherCategoryNames = state.categories.filter { it.id != category.id }.map { it.name },
                        )
                    }
                }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

        fun onTypeChange(type: TransactionType) {
            selectedType.value = type
        }

        fun addCategory(name: String) {
            viewModelScope.launch { addCategory(name, selectedType.value) }
        }

        fun renameCategory(
            category: Category,
            newName: String,
        ) {
            viewModelScope.launch { renameCategory(category.id, newName) }
        }

        /** Shows a confirmation/reassignment dialog before deletion. */
        fun onDeleteClicked(category: Category) {
            _deletionCategory.value = category
        }

        /** Moves a single transaction to a different category. The reactive flow auto-updates the list. */
        fun reassignTransaction(
            transaction: Transaction,
            newCategoryName: String,
        ) {
            viewModelScope.launch { updateTransaction(transaction.copy(categoryName = newCategoryName)) }
        }

        /** Deletes the category once all transactions have been reassigned. */
        fun confirmDelete() {
            val request = deletionRequest.value ?: return
            if (request.linkedTransactions.isNotEmpty()) return
            viewModelScope.launch {
                deleteCategory(request.category)
                _deletionCategory.value = null
            }
        }

        fun cancelDeletion() {
            _deletionCategory.value = null
        }
    }
