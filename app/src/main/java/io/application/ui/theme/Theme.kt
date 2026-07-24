package io.application.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Amber,
    onPrimary = Ink,
    secondary = Mist,
    onSecondary = Ink,
    tertiary = Color(0xFFB88BD9),
    background = Ink,
    onBackground = Mist,
    surface = Ash,
    onSurface = Mist,
    surfaceVariant = Color(0xFF35303A),
    onSurfaceVariant = Mist.copy(alpha = .72f),
)

@Composable
fun ApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}