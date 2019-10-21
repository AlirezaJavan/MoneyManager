package com.javanapps.moneymanager.core.data.repository

import com.javanapps.moneymanager.core.data.DefaultCategories
import com.javanapps.moneymanager.core.database.dao.CategoryDao
import com.javanapps.moneymanager.core.database.model.CategoryEntity
import com.javanapps.moneymanager.core.database.model.asEntity
import com.javanapps.moneymanager.core.database.model.asExternalModel
import com.javanapps.moneymanager.core.model.Category
import com.javanapps.moneymanager.core.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class DefaultCategoryRepository
    @Inject
    constructor(
        private val dao: CategoryDao,
    ) : CategoryRepository {
        override fun observeByType(type: TransactionType): Flow<List<Category>> =
            dao.observeByType(type.name).map { list -> list.map { it.asExternalModel() } }

        override fun observeAll(): Flow<List<Category>> = dao.observeAll().map { list -> list.map { it.asExternalModel() } }

        override suspend fun exists(
            name: String,
            type: TransactionType,
        ): Boolean = dao.countByNameAndType(name, type.name) > 0

        override suspend fun add(
            name: String,
            type: TransactionType,
        ): Long = dao.upsert(CategoryEntity(name = name.trim(), type = type))

        override suspend fun rename(
            id: Long,
            newName: String,
        ) = dao.rename(id, newName.trim())

        override suspend fun delete(id: Long) = dao.deleteById(id)

        override suspend fun seedDefaultsIfEmpty() {
            if (dao.count() == 0) {
                dao.insertAllIgnoringConflicts(DefaultCategories.all().map { it.asEntity() })
            }
        }

        override suspend fun import(categories: List<Category>) {
            dao.insertAllIgnoringConflicts(categories.map { it.asEntity() })
        }
    }
