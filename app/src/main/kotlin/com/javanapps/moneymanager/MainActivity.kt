package com.javanapps.moneymanager

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.javanapps.moneymanager.core.designsystem.theme.MoneyManagerTheme
import com.javanapps.moneymanager.core.model.DarkThemeConfig
import com.javanapps.moneymanager.sms.BatteryOptimizationHelper
import com.javanapps.moneymanager.ui.AppUiState
import com.javanapps.moneymanager.ui.AppViewModel
import com.javanapps.moneymanager.ui.MoneyManagerApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    private val appViewModel by viewModels<AppViewModel>()

    // Permission state exposed to composition
    var hasSmsPermission by mutableStateOf(false)
        private set
    var hasNotificationPermission by mutableStateOf(false)
        private set
    var isBatteryOptimized by mutableStateOf(true)
        private set

    private val smsPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { granted -> hasSmsPermission = granted }

    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { granted -> hasNotificationPermission = granted }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        refreshPermissionState()

        setContent {
            val uiState by appViewModel.uiState.collectAsStateWithLifecycle()
            val darkThemeConfig =
                (uiState as? AppUiState.Ready)?.darkThemeConfig
                    ?: DarkThemeConfig.FOLLOW_SYSTEM
            MoneyManagerTheme(darkThemeConfig = darkThemeConfig) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    MoneyManagerApp(
                        onShowBiometric = ::showBiometricPrompt,
                        hasSmsPermission = hasSmsPermission,
                        hasNotificationPermission = hasNotificationPermission,
                        isBatteryOptimized = isBatteryOptimized,
                        onRequestSmsPermission = ::requestSmsPermission,
                        onRequestNotificationPermission = ::requestNotificationPermission,
                        onRequestBatteryOptimization = ::requestBatteryOptimization,
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshPermissionState()
    }

    private fun refreshPermissionState() {
        hasSmsPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECEIVE_SMS,
        ) == PackageManager.PERMISSION_GRANTED

        hasNotificationPermission =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }

        isBatteryOptimized = !BatteryOptimizationHelper.isIgnoringBatteryOptimizations(this)
    }

    private fun requestSmsPermission() {
        smsPermissionLauncher.launch(Manifest.permission.RECEIVE_SMS)
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            hasNotificationPermission = true
        }
    }

    private fun requestBatteryOptimization() {
        val openedManufacturer = BatteryOptimizationHelper.openManufacturerBatterySettings(this)
        if (!openedManufacturer) {
            BatteryOptimizationHelper.requestIgnoreBatteryOptimizations(this)
        }
    }

    private fun showBiometricPrompt(onSuccess: () -> Unit) {
        val authenticators =
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL

        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                val executor = ContextCompat.getMainExecutor(this)
                val prompt =
                    BiometricPrompt(
                        this,
                        executor,
                        object : BiometricPrompt.AuthenticationCallback() {
                            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                onSuccess()
                            }
                        },
                    )
                val info =
                    BiometricPrompt.PromptInfo
                        .Builder()
                        .setTitle(getString(R.string.biometric_title))
                        .setSubtitle(getString(R.string.biometric_subtitle))
                        .setAllowedAuthenticators(authenticators)
                        .build()
                prompt.authenticate(info)
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val enrollIntent =
                        Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                            putExtra(
                                android.provider.Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                authenticators,
                            )
                        }
                    startActivity(enrollIntent)
                }
            }

            else -> Unit
        }
    }
}
