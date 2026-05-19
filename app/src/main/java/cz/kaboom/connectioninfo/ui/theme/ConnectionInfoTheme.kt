package cz.kaboom.connectioninfo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import cz.kaboom.connectioninfo.R

object ConnectionInfoColors {
    val Background = Color(0xFF0B1020)
    val Surface = Color(0xFF111827)
    val SurfaceLine = Color(0xFF263247)
    val TextPrimary = Color(0xFFE7ECF4)
    val TextSecondary = Color(0xFF9AA7BA)
    val TextMuted = Color(0xFF667085)
    val SpeedValue = Color(0xFFF4D35E)
    val Error = Color(0xFFEF4444)
    val MaterialPurple = Color(0xFFBB86FC)
    val ActionText = Color(0xFF12061F)
}

@Composable
fun ConnectionInfoTheme(content: @Composable () -> Unit) {
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
