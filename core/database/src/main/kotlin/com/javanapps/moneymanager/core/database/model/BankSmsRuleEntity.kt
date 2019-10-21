package com.javanapps.moneymanager.core.database.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A user-taught (or seeded) rule for parsing a bank's SMS, keyed by [senderPattern]. Replaces the
 * legacy hardcoded per-bank logic: the user can teach a new bank by pasting a sample SMS + sender.
 * Extraction details are intentionally simple and self-contained so the parser stays deterministic
 * and testable. See `:core:sms` for how rules are applied.
 */
@Entity(
    tableName = "bank_sms_rules",
    indices = [Index(value = ["sender_pattern"], unique = true)],
)
data class BankSmsRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @androidx.room.ColumnInfo(name = "sender_pattern")
    val senderPattern: String,
    @androidx.room.ColumnInfo(name = "bank_name")
    val bankName: String,
    /** Keywords (comma-separated) that mark an income/deposit, e.g. «واریز,بستانکار». */
    @androidx.room.ColumnInfo(name = "income_keywords")
    val incomeKeywords: String,
    /** Keywords (comma-separated) that mark an expense/withdrawal, e.g. «برداشت,خرید,بدهکار». */
    @androidx.room.ColumnInfo(name = "expense_keywords")
    val expenseKeywords: String,
    /** Whether amounts in this bank's SMS are in Rial (true) and must be divided by 10 to Toman. */
    @androidx.room.ColumnInfo(name = "amount_in_rial")
    val amountInRial: Boolean,
    /** Default category name applied to transactions created from this bank. */
    @androidx.room.ColumnInfo(name = "default_category")
    val defaultCategory: String,
    /** The sample SMS the rule was learned from (for display/editing). */
    @androidx.room.ColumnInfo(name = "sample_body")
    val sampleBody: String,
    @androidx.room.ColumnInfo(name = "enabled")
    val enabled: Boolean = true,
)
