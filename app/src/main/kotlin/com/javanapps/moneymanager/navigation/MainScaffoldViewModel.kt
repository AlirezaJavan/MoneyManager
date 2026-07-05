package com.javanapps.moneymanager.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.javanapps.moneymanager.core.data.repository.CategoryRepository
import com.javanapps.moneymanager.core.data.repository.PreferencesRepository
import com.javanapps.moneymanager.core.domain.category.AddCategoryUseCase
import com.javanapps.moneymanager.core.domain.category.RenameCategoryUseCase
import com.javanapps.moneymanager.core.domain.transaction.DeleteTransactionUseCase
import com.javanapps.moneymanager.core.domain.transaction.GetPendingTransactionsUseCase
import com.javanapps.moneymanager.core.domain.transaction.UpdateTransactionUseCase
import com.javanapps.moneymanager.core.model.Transaction
import com.javanapps.moneymanager.core.model.UserData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainScaffoldViewModel
    @Inject
    constructor(
        preferencesRepository: PreferencesRepository,
        getPendingTransactions: GetPendingTransactionsUseCase,
        private val updateTransaction: UpdateTransactionUseCase,
        private val deleteTransaction: DeleteTransactionUseCase,
        val categoryRepository: CategoryRepository,
        val addCategoryUseCase: AddCategoryUseCase,
        val renameCategoryUseCase: RenameCategoryUseCase,
    ) : ViewModel() {
        val userData: StateFlow<UserData?> =
            preferencesRepository.userData
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

        val pendingTransactions: StateFlow<List<Transaction>> =
            getPendingTransactions()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

        fun confirmTransaction(transaction: Transaction) {
            viewModelScope.launch {
                updateTransaction(transaction.copy(isPending = false))
            }
        }

        fun removeTransaction(id: Long) {
            viewModelScope.launch {
                deleteTransaction(id)
            }
        }
    }
