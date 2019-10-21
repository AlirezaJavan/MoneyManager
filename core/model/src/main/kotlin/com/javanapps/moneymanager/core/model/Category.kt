package com.javanapps.moneymanager.core.model

/**
 * A user-defined money category (e.g. «خوراک», «حقوق ماهانه»).
 *
 * A category is scoped to a [type]: expense categories and income categories are independent lists,
 * mirroring the legacy `cat_type` flag. Categories are fully deletable; when a category still has
 * linked transactions the UI first reassigns or removes those transactions (see
 * `PrepareCategoryDeletionUseCase`).
 */
data class Category(
    val id: Long,
    val name: String,
    val type: TransactionType,
) {
    companion object {
        /** Sentinel id for a not-yet-persisted category. */
        const val NO_ID: Long = 0L
    }
}
