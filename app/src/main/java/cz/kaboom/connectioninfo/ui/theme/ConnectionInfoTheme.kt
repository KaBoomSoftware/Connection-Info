package cz.kaboom.connectioninfo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import cz.kaboom.connectioninfo.R

/**
 * Central color tokens used across the custom Material 3 UI.
 */
object ConnectionInfoColors {
    /** Screen background. */
    val Background = Color(0xFF0B1020)

    /** Card and panel background. */
    val Surface = Color(0xFF111827)

    /** Borders, dividers, and inactive progress tracks. */
    val SurfaceLine = Color(0xFF263247)

    /** Primary readable text. */
    val TextPrimary = Color(0xFFE7ECF4)

    /** Secondary labels and metadata. */
    val TextSecondary = Color(0xFF9AA7BA)

    /** Muted footer text. */
    val TextMuted = Color(0xFF667085)

    /** Numeric metric highlight color. */
    val SpeedValue = Color(0xFFF4D35E)

    /** Error text and destructive status color. */
    val Error = Color(0xFFEF4444)

    /** Material-inspired accent used for tabs, progress, and actions. */
    val MaterialPurple = Color(0xFFBB86FC)

    /** Text color placed on top of bright accent buttons. */
    val ActionText = Color(0xFF12061F)
}

/**
 * Application Material theme with a custom dark palette and bundled Roboto font.
 */
@Composable
fun ConnectionInfoTheme(content: @Composable () -> Unit) {
    /** Font family intentionally scoped here so previews and runtime share identical typography. */
    val roboto = FontFamily(Font(R.font.roboto))

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
            bodyLarge = MaterialTheme.typography.bodyLarge.copy(fontFamily = roboto),
            bodyMedium = MaterialTheme.typography.bodyMedium.copy(fontFamily = roboto),
            labelLarge = MaterialTheme.typography.labelLarge.copy(fontFamily = roboto),
            titleLarge = MaterialTheme.typography.titleLarge.copy(fontFamily = roboto)
        ),
        content = content
    )
}
