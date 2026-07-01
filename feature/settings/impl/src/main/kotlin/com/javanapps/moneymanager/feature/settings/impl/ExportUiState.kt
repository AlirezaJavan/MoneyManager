package com.javanapps.moneymanager.feature.settings.impl

import com.javanapps.moneymanager.core.model.TransactionType
import io.github.alirezajavan.shamsipicker.model.MonthKey

data class ExportUiState(
    val filterFromMonth: MonthKey? = null,
    val filterToMonth: MonthKey? = null,
    val filterType: TransactionType? = null,
    val titleQuery: String = "",
    val categoryQuery: String = "",
    val isExporting: Boolean = false,
    val exportMessage: ExportMessage? = null,
    val launchDbPicker: Boolean = false,
    val launchCsvPicker: Boolean = false,
) {
    val hasActiveFilters: Boolean
        get() =
            filterFromMonth != null ||
                filterToMonth != null ||
                filterType != null ||
                titleQuery.isNotBlank() ||
                categoryQuery.isNotBlank()
}

enum class ExportMessage { DatabaseExported, CsvExported, ExportError }
