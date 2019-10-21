package com.javanapps.moneymanager.core.model

/**
 * A single income or expense entry.
 *
 * The amount is stored as a non-negative magnitude in **Toman** ([amountToman]) together with an
 * explicit [type], which is cleaner than the legacy signed-amount convention. Use [signedAmountToman]
 * when a signed value is needed (e.g. computing a balance or interoperating with legacy data).
 *
 * @param id Stable identifier (0 for a not-yet-persisted transaction).
 * @param amountToman Non-negative amount in Toman.
 * @param type Whether this is income or an expense.
 * @param categoryName Name of the owning category (Persian).
 * @param title Short title/name (e.g. «برداشت از بانک ملت» or a user-entered title).
 * @param note Optional free-text description.
 * @param date Shamsi date & time of the transaction.
 * @param createdAtEpochMillis Wall-clock creation time, used for stable ordering of same-day items.
 * @param source Where the transaction came from.
 * @param isPending Whether this transaction is waiting for user confirmation (e.g. from SMS).
 */
data class Transaction(
    val id: Long,
    val amountToman: Long,
    val type: TransactionType,
    val categoryName: String,
    val title: String,
    val note: String,
    val date: ShamsiDate,
    val createdAtEpochMillis: Long,
    val source: TransactionSource,
    val isPending: Boolean = false,
) {
    init {
        require(amountToman >= 0) { "amountToman must be non-negative; sign is carried by [type]" }
    }

    /** Negative for an expense, positive for income. */
    val signedAmountToman: Long
        get() = if (type == TransactionType.EXPENSE) -amountToman else amountToman

    companion object {
        const val NO_ID: Long = 0L
    }
}
