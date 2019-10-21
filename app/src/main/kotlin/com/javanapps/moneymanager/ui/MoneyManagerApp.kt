package com.javanapps.moneymanager.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.javanapps.moneymanager.R
import com.javanapps.moneymanager.feature.auth.impl.AuthRoute
import com.javanapps.moneymanager.feature.migration.impl.MigrationRoute
import com.javanapps.moneymanager.navigation.MainScaffold

/**
 * Root of the app. Gates: remote kill-switch → authentication → migration → main.
 * Permission state is threaded from MainActivity into the main scaffold via callbacks.
 */
@Composable
fun MoneyManagerApp(
    onShowBiometric: (onSuccess: () -> Unit) -> Unit,
    hasSmsPermission: Boolean = true,
    hasNotificationPermission: Boolean = true,
    hasOverlayPermission: Boolean = true,
    isBatteryOptimized: Boolean = false,
    onRequestSmsPermission: () -> Unit = {},
    onRequestNotificationPermission: () -> Unit = {},
    onRequestOverlayPermission: () -> Unit = {},
    onRequestBatteryOptimization: () -> Unit = {},
    appViewModel: AppViewModel = hiltViewModel(),
) {
    val uiState by appViewModel.uiState.collectAsStateWithLifecycle()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when (val state = uiState) {
            AppUiState.Loading -> Unit
            is AppUiState.Ready ->
                when {
                    !state.isAppActive -> DisabledScreen()
                    !state.loggedIn ->
                        AuthRoute(
                            onAuthenticated = appViewModel::onLoggedIn,
                            onBiometricRequested = {
                                onShowBiometric {
                                    appViewModel.onLoggedIn()
                                }
                            },
                        )
                    !state.migrationDone -> MigrationRoute()
                    else ->
                        MainScaffold(
                            hasSmsPermission = hasSmsPermission,
                            hasNotificationPermission = hasNotificationPermission,
                            hasOverlayPermission = hasOverlayPermission,
                            isBatteryOptimized = isBatteryOptimized,
                            onRequestSmsPermission = onRequestSmsPermission,
                            onRequestNotificationPermission = onRequestNotificationPermission,
                            onRequestOverlayPermission = onRequestOverlayPermission,
                            onRequestBatteryOptimization = onRequestBatteryOptimization,
                        )
                }
        }
    }
}

@Composable
private fun DisabledScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.app_disabled),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
    }
}
