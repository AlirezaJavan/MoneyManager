package com.javanapps.moneymanager.core.domain.migration

import com.javanapps.moneymanager.core.data.DefaultCategories
import com.javanapps.moneymanager.core.data.legacy.LegacyDataReader
import com.javanapps.moneymanager.core.data.legacy.LegacyDatabaseLocator
import com.javanapps.moneymanager.core.data.repository.CategoryRepository
import com.javanapps.moneymanager.core.data.repository.PreferencesRepository
import com.javanapps.moneymanager.core.data.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/** Outcome of a migration run. */
data class MigrationResult(
    val importedTransactions: Int,
    val importedCategories: Int,
)

/** Auto-detects a readable legacy database at its known path, if any. */
class FindLegacyDatabaseUseCase
    @Inject
    constructor(
        private val locator: LegacyDatabaseLocator,
    ) {
        operator fun invoke(): File? = locator.autoDetect()
    }

/**
 * Converts a legacy `Contacts_DB` into the new Room database: imports categories (plus defaults) and
 * transactions, deletes the old database file, and marks migration done. Idempotent at the app level
 * via the `migrationDone` flag. CQS: command.
 */
class RunMigrationUseCase
    @Inject
    constructor(
        private val reader: LegacyDataReader,
        private val categoryRepository: CategoryRepository,
        private val bankSmsRuleRepository: com.javanapps.moneymanager.core.data.repository.BankSmsRuleRepository,
        private val transactionRepository: TransactionRepository,
        private val preferencesRepository: PreferencesRepository,
    ) {
        suspend operator fun invoke(databaseFile: File): MigrationResult =
            withContext(Dispatchers.IO) {
                val data = reader.read(databaseFile)
                categoryRepository.import(data.categories)
                categoryRepository.import(DefaultCategories.all())
                bankSmsRuleRepository.seedDefaultsIfEmpty()
                transactionRepository.addAll(data.transactions)
                runCatching { databaseFile.delete() }
                preferencesRepository.setMigrationDone(true)
                MigrationResult(
                    importedTransactions = data.transactions.size,
                    importedCategories = data.categories.size,
                )
            }
    }

/** Skips migration (no legacy data / user declined): seeds defaults and marks migration done. */
class CompleteWithoutMigrationUseCase
    @Inject
    constructor(
        private val categoryRepository: CategoryRepository,
        private val bankSmsRuleRepository: com.javanapps.moneymanager.core.data.repository.BankSmsRuleRepository,
        private val preferencesRepository: PreferencesRepository,
    ) {
        suspend operator fun invoke() {
            categoryRepository.seedDefaultsIfEmpty()
            bankSmsRuleRepository.seedDefaultsIfEmpty()
            preferencesRepository.setMigrationDone(true)
        }
    }
