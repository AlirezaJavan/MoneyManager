package com.javanapps.moneymanager.feature.categories.impl

import com.javanapps.moneymanager.core.model.Category
import com.javanapps.moneymanager.core.model.Transaction
import com.javanapps.moneymanager.core.model.TransactionType

data class CategoriesUiState(
    val type: TransactionType = TransactionType.EXPENSE,
    val categories: List<Category> = emptyList(),
)

/**
 * Surfaced when deleting a category that still has linked transactions.
 * The user must reassign every transaction individually before the delete button becomes active.
 */
data class CategoryDeletionRequest(
    val category: Category,
    val linkedTransactions: List<Transaction>,
    val otherCategoryNames: List<String>,
)
