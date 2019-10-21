package com.javanapps.moneymanager.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.javanapps.moneymanager.core.model.DarkThemeConfig

/**
 * App theme. RTL is handled at the app root; this provides Material 3 colors + [Vazir]
 * typography. Dynamic color is opt-in (off by default to keep the brand teal identity).
 */
@Composable
fun MoneyManagerTheme(
    darkThemeConfig: DarkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val darkTheme =
        when (darkThemeConfig) {
            DarkThemeConfig.DARK -> true
            DarkThemeConfig.LIGHT -> false
            DarkThemeConfig.FOLLOW_SYSTEM -> isSystemInDarkTheme()
        }
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            darkTheme -> DarkColors
            else -> LightColors
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MoneyManagerTypography,
        content = content,
    )
}
