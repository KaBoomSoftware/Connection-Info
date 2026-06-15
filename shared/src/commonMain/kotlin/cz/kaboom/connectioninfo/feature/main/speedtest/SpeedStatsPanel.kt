package cz.kaboom.connectioninfo.feature.main.speedtest

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import cz.kaboom.connectioninfo.feature.main.MainLayoutSpec
import cz.kaboom.connectioninfo.feature.main.UiText
import cz.kaboom.connectioninfo.feature.main.formatLatency
import cz.kaboom.connectioninfo.feature.main.formatSpeed
import cz.kaboom.connectioninfo.presentation.main.LatencyStats
import cz.kaboom.connectioninfo.presentation.main.SpeedRateStats
import cz.kaboom.connectioninfo.ui.theme.ConnectionInfoColors

private const val LongSpeedValueLength = 9

/** Combined panel showing latency, download, and upload statistics. */
@Composable
internal fun SpeedStatsPanel(
    ping: LatencyStats,
    download: SpeedRateStats,
    upload: SpeedRateStats,
    layoutSpec: MainLayoutSpec
) {
    Surface(
        color = ConnectionInfoColors.Surface,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, ConnectionInfoColors.SurfaceLine),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = layoutSpec.statsHorizontalPadding,
                vertical = layoutSpec.statsVerticalPadding
            )
        ) {
            PingSummaryRow(
                latency = ping,
                layoutSpec = layoutSpec
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = layoutSpec.statsDividerVerticalPadding)
                    .height(1.dp)
                    .background(ConnectionInfoColors.SurfaceLine.copy(alpha = 0.75f))
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.width(layoutSpec.statsLabelWidth))
                SpeedHeaderText(
                    text = UiText.last,
                    layoutSpec = layoutSpec,
                    modifier = Modifier.weight(1f)
                )
                SpeedHeaderText(
                    text = UiText.max,
                    layoutSpec = layoutSpec,
                    modifier = Modifier.weight(1f)
                )
                SpeedHeaderText(
                    text = UiText.avg,
                    layoutSpec = layoutSpec,
                    modifier = Modifier.weight(1f)
                )
            }
            SpeedStatsRow(
                label = UiText.download,
                stats = download,
                layoutSpec = layoutSpec
            )
            SpeedStatsRow(
                label = UiText.upload,
                stats = upload,
                layoutSpec = layoutSpec
            )
        }
    }
}

/** Latency summary row with current, best, and average ping values. */
@Composable
private fun PingSummaryRow(
    latency: LatencyStats,
    layoutSpec: MainLayoutSpec
) {
    val hasSamples = latency.count > 0
    val current = latency.current.takeIf { hasSamples }?.let { formatLatency(it) } ?: "-- ms"
    val best = latency.best.takeIf { hasSamples }?.let { formatLatency(it) } ?: "-- ms"
    val average = latency.average.takeIf { hasSamples }?.let { formatLatency(it) } ?: "-- ms"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(layoutSpec.pingRowHeight),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = UiText.ping,
                color = ConnectionInfoColors.TextPrimary,
                fontSize = layoutSpec.pingLabelTextSize,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = UiText.latency,
                color = ConnectionInfoColors.TextSecondary,
                fontSize = layoutSpec.pingSubtitleTextSize,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Text(
            text = current,
            color = ConnectionInfoColors.SpeedValue,
            fontSize = layoutSpec.pingCurrentTextSize,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${UiText.best} $best",
                color = ConnectionInfoColors.TextSecondary,
                fontSize = layoutSpec.pingMetaTextSize,
                lineHeight = layoutSpec.pingMetaLineHeight,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End
            )
            Text(
                text = "${UiText.avgShort} $average",
                color = ConnectionInfoColors.TextSecondary,
                fontSize = layoutSpec.pingMetaTextSize,
                lineHeight = layoutSpec.pingMetaLineHeight,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

/** Header text used by the throughput stats table. */
@Composable
private fun SpeedHeaderText(
    text: String,
    layoutSpec: MainLayoutSpec,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = ConnectionInfoColors.TextSecondary,
        fontSize = layoutSpec.statsHeaderTextSize,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.End,
        modifier = modifier
    )
}

/** One throughput row for download or upload metrics. */
@Composable
private fun SpeedStatsRow(
    label: String,
    stats: SpeedRateStats,
    layoutSpec: MainLayoutSpec
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(layoutSpec.statsRowHeight),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = ConnectionInfoColors.TextPrimary,
            fontSize = layoutSpec.statsLabelTextSize,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(layoutSpec.statsLabelWidth)
        )
        SpeedValueText(
            text = stats.current.formattedSpeedOrEmpty(hasSamples = stats.count > 0),
            layoutSpec = layoutSpec
        )
        SpeedValueText(
            text = stats.maximum.formattedSpeedOrEmpty(hasSamples = stats.count > 0),
            layoutSpec = layoutSpec
        )
        SpeedValueText(
            text = stats.average.formattedSpeedOrEmpty(hasSamples = stats.count > 0),
            layoutSpec = layoutSpec
        )
    }
}

/** Highlighted numeric value cell inside the throughput stats table. */
@Composable
private fun RowScope.SpeedValueText(
    text: String,
    layoutSpec: MainLayoutSpec
) {
    val valueTextSize = if (text.length > LongSpeedValueLength) {
        layoutSpec.statsValueCompactTextSize
    } else {
        layoutSpec.statsValueTextSize
    }

    Text(
        text = text,
        color = ConnectionInfoColors.SpeedValue,
        fontSize = valueTextSize,
        textAlign = TextAlign.End,
        maxLines = 1,
        overflow = TextOverflow.Clip,
        softWrap = false,
        modifier = Modifier
            .weight(1f)
            .padding(start = 8.dp)
    )
}

/** Formats a throughput value only after at least one sample has arrived. */
private fun Float.formattedSpeedOrEmpty(hasSamples: Boolean): String {
    return takeIf { hasSamples }?.let { formatSpeed(it) }.orEmpty()
}
