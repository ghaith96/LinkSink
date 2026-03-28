package com.linksink.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

internal val DarkColorScheme = darkColorScheme(
    primary = LinkSinkPrimaryLight,
    onPrimary = LinkSinkOnSecondaryDark,
    primaryContainer = LinkSinkPrimaryContainerDark,
    onPrimaryContainer = LinkSinkOnPrimaryContainerDark,
    secondary = LinkSinkSecondaryDark,
    onSecondary = LinkSinkOnSecondaryDark,
    secondaryContainer = LinkSinkSecondaryContainerDark,
    onSecondaryContainer = LinkSinkOnSecondaryContainerDark,
    tertiary = LinkSinkTertiaryDark,
    onTertiary = LinkSinkOnTertiaryDark,
    tertiaryContainer = LinkSinkTertiaryContainerDark,
    onTertiaryContainer = LinkSinkOnTertiaryContainerDark,
    background = LinkSinkBackgroundDark,
    onBackground = LinkSinkOnBackgroundDark,
    surface = LinkSinkSurfaceDark,
    onSurface = LinkSinkOnSurfaceDark,
    surfaceVariant = LinkSinkSurfaceVariantDark,
    onSurfaceVariant = LinkSinkOnSurfaceVariantDark,
    outline = LinkSinkOutlineDark,
    outlineVariant = LinkSinkOutlineVariantDark,
)

internal val LightColorScheme = lightColorScheme(
    primary = LinkSinkPrimary,
    onPrimary = LinkSinkOnSecondary,
    primaryContainer = LinkSinkPrimaryContainer,
    onPrimaryContainer = LinkSinkOnPrimaryContainer,
    secondary = LinkSinkSecondary,
    onSecondary = LinkSinkOnSecondary,
    secondaryContainer = LinkSinkSecondaryContainer,
    onSecondaryContainer = LinkSinkOnSecondaryContainer,
    tertiary = LinkSinkTertiary,
    onTertiary = LinkSinkOnTertiary,
    tertiaryContainer = LinkSinkTertiaryContainer,
    onTertiaryContainer = LinkSinkOnTertiaryContainer,
    background = LinkSinkBackground,
    onBackground = LinkSinkOnBackground,
    surface = LinkSinkSurface,
    onSurface = LinkSinkOnSurface,
    surfaceVariant = LinkSinkSurfaceVariant,
    onSurfaceVariant = LinkSinkOnSurfaceVariant,
    outline = LinkSinkOutline,
    outlineVariant = LinkSinkOutlineVariant,
)

@Composable
fun LinkSinkTheme(
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
        typography = LinkSinkTypography,
        content = content
    )
}
