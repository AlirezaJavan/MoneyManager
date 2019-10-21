package com.javanapps.moneymanager.feature.migration.impl

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.javanapps.moneymanager.core.domain.migration.CompleteWithoutMigrationUseCase
import com.javanapps.moneymanager.core.domain.migration.FindLegacyDatabaseUseCase
import com.javanapps.moneymanager.core.domain.migration.RunMigrationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MigrationViewModel
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val findLegacyDatabase: FindLegacyDatabaseUseCase,
        private val runMigration: RunMigrationUseCase,
        private val completeWithoutMigration: CompleteWithoutMigrationUseCase,
    ) : ViewModel() {
        private val _uiState =
            MutableStateFlow<MigrationUiState>(
                MigrationUiState.Idle(autoDetectAvailable = findLegacyDatabase() != null),
            )
        val uiState = _uiState.asStateFlow()

        fun migrateAutoDetected() {
            val file = findLegacyDatabase()
            if (file == null) {
                _uiState.value = MigrationUiState.Error(context.getString(R.string.feature_migration_impl_migration_error_not_found))
                return
            }
            migrate(file)
        }

        fun migrateFromUri(uri: Uri) {
            viewModelScope.launch {
                _uiState.value = MigrationUiState.Running
                val file = runCatching { copyToCache(uri) }.getOrNull()
                if (file == null) {
                    _uiState.value = MigrationUiState.Error(context.getString(R.string.feature_migration_impl_migration_error_read))
                    return@launch
                }
                runMigrationOn(file)
            }
        }

        fun skip() {
            viewModelScope.launch { completeWithoutMigration() }
        }

        private fun migrate(file: File) {
            viewModelScope.launch {
                _uiState.value = MigrationUiState.Running
                runMigrationOn(file)
            }
        }

        private suspend fun runMigrationOn(file: File) {
            runCatching { runMigration(file) }
                .onSuccess { _uiState.value = MigrationUiState.Success(it.importedTransactions, it.importedCategories) }
                .onFailure {
                    _uiState.value =
                        MigrationUiState.Error(it.message ?: context.getString(R.string.feature_migration_impl_migration_error_generic))
                }
        }

        private suspend fun copyToCache(uri: Uri): File =
            withContext(Dispatchers.IO) {
                val target = File(context.cacheDir, "legacy_import.db")
                context.contentResolver.openInputStream(uri)!!.use { input ->
                    target.outputStream().use { output -> input.copyTo(output) }
                }
                target
            }
    }
