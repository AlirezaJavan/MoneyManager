package com.javanapps.moneymanager.core.database.model

import com.javanapps.moneymanager.core.model.BankSmsRule

private const val SEPARATOR = ","

private fun String.toKeywordList(): List<String> = split(SEPARATOR).map { it.trim() }.filter { it.isNotEmpty() }

private fun List<String>.toKeywordString(): String = joinToString(SEPARATOR) { it.trim() }

fun BankSmsRuleEntity.asExternalModel(): BankSmsRule =
    BankSmsRule(
        id = id,
        senderPattern = senderPattern,
        bankName = bankName,
        incomeKeywords = incomeKeywords.toKeywordList(),
        expenseKeywords = expenseKeywords.toKeywordList(),
        amountInRial = amountInRial,
        defaultCategory = defaultCategory,
        sampleBody = sampleBody,
        enabled = enabled,
    )

fun BankSmsRule.asEntity(): BankSmsRuleEntity =
    BankSmsRuleEntity(
        id = id,
        senderPattern = senderPattern,
        bankName = bankName,
        incomeKeywords = incomeKeywords.toKeywordString(),
        expenseKeywords = expenseKeywords.toKeywordString(),
        amountInRial = amountInRial,
        defaultCategory = defaultCategory,
        sampleBody = sampleBody,
        enabled = enabled,
    )
