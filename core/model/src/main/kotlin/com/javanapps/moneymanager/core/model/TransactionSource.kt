package com.javanapps.moneymanager.core.model

/** Where a transaction originated from. */
enum class TransactionSource {
    /** Entered manually by the user. */
    MANUAL,

    /** Created from a parsed bank SMS. */
    SMS,

    /** Imported from the legacy SQLite database during migration. */
    LEGACY_IMPORT,
}
