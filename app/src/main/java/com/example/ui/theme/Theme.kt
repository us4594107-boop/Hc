package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = AlfaCrimson,
    onPrimary = Color.White,
    primaryContainer = AlfaCrimsonContainer,
    onPrimaryContainer = AlfaOnCrimsonContainer,
    secondary = AlfaNavy,
    onSecondary = Color.White,
    secondaryContainer = AlfaNavyContainer,
    onSecondaryContainer = AlfaOnNavyContainer,
    tertiary = AlfaGold,
    onTertiary = Color.White,
    tertiaryContainer = AlfaGoldContainer,
    onTertiaryContainer = AlfaOnGoldContainer,
    background = WarmBackground,
    onBackground = WarmTextPrimary,
    surface = WarmSurface,
    onSurface = WarmTextPrimary,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = WarmTextSecondary,
    outline = WarmOutline,
    outlineVariant = WarmOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkCrimsonPrimary,
    onPrimary = Color(0xFF3E0003),
    primaryContainer = DarkCrimsonContainer,
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = DarkNavySecondary,
    onSecondary = Color(0xFF002D63),
    secondaryContainer = DarkNavyContainer,
    onSecondaryContainer = Color(0xFFD6E3FF),
    tertiary = AlfaGold,
    onTertiary = Color(0xFF3D2400),
    tertiaryContainer = Color(0xFF583800),
    onTertiaryContainer = Color(0xFFFEF3C7),
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkOutline,
    outlineVariant = DarkOutline
)

val AlfaoneShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun AlfaoneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AlfaoneTypography,
        shapes = AlfaoneShapes,
        content = content
    )
}
