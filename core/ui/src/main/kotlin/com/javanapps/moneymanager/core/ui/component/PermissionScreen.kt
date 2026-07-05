package com.javanapps.moneymanager.core.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.javanapps.moneymanager.core.ui.R

/**
 * A premium-looking permission request screen shown when the app needs critical permissions.
 * Presented on the Home screen when SMS permissions are enabled but not granted.
 */
@Composable
fun SmsPermissionRequestScreen(
    hasSmsPermission: Boolean,
    hasNotificationPermission: Boolean,
    hasOverlayPermission: Boolean,
    isBatteryOptimized: Boolean,
    onRequestSms: () -> Unit,
    onRequestNotification: () -> Unit,
    onRequestOverlay: () -> Unit,
    onRequestBatteryOptimization: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(32.dp))

            // Header icon
            Box(
                modifier =
                    Modifier
                        .size(80.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Message,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            Text(
                stringResource(R.string.core_ui_permission_screen_headline),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            Text(
                stringResource(R.string.core_ui_permission_screen_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(8.dp))

            PermissionItem(
                icon = Icons.AutoMirrored.Filled.Message,
                title = stringResource(R.string.core_ui_permission_sms_title),
                description = stringResource(R.string.core_ui_permission_sms_description),
                granted = hasSmsPermission,
                onRequest = onRequestSms,
            )

            PermissionItem(
                icon = Icons.Default.NotificationsActive,
                title = stringResource(R.string.core_ui_permission_notification_title),
                description = stringResource(R.string.core_ui_permission_notification_description),
                granted = hasNotificationPermission,
                onRequest = onRequestNotification,
            )

            PermissionItem(
                icon = Icons.Default.Layers,
                title = stringResource(R.string.core_ui_permission_overlay_title),
                description = stringResource(R.string.core_ui_permission_overlay_description),
                granted = hasOverlayPermission,
                onRequest = onRequestOverlay,
                optional = true,
            )

            PermissionItem(
                icon = Icons.Default.BatteryChargingFull,
                title = stringResource(R.string.core_ui_permission_battery_title),
                description = stringResource(R.string.core_ui_permission_battery_description),
                granted = !isBatteryOptimized,
                onRequest = onRequestBatteryOptimization,
                optional = true,
            )
        }

        Spacer(Modifier.height(16.dp))

        AnimatedVisibility(hasSmsPermission) {
            Button(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.core_ui_permission_back_home))
            }
        }

        if (!hasSmsPermission) {
            OutlinedButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.core_ui_permission_skip))
            }
        }
    }
}

@Composable
private fun PermissionItem(
    icon: ImageVector,
    title: String,
    description: String,
    granted: Boolean,
    onRequest: () -> Unit,
    optional: Boolean = false,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (granted) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .background(
                            if (granted) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            } else {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                            },
                            CircleShape,
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint =
                        if (granted) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (optional) {
                    Text(
                        stringResource(R.string.core_ui_permission_optional_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            if (!granted) {
                OutlinedButton(onClick = onRequest) { Text(stringResource(R.string.core_ui_permission_grant_action)) }
            } else {
                Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
