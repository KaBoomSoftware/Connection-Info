package cz.kaboom.connectioninfo.ui.theme

import androidx.compose.ui.graphics.Color

/** Central color tokens used across the custom Material 3 UI. */
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

    /** Gauge gradient: low-speed green anchor. */
    val GaugeLow = Color(0xFF22C55E)

    /** Gauge gradient: high-speed red peak. */
    val GaugeHigh = Color(0xFFEF4444)

    /** Gauge gradient: mid-speed amber. */
    val GaugeMid = Color(0xFFF59E0B)

    /** Gauge gradient: upper-mid yellow. */
    val GaugeUpperMid = Color(0xFFF4D35E)

    /** Gauge gradient: top-end lime wrap-around. */
    val GaugeLime = Color(0xFF84CC16)
}
