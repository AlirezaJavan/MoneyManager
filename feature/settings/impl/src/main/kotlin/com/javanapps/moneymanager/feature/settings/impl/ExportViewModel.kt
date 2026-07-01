package com.javanapps.moneymanager.feature.settings.impl

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.javanapps.moneymanager.core.database.DatabaseExportHelper
import com.javanapps.moneymanager.core.domain.transaction.SearchTransactionsUseCase
import com.javanapps.moneymanager.core.model.Transaction
import com.javanapps.moneymanager.core.model.TransactionFilter
import com.javanapps.moneymanager.core.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.alirezajavan.shamsipicker.model.MonthKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ExportViewModel
    @Inject
    constructor(
        private val searchTransactions: SearchTransactionsUseCase,
        private val dbExportHelper: DatabaseExportHelper,
        @ApplicationContext private val context: Context,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ExportUiState())
        val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

        fun setFromMonth(month: MonthKey?) = _uiState.update { it.copy(filterFromMonth = month) }

        fun setToMonth(month: MonthKey?) = _uiState.update { it.copy(filterToMonth = month) }

        fun setFilterType(type: TransactionType?) = _uiState.update { it.copy(filterType = type) }

        fun setTitleQuery(query: String) = _uiState.update { it.copy(titleQuery = query) }

        fun setCategoryQuery(query: String) = _uiState.update { it.copy(categoryQuery = query) }

        fun clearFilters() =
            _uiState.update {
                it.copy(
                    filterFromMonth = null,
                    filterToMonth = null,
                    filterType = null,
                    titleQuery = "",
                    categoryQuery = "",
                )
            }

        fun requestDbExport() = _uiState.update { it.copy(launchDbPicker = true) }

        fun onDbPickerLaunched() = _uiState.update { it.copy(launchDbPicker = false) }

        fun requestCsvExport() = _uiState.update { it.copy(launchCsvPicker = true) }

        fun onCsvPickerLaunched() = _uiState.update { it.copy(launchCsvPicker = false) }

        fun consumeMessage() = _uiState.update { it.copy(exportMessage = null) }

        fun exportDatabase(uri: Uri) {
            viewModelScope.launch(Dispatchers.IO) {
                _uiState.update { it.copy(isExporting = true) }
                val result =
                    runCatching {
                        Log.d("ExportViewModel", "Starting DB export to URI: $uri")
                        dbExportHelper.checkpointWal()
                        val dbFile = dbExportHelper.databaseFile()

                        Log.d("ExportViewModel", "Source DB file: ${dbFile.absolutePath}, size: ${dbFile.length()} bytes")

                        // Log sidecar files
                        val walFile = File("${dbFile.absolutePath}-wal")
                        val shmFile = File("${dbFile.absolutePath}-shm")
                        Log.d("ExportViewModel", "WAL file exists: ${walFile.exists()}, size: ${walFile.length()} bytes")
                        Log.d("ExportViewModel", "SHM file exists: ${shmFile.exists()}, size: ${shmFile.length()} bytes")

                        if (!dbFile.exists()) throw Exception("Database file not found at ${dbFile.absolutePath}")
                        if (dbFile.length() == 0L) {
                            Log.e("ExportViewModel", "Database file is 0 bytes!")
                            throw Exception("Database file is empty")
                        }

                        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                            dbFile.inputStream().use { inputStream ->
                                val bytesCopied = inputStream.copyTo(outputStream)
                                outputStream.flush()
                                Log.d("ExportViewModel", "Bytes copied to output stream: $bytesCopied")
                                if (bytesCopied == 0L) throw Exception("No bytes were copied")
                            }
                        } ?: throw Exception("Failed to open output stream for URI: $uri")
                    }

                result.fold(
                    onSuccess = {
                        Log.d("ExportViewModel", "Export successful")
                        _uiState.update { it.copy(isExporting = false, exportMessage = ExportMessage.DatabaseExported) }
                    },
                    onFailure = { e ->
                        Log.e("ExportViewModel", "Export failed", e)
                        _uiState.update { it.copy(isExporting = false, exportMessage = ExportMessage.ExportError) }
                    },
                )
            }
        }

        fun exportCsv(uri: Uri) {
            viewModelScope.launch(Dispatchers.IO) {
                _uiState.update { it.copy(isExporting = true) }
                runCatching {
                    val state = _uiState.value
                    val filter =
                        TransactionFilter(
                            from = state.filterFromMonth,
                            to = state.filterToMonth,
                            type = state.filterType,
                            titleQuery = state.titleQuery.takeIf { it.isNotBlank() },
                            categoryQuery = state.categoryQuery.takeIf { it.isNotBlank() },
                        )
                    val transactions = searchTransactions(filter).first()
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        out.writer(Charsets.UTF_8).use { writer ->
                            writer.write("\uFEFF") // BOM for Excel compatibility
                            writer.write(buildCsv(transactions))
                        }
                    }
                }.fold(
                    onSuccess = { _uiState.update { it.copy(isExporting = false, exportMessage = ExportMessage.CsvExported) } },
                    onFailure = { _uiState.update { it.copy(isExporting = false, exportMessage = ExportMessage.ExportError) } },
                )
            }
        }

        private fun buildCsv(transactions: List<Transaction>): String =
            buildString {
                appendLine("ID,Date,Type,Category,Title,Note,Amount (Toman)")
                for (tx in transactions) {
                    val date =
                        "${tx.date.year}/" +
                            "${tx.date.month.toString().padStart(2, '0')}/" +
                            tx.date.day
                                .toString()
                                .padStart(2, '0')
                    val type =
                        when (tx.type) {
                            TransactionType.EXPENSE -> "Expense"
                            TransactionType.INCOME -> "Income"
                        }
                    appendLine(
                        "${tx.id},$date,$type," +
                            "\"${tx.categoryName.escapeCsv()}\"," +
                            "\"${tx.title.escapeCsv()}\"," +
                            "\"${tx.note.escapeCsv()}\"," +
                            "${tx.amountToman}",
                    )
                }
            }

        private fun String.escapeCsv() = replace("\"", "\"\"")
    }
