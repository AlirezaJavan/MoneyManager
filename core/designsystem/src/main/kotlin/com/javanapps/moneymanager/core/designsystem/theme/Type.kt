package com.javanapps.moneymanager.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.javanapps.moneymanager.core.designsystem.R

/** Vazir — the Persian font carried over from the legacy app, used across the whole UI. */
val Vazir: FontFamily =
    FontFamily(
        Font(R.font.vazir, FontWeight.Normal),
        Font(R.font.vazir_fd, FontWeight.Medium),
        Font(R.font.vazir_fd, FontWeight.Bold),
    )

/** Material 3 typography with every style re-based on the [Vazir] family for correct Persian shaping. */
val MoneyManagerTypography: Typography =
    Typography().run {
        fun TextStyle.withVazir() = copy(fontFamily = Vazir)
        copy(
            displayLarge = displayLarge.withVazir(),
            displayMedium = displayMedium.withVazir(),
            displaySmall = displaySmall.withVazir(),
            headlineLarge = headlineLarge.withVazir(),
            headlineMedium = headlineMedium.withVazir(),
            headlineSmall = headlineSmall.withVazir(),
            titleLarge = titleLarge.withVazir(),
            titleMedium = titleMedium.withVazir(),
            titleSmall = titleSmall.withVazir(),
            bodyLarge = bodyLarge.withVazir(),
            bodyMedium = bodyMedium.withVazir(),
            bodySmall = bodySmall.withVazir(),
            labelLarge = labelLarge.withVazir(),
            labelMedium = labelMedium.withVazir(),
            labelSmall = labelSmall.withVazir(),
        )
    }
