package com.javanapps.moneymanager.core.testing.repository

import com.javanapps.moneymanager.core.data.repository.BankSmsRuleRepository
import com.javanapps.moneymanager.core.model.BankSmsRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TestBankSmsRuleRepository : BankSmsRuleRepository {
    private val state = MutableStateFlow<List<BankSmsRule>>(emptyList())
    private var nextId = 1L

    fun setRules(rules: List<BankSmsRule>) {
        state.value = rules
        nextId = (rules.maxOfOrNull { it.id } ?: 0L) + 1
    }

    override fun observeAll(): Flow<List<BankSmsRule>> = state.asStateFlow()

    override suspend fun enabledRules(): List<BankSmsRule> = state.value.filter { it.enabled }

    override suspend fun upsert(rule: BankSmsRule): Long =
        if (rule.id == BankSmsRule.NO_ID) {
            val id = nextId++
            state.value += rule.copy(id = id)
            id
        } else {
            state.value = state.value.map { if (it.id == rule.id) rule else it }
            rule.id
        }

    override suspend fun delete(id: Long) {
        state.value = state.value.filterNot { it.id == id }
    }

    override suspend fun seedDefaultsIfEmpty() {
        // no-op for tests
    }
}
