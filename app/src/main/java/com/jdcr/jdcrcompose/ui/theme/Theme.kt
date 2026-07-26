package com.jdcr.jdcrcompose.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PineLight,
    onPrimary = Night,
    secondary = CoralLight,
    background = Night,
    surface = NightSurface,
    onBackground = Paper,
    onSurface = Paper,
    outline = NightOutline,
)

private val LightColorScheme = lightColorScheme(
    primary = Pine,
    onPrimary = Paper,
    secondary = Coral,
    background = Paper,
    surface = Color.White,
    onBackground = Ink,
    onSurface = Ink,
    surfaceVariant = Mist,
)

@Composable
fun JdcrComposeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
