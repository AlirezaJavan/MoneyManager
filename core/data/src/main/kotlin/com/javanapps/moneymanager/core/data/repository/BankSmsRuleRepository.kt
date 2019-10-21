package com.javanapps.moneymanager.core.data.repository

import com.javanapps.moneymanager.core.data.sms.DefaultBankSmsRules
import com.javanapps.moneymanager.core.database.dao.BankSmsRuleDao
import com.javanapps.moneymanager.core.database.model.asEntity
import com.javanapps.moneymanager.core.database.model.asExternalModel
import com.javanapps.moneymanager.core.model.BankSmsRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface BankSmsRuleRepository {
    fun observeAll(): Flow<List<BankSmsRule>>

    suspend fun enabledRules(): List<BankSmsRule>

    suspend fun upsert(rule: BankSmsRule): Long

    suspend fun delete(id: Long)

    suspend fun seedDefaultsIfEmpty()
}

internal class DefaultBankSmsRuleRepository
    @Inject
    constructor(
        private val dao: BankSmsRuleDao,
    ) : BankSmsRuleRepository {
        override fun observeAll(): Flow<List<BankSmsRule>> = dao.observeAll().map { list -> list.map { it.asExternalModel() } }

        override suspend fun enabledRules(): List<BankSmsRule> = dao.getEnabled().map { it.asExternalModel() }

        override suspend fun upsert(rule: BankSmsRule): Long = dao.upsert(rule.asEntity())

        override suspend fun delete(id: Long) = dao.deleteById(id)

        override suspend fun seedDefaultsIfEmpty() {
            if (dao.count() == 0) {
                dao.insertAllIgnoringConflicts(DefaultBankSmsRules.all().map { it.asEntity() })
            }
        }
    }
