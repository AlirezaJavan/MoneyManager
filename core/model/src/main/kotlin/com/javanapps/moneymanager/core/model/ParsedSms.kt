package com.javanapps.moneymanager.core.model

/**
 * Result of parsing an incoming SMS for bank transaction information.
 *
 * @param amountToman Extracted amount in Toman (already divided from Rial if needed).
 * @param type Whether this is income or expense.
 * @param bankName Display name of the bank or sender.
 * @param confidence 0..100 confidence score; below threshold the overlay still shows for user confirmation.
 * @param rawBody The original SMS body that was parsed.
 * @param sender The SMS sender id.
 * @param ruleId The [BankSmsRule.id] that matched, or null if heuristic.
 * @param timestampMillis Epoch millis the SMS was actually received at (not when it was processed).
 */
data class ParsedSms(
    val amountToman: Long,
    val type: TransactionType,
    val bankName: String,
    val confidence: Int,
    val rawBody: String,
    val sender: String,
    val ruleId: Long? = null,
    val timestampMillis: Long = System.currentTimeMillis(),
) {
    val isHighConfidence: Boolean get() = confidence >= MIN_CONFIDENCE_THRESHOLD

    companion object {
        const val MIN_CONFIDENCE_THRESHOLD = 60
    }
}
