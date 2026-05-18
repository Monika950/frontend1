package com.example.treasurehuntapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AppDarkColorScheme = darkColorScheme(
    primary = AppColors.PurplePrimary,
    onPrimary = AppColors.TextPrimary,
    secondary = AppColors.PurpleAccent,
    onSecondary = AppColors.TextPrimary,
    background = AppColors.BgBottom,
    onBackground = AppColors.TextPrimary,
    surface = AppColors.Surface,
    onSurface = AppColors.TextPrimary,
    surfaceVariant = AppColors.SurfaceAlt,
    onSurfaceVariant = AppColors.TextSecondary,
    error = AppColors.Error,
    onError = AppColors.TextPrimary,
    outline = AppColors.Border
)

private val AppLightColorScheme = lightColorScheme(
    primary = AppColors.PurplePrimary,
    onPrimary = AppColors.TextPrimary,
    secondary = AppColors.PurpleAccent,
    onSecondary = AppColors.TextPrimary
)

@Composable
fun AppTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) AppDarkColorScheme else AppLightColorScheme,
        typography = AppTypography,
        shapes = Shapes,
        content = content
    )
}
