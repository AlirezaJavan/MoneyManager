package com.javanapps.moneymanager.feature.migration.impl

sealed interface MigrationUiState {
    data class Idle(
        val autoDetectAvailable: Boolean,
    ) : MigrationUiState

    data object Running : MigrationUiState

    data class Success(
        val transactions: Int,
        val categories: Int,
    ) : MigrationUiState

    data class Error(
        val message: String,
    ) : MigrationUiState
}
