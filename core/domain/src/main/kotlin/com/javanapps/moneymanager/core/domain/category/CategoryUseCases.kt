package com.javanapps.moneymanager.core.domain.category

import com.javanapps.moneymanager.core.data.repository.CategoryRepository
import com.javanapps.moneymanager.core.data.repository.TransactionRepository
import com.javanapps.moneymanager.core.model.Category
import com.javanapps.moneymanager.core.model.TransactionType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Observes categories of a given type (expense or income). */
class GetCategoriesUseCase
    @Inject
    constructor(
        private val repository: CategoryRepository,
    ) {
        operator fun invoke(type: TransactionType): Flow<List<Category>> = repository.observeByType(type)
    }

/** Result of trying to add a category. */
sealed interface AddCategoryResult {
    data class Added(
        val id: Long,
    ) : AddCategoryResult

    data object Blank : AddCategoryResult

    data object Duplicate : AddCategoryResult
}

/** Adds a category, rejecting blank or duplicate names within the same type. */
class AddCategoryUseCase
    @Inject
    constructor(
        private val repository: CategoryRepository,
    ) {
        suspend operator fun invoke(
            name: String,
            type: TransactionType,
        ): AddCategoryResult {
            val trimmed = name.trim()
            if (trimmed.isEmpty()) return AddCategoryResult.Blank
            if (repository.exists(trimmed, type)) return AddCategoryResult.Duplicate
            return AddCategoryResult.Added(repository.add(trimmed, type))
        }
    }

/** Renames a category. */
class RenameCategoryUseCase
    @Inject
    constructor(
        private val repository: CategoryRepository,
    ) {
        suspend operator fun invoke(
            id: Long,
            newName: String,
        ) = repository.rename(id, newName)
    }

/**
 * Deletes a category. If it still has linked transactions the caller must pass `reassignTo` (move
 * them to another category) or set `deleteTransactions` = true; otherwise the deletion is rejected so
 * the UI can force the user to choose. CQS: this is a command.
 */
class DeleteCategoryUseCase
    @Inject
    constructor(
        private val categoryRepository: CategoryRepository,
        private val transactionRepository: TransactionRepository,
    ) {
        sealed interface Result {
            data object Deleted : Result

            data class HasTransactions(
                val count: Int,
            ) : Result
        }

        suspend operator fun invoke(
            category: Category,
            reassignTo: String? = null,
            deleteTransactions: Boolean = false,
        ): Result {
            val linked = transactionRepository.countInCategory(category.name, category.type)
            if (linked > 0) {
                when {
                    reassignTo != null ->
                        transactionRepository.reassignCategory(category.name, reassignTo, category.type)

                    deleteTransactions ->
                        transactionRepository.deleteByCategory(category.name, category.type)

                    else -> return Result.HasTransactions(linked)
                }
            }
            categoryRepository.delete(category.id)
            return Result.Deleted
        }
    }
