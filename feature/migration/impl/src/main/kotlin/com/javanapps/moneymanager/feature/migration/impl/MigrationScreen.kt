package com.javanapps.moneymanager.feature.migration.impl

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun MigrationRoute(viewModel: MigrationViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MigrationScreen(
        uiState = uiState,
        onAutoDetect = viewModel::migrateAutoDetected,
        onPickFile = viewModel::migrateFromUri,
        onSkip = viewModel::skip,
    )
}

@Composable
internal fun MigrationScreen(
    uiState: MigrationUiState,
    onAutoDetect: () -> Unit,
    onPickFile: (android.net.Uri) -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val filePicker =
        rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument(),
        ) { uri -> uri?.let(onPickFile) }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.feature_migration_impl_migration_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.feature_migration_impl_migration_description),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )

        when (uiState) {
            is MigrationUiState.Running -> CircularProgressIndicator()

            is MigrationUiState.Error ->
                Text(
                    text = uiState.message,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )

            is MigrationUiState.Success ->
                Text(
                    text = stringResource(R.string.feature_migration_impl_migration_success),
                    textAlign = TextAlign.Center,
                )

            is MigrationUiState.Idle -> Unit
        }

        if (uiState !is MigrationUiState.Running) {
            val autoAvailable = (uiState as? MigrationUiState.Idle)?.autoDetectAvailable == true
            if (autoAvailable) {
                Button(onClick = onAutoDetect, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.feature_migration_impl_migration_auto_detect))
                }
            }
            OutlinedButton(
                onClick = { filePicker.launch(arrayOf("*/*")) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.feature_migration_impl_migration_pick_file))
            }
            TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.feature_migration_impl_migration_skip))
            }
        }
    }
}
