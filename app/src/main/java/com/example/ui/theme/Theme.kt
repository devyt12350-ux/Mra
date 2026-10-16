package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = DarkNavyPrimary,
    onPrimary = OnDarkNavyPrimary,
    primaryContainer = DarkNavyContainer,
    onPrimaryContainer = OnDarkNavyContainer,
    secondary = DarkGoldSecondary,
    onSecondary = OnDarkGoldSecondary,
    secondaryContainer = DarkGoldContainer,
    onSecondaryContainer = OnDarkGoldContainer,
    tertiary = DarkEmeraldTertiary,
    onTertiary = OnDarkEmeraldTertiary,
    tertiaryContainer = DarkEmeraldContainer,
    onTertiaryContainer = OnDarkEmeraldContainer,
    background = DarkSlateBackground,
    onBackground = OnDarkSlateBackground,
    surface = DarkSlateSurface,
    onSurface = OnDarkSlateSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = OnDarkSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = RoyalNavyPrimary,
    onPrimary = OnRoyalNavyPrimary,
    primaryContainer = RoyalNavyContainer,
    onPrimaryContainer = OnRoyalNavyContainer,
    secondary = AcademicGoldSecondary,
    onSecondary = OnAcademicGoldSecondary,
    secondaryContainer = AcademicGoldContainer,
    onSecondaryContainer = OnAcademicGoldContainer,
    tertiary = SageEmeraldTertiary,
    onTertiary = OnSageEmeraldTertiary,
    tertiaryContainer = SageEmeraldContainer,
    onTertiaryContainer = OnSageEmeraldContainer,
    background = WarmParchmentBackground,
    onBackground = OnWarmParchmentBackground,
    surface = WarmParchmentSurface,
    onSurface = OnWarmParchmentSurface,
    surfaceVariant = SurfaceVariantCream,
    onSurfaceVariant = OnSurfaceVariantCream,
    outline = OutlineWarm
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun ScholarHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
