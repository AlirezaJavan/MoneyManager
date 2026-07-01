package com.javanapps.moneymanager.core.model

import io.github.alirezajavan.shamsipicker.model.MonthKey

/**
 * Filter for searching transactions and driving the report charts. Maps 1:1 to the legacy
 * `SearchModel`: an optional Shamsi month range ([from]..[to]), an optional [type], and optional
 * substring matches on title and category. A null field means "no constraint".
 */
data class TransactionFilter(
    val from: MonthKey? = null,
    val to: MonthKey? = null,
    val type: TransactionType? = null,
    val titleQuery: String? = null,
    val categoryQuery: String? = null,
) {
    companion object {
        val None = TransactionFilter()
    }
}
