package com.javanapps.moneymanager.feature.transaction.impl

import com.javanapps.moneymanager.core.model.Category
import com.javanapps.moneymanager.core.model.ShamsiDate
import com.javanapps.moneymanager.core.model.TransactionType

data class AddEditTransactionUiState(
    val editingId: Long? = null,
    val type: TransactionType = TransactionType.EXPENSE,
    val amountText: String = "",
    val title: String = "",
    val note: String = "",
    val date: ShamsiDate,
    val categories: List<Category> = emptyList(),
    val selectedCategory: String? = null,
    val amountError: Boolean = false,
) {
    val isEditing: Boolean get() = editingId != null
}
