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
    primary = Color(0xFF2357C6),
    onPrimary = WorkshopSurface,
    secondary = Color(0xFFDEE9FF),
    onSecondary = Color(0xFF17346A),
    secondaryContainer = Color(0xFFDEE9FF),
    onSecondaryContainer = Color(0xFF17346A),
    primaryContainer = Color(0xFF162C50),
    onPrimaryContainer = Color.White,
    tertiary = Color(0xFF2357C6),
    background = Color(0xFFF3F6FB),
    onBackground = Color(0xFF17243A),
    surface = Color.White,
    onSurface = Color(0xFF17243A),
    surfaceVariant = Color(0xFFEBF0F7),
    onSurfaceVariant = Color(0xFF53627A),
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color(0xFFEBF0F7),
    surfaceContainerHighest = Color(0xFFEBF0F7),
    outline = Color(0xFF75849A),
    outlineVariant = Color(0xFFDCE3EE),
    error = Color(0xFFB3261E),
    errorContainer = Color(0xFFFCE9E7),
    onErrorContainer = Color(0xFF8D211A),
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
    tertiary = Cobalt,
    background = DarkCanvas,
    onBackground = WorkshopSurface,
    surface = ColorTokens.DarkSurface,
    onSurface = WorkshopSurface,
    surfaceVariant = ColorTokens.DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFC6CFDA),
    outline = Color(0xFF303A45),
    outlineVariant = Color(0xFF3B4757),
    surfaceContainer = DarkPanel,
    surfaceContainerHigh = Color(0xFF232B34),
    surfaceContainerHighest = Color(0xFF232B34),
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
            small = RoundedCornerShape(10.dp),
            medium = RoundedCornerShape(16.dp),
            large = RoundedCornerShape(22.dp),
            extraLarge = RoundedCornerShape(28.dp),
        ),
        content = content,
    )
}
