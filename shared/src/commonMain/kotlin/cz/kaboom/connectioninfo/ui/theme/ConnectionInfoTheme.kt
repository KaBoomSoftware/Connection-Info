package cz.kaboom.connectioninfo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

/** Application Material theme with a custom dark palette and bundled Roboto font. */
@Composable
fun ConnectionInfoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = ConnectionInfoColors.MaterialPurple,
            background = ConnectionInfoColors.Background,
            surface = ConnectionInfoColors.Surface,
            onPrimary = ConnectionInfoColors.ActionText,
            onBackground = ConnectionInfoColors.TextPrimary,
            onSurface = ConnectionInfoColors.TextPrimary
        ),
        typography = MaterialTheme.typography.copy(
            bodyLarge = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.SansSerif),
            bodyMedium = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.SansSerif),
            labelLarge = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.SansSerif),
            titleLarge = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.SansSerif)
        ),
        content = content
    )
}
