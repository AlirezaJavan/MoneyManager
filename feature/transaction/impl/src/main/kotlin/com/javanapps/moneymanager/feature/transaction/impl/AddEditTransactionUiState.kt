package com.javanapps.moneymanager.feature.transaction.impl

import com.javanapps.moneymanager.core.model.Category
import com.javanapps.moneymanager.core.model.TransactionType
import io.github.alirezajavan.shamsipicker.model.ShamsiDate

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
    val saved: Boolean = false,
) {
    val isEditing: Boolean get() = editingId != null
}
