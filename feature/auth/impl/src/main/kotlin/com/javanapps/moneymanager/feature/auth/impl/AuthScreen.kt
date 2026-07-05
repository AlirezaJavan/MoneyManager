package com.javanapps.moneymanager.feature.auth.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AuthRoute(
    onAuthenticated: () -> Unit,
    onBiometricRequested: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.authenticated) {
        if (uiState.authenticated) onAuthenticated()
    }

    var biometricAutoPromptShown by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.canUseBiometric) {
        if (uiState.canUseBiometric && !biometricAutoPromptShown) {
            biometricAutoPromptShown = true
            onBiometricRequested()
        }
    }

    AuthScreen(
        uiState = uiState,
        onUsernameChange = viewModel::onUsernameChange,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onSubmit = viewModel::submit,
        onBiometric = onBiometricRequested,
    )
}

@Composable
internal fun AuthScreen(
    uiState: AuthUiState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBiometric: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .imePadding()
                .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.feature_auth_impl_auth_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text =
                if (uiState.mode == AuthMode.SIGN_UP) {
                    stringResource(R.string.feature_auth_impl_auth_subtitle_signup)
                } else {
                    stringResource(R.string.feature_auth_impl_auth_subtitle_login)
                },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )

        if (uiState.mode == AuthMode.SIGN_UP) {
            OutlinedTextField(
                value = uiState.username,
                onValueChange = onUsernameChange,
                label = { Text(stringResource(R.string.feature_auth_impl_auth_label_username)) },
                singleLine = true,
                isError = uiState.usernameError != null,
                supportingText = uiState.usernameError?.let { id -> { Text(stringResource(id)) } },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        OutlinedTextField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            label = { Text(stringResource(R.string.feature_auth_impl_auth_label_password)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            isError = uiState.passwordError != null,
            supportingText = uiState.passwordError?.let { id -> { Text(stringResource(id)) } },
            modifier = Modifier.fillMaxWidth(),
        )

        if (uiState.mode == AuthMode.SIGN_UP) {
            OutlinedTextField(
                value = uiState.confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = { Text(stringResource(R.string.feature_auth_impl_auth_label_confirm_password)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                isError = uiState.confirmError != null,
                supportingText = uiState.confirmError?.let { id -> { Text(stringResource(id)) } },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Button(onClick = onSubmit, modifier = Modifier.fillMaxWidth()) {
            Text(
                if (uiState.mode == AuthMode.SIGN_UP) {
                    stringResource(R.string.feature_auth_impl_auth_action_register)
                } else {
                    stringResource(R.string.feature_auth_impl_auth_action_login)
                },
            )
        }

        if (uiState.canUseBiometric) {
            OutlinedButton(onClick = onBiometric, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Fingerprint, contentDescription = null)
                Text(
                    text = stringResource(R.string.feature_auth_impl_auth_biometric),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}
