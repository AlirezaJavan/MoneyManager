package com.javanapps.moneymanager.core.data.repository

import com.javanapps.moneymanager.core.model.Category
import com.javanapps.moneymanager.core.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeByType(type: TransactionType): Flow<List<Category>>

    fun observeAll(): Flow<List<Category>>

    suspend fun exists(
        name: String,
        type: TransactionType,
    ): Boolean

    suspend fun add(
        name: String,
        type: TransactionType,
    ): Long

    suspend fun rename(
        id: Long,
        newName: String,
    )

    suspend fun delete(id: Long)

    /** Inserts the default category set, ignoring any that already exist. */
    suspend fun seedDefaultsIfEmpty()

    /** Bulk-inserts categories (e.g. from legacy import), ignoring duplicates. */
    suspend fun import(categories: List<Category>)
}
