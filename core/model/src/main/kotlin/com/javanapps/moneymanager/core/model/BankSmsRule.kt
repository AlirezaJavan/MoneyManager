package com.javanapps.moneymanager.core.model

/**
 * A learned/seeded rule for parsing a bank's SMS. Keyed by [senderPattern] (matched against the SMS
 * sender, which may be a short-code or a text id). Replaces the legacy hardcoded bank list — users
 * teach new banks by pasting a sample SMS and its sender.
 */
data class BankSmsRule(
    val id: Long,
    val senderPattern: String,
    val bankName: String,
    val incomeKeywords: List<String>,
    val expenseKeywords: List<String>,
    val amountInRial: Boolean,
    val defaultCategory: String,
    val sampleBody: String,
    val enabled: Boolean = true,
) {
    companion object {
        const val NO_ID: Long = 0L
    }
}
