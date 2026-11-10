package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AmberPrimary,
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF78350F),
    onPrimaryContainer = Color(0xFFFEF3C7),
    secondary = AmberSecondary,
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0xFF451A03),
    onSecondaryContainer = Color(0xFFFDE68A),
    tertiary = CyanCarbs,
    onTertiary = Color(0xFF0F172A),
    background = GymDarkBg,
    onBackground = TextPrimary,
    surface = GymSurface,
    onSurface = TextPrimary,
    surfaceVariant = GymSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = GymCardStroke,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun ApexGymTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    ApexGymTheme(content = content)
}
