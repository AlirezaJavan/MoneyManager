package com.javanapps.moneymanager.core.designsystem.theme

import androidx.compose.ui.graphics.Color

// Brand: the legacy launcher teal (#0B837F) anchors the palette.
internal val Teal = Color(0xFF0B837F)
internal val TealDark = Color(0xFF005C58)
internal val TealLight = Color(0xFF4FB3AE)

// Semantic money colors (used by amounts, charts, summaries).
val IncomeGreen = Color(0xFF2E7D32)
val ExpenseRed = Color(0xFFC62828)

internal val LightColors =
    androidx.compose.material3.lightColorScheme(
        primary = Teal,
        onPrimary = Color.White,
        primaryContainer = TealLight,
        onPrimaryContainer = Color(0xFF00201E),
        secondary = TealDark,
        onSecondary = Color.White,
    )

internal val DarkColors =
    androidx.compose.material3.darkColorScheme(
        primary = TealLight,
        onPrimary = Color(0xFF00201E),
        primaryContainer = TealDark,
        onPrimaryContainer = Color(0xFFB6F2EE),
        secondary = TealLight,
        onSecondary = Color(0xFF00201E),
    )
