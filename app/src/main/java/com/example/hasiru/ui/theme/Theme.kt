package com.example.hasiru.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Private colours – only used inside the theme builder.
private val LightColorScheme = lightColorScheme(
    primary = Green40,
    onPrimary = White,
    primaryContainer = Green80,
    onPrimaryContainer = Green20,
    secondary = Amber40,
    onSecondary = White,
    secondaryContainer = Amber80,
    onSecondaryContainer = Amber20,
    tertiary = Brown40,
    onTertiary = White,
    tertiaryContainer = Brown80,
    onTertiaryContainer = Brown20,
    error = Red40,
    onError = White,
    errorContainer = Red80,
    onErrorContainer = Red20,
    background = White,
    onBackground = Color(0xFF1A1C1E),
    surface = White,
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = SoftGrey,
    onSurfaceVariant = TextGrey,
    outline = BorderGrey
)

private val DarkColorScheme = darkColorScheme(
    primary = Green80,
    onPrimary = Green20,
    primaryContainer = Green40,
    onPrimaryContainer = Green80,
    secondary = Amber80,
    onSecondary = Amber20,
    secondaryContainer = Amber40,
    onSecondaryContainer = Amber80,
    tertiary = Brown80,
    onTertiary = Brown20,
    tertiaryContainer = Brown40,
    onTertiaryContainer = Brown80,
    error = Red80,
    onError = Red20,
    errorContainer = Red40,
    onErrorContainer = Red80,
    background = Color(0xFF121212), // Standard dark background
    onBackground = White,
    surface = Color(0xFF1E1E1E), // Slightly lighter surface
    onSurface = White,
    surfaceVariant = Green20,
    onSurfaceVariant = Green80,
    outline = Green80
)

@Composable
fun HasiruTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,   // Set to false to ensure brand colours are used
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,    // we'll keep the default for now
        content = content
    )
}