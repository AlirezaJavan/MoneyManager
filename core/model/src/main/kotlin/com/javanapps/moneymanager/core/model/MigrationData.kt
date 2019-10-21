package com.javanapps.moneymanager.core.model

/** Data read from the legacy SQLite database, ready to be imported into Room. */
data class MigrationData(
    val transactions: List<Transaction>,
    val categories: List<Category>,
)
