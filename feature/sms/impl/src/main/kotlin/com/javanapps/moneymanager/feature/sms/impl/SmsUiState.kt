package com.javanapps.moneymanager.feature.sms.impl

import com.javanapps.moneymanager.core.model.BankSmsRule

data class SmsUiState(
    val rules: List<BankSmsRule> = emptyList(),
    val teachSampleBody: String = "",
    val teachSender: String = "",
    val teachBankName: String = "",
    val teachResult: TeachResult = TeachResult.Idle,
    val editingRuleId: Long? = null,
)

sealed interface TeachResult {
    data object Idle : TeachResult

    data class Preview(
        val rule: BankSmsRule,
    ) : TeachResult

    data object NoMatch : TeachResult

    data object Saved : TeachResult
}
