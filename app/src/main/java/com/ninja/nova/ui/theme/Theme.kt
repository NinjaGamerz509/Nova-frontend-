package com.ninja.nova.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NovaColorScheme = darkColorScheme(
    primary = NovaAqua,
    secondary = NovaBlue,
    background = NovaDark,
    surface = NovaSurface,
    onPrimary = NovaDark,
    onSecondary = NovaText,
    onBackground = NovaText,
    onSurface = NovaText,
    tertiary = NovaGlow
)

@Composable
fun NovaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NovaColorScheme,
        content = content
    )
}
