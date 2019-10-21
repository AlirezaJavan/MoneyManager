package com.javanapps.moneymanager.feature.settings.impl

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.javanapps.moneymanager.core.model.DarkThemeConfig

@Composable
fun SettingsRoute(
    onOpenSmsSettings: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreen(
        uiState = uiState,
        onBiometricChange = viewModel::setBiometricEnabled,
        onSmsServiceChange = viewModel::setSmsServiceEnabled,
        onThemeChange = viewModel::setDarkThemeConfig,
        onChangePassword = viewModel::changePassword,
        onMessageShown = viewModel::consumeMessage,
        onOpenSmsSettings = onOpenSmsSettings,
    )
}

@Composable
internal fun SettingsScreen(
    uiState: SettingsUiState,
    onBiometricChange: (Boolean) -> Unit,
    onSmsServiceChange: (Boolean) -> Unit,
    onThemeChange: (DarkThemeConfig) -> Unit,
    onChangePassword: (String, String) -> Unit,
    onMessageShown: () -> Unit,
    onOpenSmsSettings: () -> Unit,
) {
    var showPasswordDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.message) {
        if (uiState.message != null) onMessageShown()
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(stringResource(R.string.feature_settings_impl_settings_title), style = MaterialTheme.typography.headlineSmall)

        SwitchRow(
            title = stringResource(R.string.feature_settings_impl_settings_biometric),
            checked = uiState.biometricEnabled,
            onChange = onBiometricChange,
        )
        SwitchRow(
            title = stringResource(R.string.feature_settings_impl_settings_sms_service),
            checked = uiState.smsServiceEnabled,
            onChange = onSmsServiceChange,
        )

        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
        Text(stringResource(R.string.feature_settings_impl_settings_theme_title), style = MaterialTheme.typography.titleMedium)
        ThemeOption(
            stringResource(R.string.feature_settings_impl_settings_theme_system),
            DarkThemeConfig.FOLLOW_SYSTEM,
            uiState.darkThemeConfig,
            onThemeChange,
        )
        ThemeOption(
            stringResource(R.string.feature_settings_impl_settings_theme_light),
            DarkThemeConfig.LIGHT,
            uiState.darkThemeConfig,
            onThemeChange,
        )
        ThemeOption(
            stringResource(R.string.feature_settings_impl_settings_theme_dark),
            DarkThemeConfig.DARK,
            uiState.darkThemeConfig,
            onThemeChange,
        )

        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
        TextButton(
            onClick = { showPasswordDialog = true },
        ) { Text(stringResource(R.string.feature_settings_impl_settings_change_password)) }
        TextButton(onClick = onOpenSmsSettings) { Text(stringResource(R.string.feature_settings_impl_settings_sms_rules)) }

        uiState.message?.let { msg ->
            val text =
                when (msg) {
                    SettingsMessage.PasswordChanged -> stringResource(R.string.feature_settings_impl_settings_message_password_changed)
                    SettingsMessage.PasswordWrong -> stringResource(R.string.feature_settings_impl_settings_message_password_wrong)
                }
            Text(text, color = MaterialTheme.colorScheme.primary)
        }
    }

    if (showPasswordDialog) {
        ChangePasswordDialog(
            onConfirm = { old, new ->
                onChangePassword(old, new)
                showPasswordDialog = false
            },
            onDismiss = { showPasswordDialog = false },
        )
    }
}

@Composable
private fun SwitchRow(
    title: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun ThemeOption(
    label: String,
    config: DarkThemeConfig,
    selected: DarkThemeConfig,
    onSelect: (DarkThemeConfig) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onSelect(config) }.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected == config, onClick = { onSelect(config) })
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ChangePasswordDialog(
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var old by remember { mutableStateOf("") }
    var new by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.feature_settings_impl_settings_dialog_change_password_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = old,
                    onValueChange = { old = it },
                    label = { Text(stringResource(R.string.feature_settings_impl_settings_old_password)) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = new,
                    onValueChange = { new = it },
                    label = { Text(stringResource(R.string.feature_settings_impl_settings_new_password)) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { if (old.isNotBlank() && new.length >= 4) onConfirm(old, new) }) {
                Text(stringResource(R.string.feature_settings_impl_settings_action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.feature_settings_impl_settings_action_cancel)) }
        },
    )
}
