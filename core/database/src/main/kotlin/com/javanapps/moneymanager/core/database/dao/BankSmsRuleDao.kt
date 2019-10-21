package com.javanapps.moneymanager.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.javanapps.moneymanager.core.database.model.BankSmsRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BankSmsRuleDao {
    @Query("SELECT * FROM bank_sms_rules ORDER BY bank_name")
    fun observeAll(): Flow<List<BankSmsRuleEntity>>

    @Query("SELECT * FROM bank_sms_rules WHERE enabled = 1")
    suspend fun getEnabled(): List<BankSmsRuleEntity>

    @Upsert
    suspend fun upsert(rule: BankSmsRuleEntity): Long

    @androidx.room.Insert(onConflict = androidx.room.OnConflictStrategy.IGNORE)
    suspend fun insertAllIgnoringConflicts(rules: List<BankSmsRuleEntity>)

    @Query("SELECT COUNT(*) FROM bank_sms_rules")
    suspend fun count(): Int

    @Query("DELETE FROM bank_sms_rules WHERE id = :id")
    suspend fun deleteById(id: Long)
}
