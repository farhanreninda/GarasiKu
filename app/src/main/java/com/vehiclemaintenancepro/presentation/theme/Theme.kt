package com.vehiclemaintenancepro.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00288E),
    onPrimary = WorkshopSurface,
    secondary = Color(0xFF0051D5),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDE1FF),
    onSecondaryContainer = Color(0xFF001453),
    primaryContainer = Color(0xFF1E40AF),
    onPrimaryContainer = Color.White,
    tertiary = Color(0xFF00554E),
    tertiaryContainer = Color(0xFF89F5E7),
    onTertiaryContainer = Color(0xFF00201D),
    background = Color(0xFFFAF8FF),
    onBackground = Color(0xFF131B2E),
    surface = Color.White,
    onSurface = Color(0xFF131B2E),
    surfaceVariant = Color(0xFFF2F3FF),
    onSurfaceVariant = Color(0xFF5C6072),
    surfaceContainer = Color.White,
    surfaceContainerLow = Color(0xFFF2F3FF),
    surfaceContainerHigh = Color(0xFFE2E7FF),
    surfaceContainerHighest = Color(0xFFDAE2FD),
    outline = Color(0xFF757684),
    outlineVariant = Color(0xFFE4E7F2),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
)

private val DarkColorScheme = darkColorScheme(
    primary = ModernBlueDark,
    onPrimary = Graphite,
    secondary = SoftBlueDark,
    onSecondary = Graphite,
    primaryContainer = Color(0xFF243E65),
    onPrimaryContainer = Color.White,
    secondaryContainer = Color(0xFF243E65),
    onSecondaryContainer = Color(0xFFDDE8FF),
    tertiary = Color(0xFF89F5E7),
    onTertiary = Color(0xFF003731),
    tertiaryContainer = Color(0xFF005048),
    onTertiaryContainer = Color(0xFFA6F2E7),
    background = Color(0xFF10121C),
    onBackground = WorkshopSurface,
    surface = Color(0xFF191D2B),
    onSurface = WorkshopSurface,
    surfaceVariant = Color(0xFF252B3E),
    onSurfaceVariant = Color(0xFFC6CFDA),
    outline = Color(0xFF8D96A9),
    outlineVariant = Color(0xFF3B4757),
    surfaceContainer = Color(0xFF191D2B),
    surfaceContainerLow = Color(0xFF191D2B),
    surfaceContainerHigh = Color(0xFF252B3E),
    surfaceContainerHighest = Color(0xFF252B3E),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF5F211D),
    onErrorContainer = Color(0xFFFFDAD6),
)

@Composable
fun VehicleMaintenanceProTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme -> dynamicDarkColorScheme(context)
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicLightColorScheme(context)
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = Shapes(
            extraSmall = RoundedCornerShape(8.dp),
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(16.dp),
            large = RoundedCornerShape(20.dp),
            extraLarge = RoundedCornerShape(24.dp),
        ),
        content = content,
    )
}
