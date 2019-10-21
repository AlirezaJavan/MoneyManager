package com.javanapps.moneymanager.core.testing.repository

import com.javanapps.moneymanager.core.data.repository.CategoryRepository
import com.javanapps.moneymanager.core.model.Category
import com.javanapps.moneymanager.core.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class TestCategoryRepository : CategoryRepository {
    private val state = MutableStateFlow<List<Category>>(emptyList())
    private var nextId = 1L

    fun setCategories(categories: List<Category>) {
        state.value = categories
        nextId = (categories.maxOfOrNull { it.id } ?: 0L) + 1
    }

    override fun observeByType(type: TransactionType): Flow<List<Category>> =
        state.map { list -> list.filter { it.type == type }.sortedBy { it.name } }

    override fun observeAll(): Flow<List<Category>> = state

    override suspend fun exists(
        name: String,
        type: TransactionType,
    ): Boolean = state.value.any { it.name == name.trim() && it.type == type }

    override suspend fun add(
        name: String,
        type: TransactionType,
    ): Long {
        val id = nextId++
        state.value = state.value + Category(id, name.trim(), type)
        return id
    }

    override suspend fun rename(
        id: Long,
        newName: String,
    ) {
        state.value = state.value.map { if (it.id == id) it.copy(name = newName.trim()) else it }
    }

    override suspend fun delete(id: Long) {
        state.value = state.value.filterNot { it.id == id }
    }

    override suspend fun seedDefaultsIfEmpty() {
        // No-op for tests; set categories explicitly via setCategories.
    }

    override suspend fun import(categories: List<Category>) {
        val existing = state.value
        val toAdd =
            categories
                .filterNot { c -> existing.any { it.name == c.name && it.type == c.type } }
                .map { it.copy(id = nextId++) }
        state.value = existing + toAdd
    }
}
