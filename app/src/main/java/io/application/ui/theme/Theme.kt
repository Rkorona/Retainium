package io.application.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 奇幻暗色主题 — 游戏始终在黑暗中展开
private val FantasyColorScheme = darkColorScheme(
    primary              = GoldAncient,
    onPrimary            = InkVoid,
    primaryContainer     = Color(0xFF3A2A08),
    onPrimaryContainer   = GoldBright,

    secondary            = RuneBlue,
    onSecondary          = InkVoid,
    secondaryContainer   = Color(0xFF0C2535),
    onSecondaryContainer = RuneBlue,

    tertiary             = RuneViolet,
    onTertiary           = InkVoid,
    tertiaryContainer    = Color(0xFF241540),
    onTertiaryContainer  = RuneViolet,

    background           = InkDeep,
    onBackground         = ScrollText,

    surface              = ParchmentNight,
    onSurface            = ScrollText,
    surfaceVariant       = Color(0xFF1E1A2C),
    onSurfaceVariant     = ScrollTextDim,

    outline              = GoldDim,
    outlineVariant       = Color(0xFF2E2618),

    error                = BloodHP,
    onError              = ScrollText,
    errorContainer       = Color(0xFF4A0E0E),
    onErrorContainer     = Color(0xFFFFB4AB),

    inverseSurface       = ScrollText,
    inverseOnSurface     = InkDeep,
    inversePrimary       = GoldDim,

    scrim                = Color(0xCC000000),
    surfaceTint          = GoldAncient,
)

@Composable
fun ApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FantasyColorScheme,
        typography  = FantasyTypography,
        content     = content
    )
}
