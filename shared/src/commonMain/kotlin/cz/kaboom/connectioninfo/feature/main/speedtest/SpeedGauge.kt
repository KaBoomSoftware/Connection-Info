package cz.kaboom.connectioninfo.feature.main.speedtest

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cz.kaboom.connectioninfo.feature.main.MainLayoutSpec
import cz.kaboom.connectioninfo.feature.main.formatSpeedNumber
import cz.kaboom.connectioninfo.ui.theme.ConnectionInfoColors
import kotlin.math.min

/** Animated semi-circular gauge for the current Mbps value. */
@Composable
internal fun SpeedGauge(
    value: Float,
    maxValue: Float,
    modifier: Modifier = Modifier,
    layoutSpec: MainLayoutSpec
) {
    val animatedValue by animateFloatAsState(
        targetValue = value.coerceIn(0f, maxValue),
        animationSpec = tween(
            durationMillis = 850,
            easing = LinearOutSlowInEasing
        ),
        label = "speedGaugeValue"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth(layoutSpec.gaugeFillFraction)
                .aspectRatio(1f)
        ) {
            val strokeWidth = layoutSpec.gaugeStrokeWidth.toPx()
            val diameter = min(size.width, size.height) - strokeWidth
            val topLeft = Offset(
                x = (size.width - diameter) / 2f,
                y = (size.height - diameter) / 2f
            )
            val arcSize = Size(diameter, diameter)
            val startAngle = 135f
            val totalSweep = 270f
            val progressSweep = (animatedValue / maxValue).coerceIn(0f, 1f) * totalSweep
            val trackStroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            val progressStroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            val progressBrush = Brush.sweepGradient(
                colorStops = arrayOf(
                    0.000f to Color(0xFF22C55E),
                    0.125f to Color(0xFF22C55E),
                    0.375f to Color(0xFFEF4444),
                    0.625f to Color(0xFFF59E0B),
                    0.875f to Color(0xFFF4D35E),
                    1.000f to Color(0xFF84CC16)
                ),
                center = Offset(size.width / 2f, size.height / 2f)
            )

            drawArc(
                color = ConnectionInfoColors.SurfaceLine.copy(alpha = 0.55f),
                startAngle = startAngle,
                sweepAngle = totalSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = trackStroke
            )

            if (progressSweep > 0f) {
                drawArc(
                    brush = progressBrush,
                    startAngle = startAngle,
                    sweepAngle = progressSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = progressStroke
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formatSpeedNumber(animatedValue),
                color = ConnectionInfoColors.TextPrimary,
                fontSize = if (animatedValue >= 100f) {
                    layoutSpec.gaugeValueSmallTextSize
                } else {
                    layoutSpec.gaugeValueLargeTextSize
                },
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Mbps",
                color = ConnectionInfoColors.TextSecondary,
                fontSize = layoutSpec.gaugeUnitTextSize,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
