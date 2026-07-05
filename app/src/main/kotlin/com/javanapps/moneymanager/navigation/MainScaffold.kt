package com.javanapps.moneymanager.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.javanapps.moneymanager.R
import com.javanapps.moneymanager.core.ui.component.SmsPermissionRequestScreen
import com.javanapps.moneymanager.feature.categories.impl.CategoriesRoute
import com.javanapps.moneymanager.feature.home.impl.HomeRoute
import com.javanapps.moneymanager.feature.reports.impl.ReportsRoute
import com.javanapps.moneymanager.feature.settings.impl.ExportRoute
import com.javanapps.moneymanager.feature.settings.impl.SettingsRoute
import com.javanapps.moneymanager.feature.sms.impl.SmsRoute
import com.javanapps.moneymanager.feature.transaction.impl.AddEditTransactionRoute
import com.javanapps.moneymanager.sms.PendingTransactionsScreen

private data class TopLevelDestination(
    val key: NavKey,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
)

private val topLevelDestinations =
    listOf(
        TopLevelDestination(HomeNavKey, R.string.nav_home, Icons.Filled.Home),
        TopLevelDestination(ReportsNavKey, R.string.nav_reports, Icons.Filled.BarChart),
        TopLevelDestination(CategoriesNavKey, R.string.nav_categories, Icons.Filled.Category),
        TopLevelDestination(SettingsNavKey, R.string.nav_settings, Icons.Filled.Settings),
    )

@Composable
fun MainScaffold(
    hasSmsPermission: Boolean = true,
    hasNotificationPermission: Boolean = true,
    hasOverlayPermission: Boolean = true,
    isBatteryOptimized: Boolean = false,
    onRequestSmsPermission: () -> Unit = {},
    onRequestNotificationPermission: () -> Unit = {},
    onRequestOverlayPermission: () -> Unit = {},
    onRequestBatteryOptimization: () -> Unit = {},
    viewModel: MainScaffoldViewModel = hiltViewModel(),
) {
    val userData by viewModel.userData.collectAsStateWithLifecycle()
    val pendingTransactions by viewModel.pendingTransactions.collectAsStateWithLifecycle()

    val backStack = rememberNavBackStack(HomeNavKey)
    val current = backStack.lastOrNull()

    if (pendingTransactions.isNotEmpty()) {
        PendingTransactionsScreen(
            transactions = pendingTransactions,
            categoryRepository = viewModel.categoryRepository,
            addCategoryUseCase = viewModel.addCategoryUseCase,
            renameCategoryUseCase = viewModel.renameCategoryUseCase,
            onConfirm = viewModel::confirmTransaction,
            onRemove = viewModel::removeTransaction,
        )
    } else {
        val smsEnabled = userData?.smsServiceEnabled ?: false
        val showPermissionScreen = smsEnabled && (!hasSmsPermission || !hasNotificationPermission)

        Scaffold(
            bottomBar = {
                if (current in topLevelDestinations.map { it.key }) {
                    NavigationBar {
                        topLevelDestinations.forEach { destination ->
                            NavigationBarItem(
                                selected = current == destination.key,
                                onClick = {
                                    if (current != destination.key) {
                                        backStack.clear()
                                        backStack.add(destination.key)
                                    }
                                },
                                icon = { Icon(destination.icon, contentDescription = stringResource(destination.labelRes)) },
                                label = { Text(stringResource(destination.labelRes)) },
                            )
                        }
                    }
                }
            },
        ) { padding ->
            if (showPermissionScreen && current == HomeNavKey) {
                SmsPermissionRequestScreen(
                    hasSmsPermission = hasSmsPermission,
                    hasNotificationPermission = hasNotificationPermission,
                    hasOverlayPermission = hasOverlayPermission,
                    isBatteryOptimized = isBatteryOptimized,
                    onRequestSms = onRequestSmsPermission,
                    onRequestNotification = onRequestNotificationPermission,
                    onRequestOverlay = onRequestOverlayPermission,
                    onRequestBatteryOptimization = onRequestBatteryOptimization,
                    onSkip = { backStack.add(SettingsNavKey) },
                    modifier = Modifier.padding(padding),
                )
            } else {
                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    modifier = Modifier.padding(padding),
                    entryProvider =
                        entryProvider {
                            entry<HomeNavKey> {
                                HomeRoute(
                                    onAddTransaction = { backStack.add(AddEditTransactionNavKey()) },
                                    onEditTransaction = { id -> backStack.add(AddEditTransactionNavKey(id)) },
                                )
                            }
                            entry<ReportsNavKey> {
                                ReportsRoute(
                                    onEditTransaction = { id -> backStack.add(AddEditTransactionNavKey(id)) },
                                )
                            }
                            entry<CategoriesNavKey> { CategoriesRoute() }
                            entry<SettingsNavKey> {
                                SettingsRoute(
                                    onOpenSmsSettings = { backStack.add(SmsNavKey) },
                                    onOpenExport = { backStack.add(ExportNavKey) },
                                )
                            }
                            entry<ExportNavKey> {
                                ExportRoute(onBack = { backStack.removeLastOrNull() })
                            }
                            entry<SmsNavKey> { SmsRoute() }
                            entry<AddEditTransactionNavKey> { key ->
                                AddEditTransactionRoute(
                                    transactionId = key.transactionId,
                                    onDone = { backStack.removeLastOrNull() },
                                )
                            }
                        },
                )
            }
        }
    }
}
