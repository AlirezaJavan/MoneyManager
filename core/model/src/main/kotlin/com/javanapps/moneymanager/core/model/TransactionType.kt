package com.javanapps.moneymanager.core.model

/**
 * The financial direction of a transaction.
 *
 * - [EXPENSE] (مخارج/برداشت): money leaving the account. Stored as a negative signed amount in the
 *   legacy database.
 * - [INCOME] (درآمد/واریز): money entering the account. Stored as a positive signed amount.
 */
enum class TransactionType {
    EXPENSE,
    INCOME,
    ;

    companion object {
        /** Derives the type from a legacy signed amount (negative = expense, positive = income). */
        fun fromSignedAmount(signedAmount: Double): TransactionType = if (signedAmount < 0) EXPENSE else INCOME

        /** Maps the legacy category type flag (0 = expense, 1 = income). */
        fun fromLegacyCategoryType(legacyType: Int): TransactionType = if (legacyType == 1) INCOME else EXPENSE
    }
}
