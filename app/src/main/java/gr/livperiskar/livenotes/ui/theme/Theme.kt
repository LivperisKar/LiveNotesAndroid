package gr.livperiskar.livenotes.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = LNBlue,
    secondary = LNGreen,
    tertiary = LNPink,
    background = LNDarkBackground,
    surface = LNDarkSurface,
    onPrimary = LNWhite,
    onSecondary = LNWhite,
    onTertiary = LNWhite,
    onBackground = LNWhite,
    onSurface = LNWhite
)

private val LightColorScheme = lightColorScheme(
    primary = LNBlue,
    secondary = LNGreen,
    tertiary = LNPink,
    background = LNWhiteSoft,
    surface = LNWhite,
    onPrimary = LNWhite,
    onSecondary = LNWhite,
    onTertiary = LNWhite,
    onBackground = LNBlack,
    onSurface = LNBlack
)

@Composable
fun LivenotesTheme(
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
