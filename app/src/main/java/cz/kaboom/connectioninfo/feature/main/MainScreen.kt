package cz.kaboom.connectioninfo.feature.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cz.kaboom.connectioninfo.R
import cz.kaboom.connectioninfo.domain.model.NetworkDetails
import cz.kaboom.connectioninfo.domain.model.NetworkLookup
import cz.kaboom.connectioninfo.domain.model.NetworkTransport
import cz.kaboom.connectioninfo.domain.model.SpeedTestPhase
import cz.kaboom.connectioninfo.presentation.main.MainAction
import cz.kaboom.connectioninfo.presentation.main.MainTab
import cz.kaboom.connectioninfo.presentation.main.MainUiState
import cz.kaboom.connectioninfo.presentation.main.LatencyStats
import cz.kaboom.connectioninfo.presentation.main.SpeedRateStats
import cz.kaboom.connectioninfo.presentation.main.SpeedTestUiState
import cz.kaboom.connectioninfo.ui.theme.ConnectionInfoColors
import cz.kaboom.connectioninfo.ui.theme.ConnectionInfoTheme
import java.util.Locale
import kotlin.math.min

/** Minimum horizontal drag distance required to switch tabs. */
private val TabSwipeThreshold = 72.dp

/**
 * Root composable for the application.
 *
 * It owns the tab chrome, animated page transitions, swipe navigation, and footer version label
 * while delegating feature content to focused child composables.
 */
@Composable
fun ConnectionInfoApp(
    state: MainUiState,
    versionName: String,
    onAction: (MainAction) -> Unit
) {
    val swipeThresholdPx = with(LocalDensity.current) { TabSwipeThreshold.toPx() }
    val selectedTab = state.selectedTab

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ConnectionInfoColors.Background)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        AppTabs(
            selectedTab = state.selectedTab,
            onTabSelected = { onAction(MainAction.SelectTab(it)) }
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(selectedTab, swipeThresholdPx) {
                    var dragDistance = 0f
                    detectHorizontalDragGestures(
                        onDragStart = { dragDistance = 0f },
                        onHorizontalDrag = { _, dragAmount ->
                            dragDistance += dragAmount
                        },
                        onDragEnd = {
                            val targetTab = when {
                                dragDistance < -swipeThresholdPx && selectedTab == MainTab.SPEED_TEST ->
                                    MainTab.NETWORK_INFO

                                dragDistance > swipeThresholdPx && selectedTab == MainTab.NETWORK_INFO ->
                                    MainTab.SPEED_TEST

                                else -> null
                            }

                            targetTab?.let { onAction(MainAction.SelectTab(it)) }
                            dragDistance = 0f
                        },
                        onDragCancel = { dragDistance = 0f }
                    )
                }
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    val direction = if (targetState.index > initialState.index) {
                        AnimatedContentTransitionScope.SlideDirection.Left
                    } else {
                        AnimatedContentTransitionScope.SlideDirection.Right
                    }

                    slideIntoContainer(
                        towards = direction,
                        animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing)
                    ) togetherWith slideOutOfContainer(
                        towards = direction,
                        animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing)
                    ) using SizeTransform(clip = false)
                },
                label = "tabContentTransition"
            ) { tab ->
                when (tab) {
                    MainTab.SPEED_TEST -> SpeedTestScreen(
                        state = state.speedTest,
                        internetAvailable = state.internetAvailable,
                        onToggleTest = { onAction(MainAction.ToggleSpeedTest) }
                    )

                    MainTab.NETWORK_INFO -> NetworkInfoScreen(
                        info = if (state.internetAvailable) state.networkInfo else null
                    )
                }
            }
        }

        Text(
            text = stringResource(R.string.version, versionName),
            color = ConnectionInfoColors.TextMuted,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        )
    }
}

/** Ordering helper used to choose the animated page transition direction. */
private val MainTab.index: Int
    get() = when (this) {
        MainTab.SPEED_TEST -> 0
        MainTab.NETWORK_INFO -> 1
    }

/** Top tab row for switching between speed test and network information pages. */
@Composable
private fun AppTabs(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppTabItem(
            text = stringResource(R.string.speed_test).uppercase(Locale.getDefault()),
            selected = selectedTab == MainTab.SPEED_TEST,
            onClick = { onTabSelected(MainTab.SPEED_TEST) },
            modifier = Modifier.weight(1f)
        )
        AppTabItem(
            text = stringResource(R.string.network_info).uppercase(Locale.getDefault()),
            selected = selectedTab == MainTab.NETWORK_INFO,
            onClick = { onTabSelected(MainTab.NETWORK_INFO) },
            modifier = Modifier.weight(1f)
        )
    }
}

/** Single tab label with the active underline affordance. */
@Composable
private fun AppTabItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(72.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = text,
            color = if (selected) ConnectionInfoColors.MaterialPurple else ConnectionInfoColors.TextSecondary,
            fontSize = 16.sp,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(18.dp))
        Box(
            modifier = Modifier
                .width(118.dp)
                .height(3.dp)
                .background(if (selected) ConnectionInfoColors.MaterialPurple else Color.Transparent)
        )
    }
}

/** Speed test page containing the gauge, metric panel, progress copy, and action button. */
@Composable
private fun SpeedTestScreen(
    state: SpeedTestUiState,
    internetAvailable: Boolean,
    onToggleTest: () -> Unit
) {
    val speedFormat = stringResource(R.string.speed_format)
    val latencyFormat = stringResource(R.string.latency_format)
    val progressText = when (state.phase) {
        SpeedTestPhase.PING -> stringResource(R.string.ping_progress_format, state.progress * 100f)
        SpeedTestPhase.DOWNLOAD -> stringResource(R.string.dl_progress_format, state.progress * 100f)
        SpeedTestPhase.UPLOAD -> stringResource(R.string.ul_progress_format, state.progress * 100f)
        SpeedTestPhase.IDLE -> ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SpeedGauge(
            value = state.gaugeValue,
            maxValue = 500f,
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        )

        Text(
            text = stringResource(R.string.speed),
            color = ConnectionInfoColors.TextPrimary,
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        SpeedStatsPanel(
            speedFormat = speedFormat,
            latencyFormat = latencyFormat,
            ping = state.ping,
            download = state.download,
            upload = state.upload
        )

        ProgressBar(
            progress = state.progress,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp)
                .height(6.dp)
        )

        Text(
            text = progressText,
            color = ConnectionInfoColors.TextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        )

        if (internetAvailable) {
            Button(
                onClick = onToggleTest,
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ConnectionInfoColors.MaterialPurple,
                    contentColor = ConnectionInfoColors.ActionText
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 22.dp)
                    .height(64.dp)
            ) {
                Text(
                    text = stringResource(if (state.running) R.string.stop_test else R.string.start_test)
                        .uppercase(Locale.getDefault()),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 22.dp)
                    .height(64.dp)
            ) {
                Text(
                    text = stringResource(R.string.network_unavailable).uppercase(Locale.getDefault()),
                    color = ConnectionInfoColors.Error,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/** Compact horizontal progress bar used for ping, download, and upload phases. */
@Composable
private fun ProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(3.dp))
            .background(ConnectionInfoColors.SurfaceLine)
    ) {
        if (progress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(ConnectionInfoColors.MaterialPurple)
            )
        }
    }
}

/** Animated semi-circular gauge for the current Mbps value. */
@Composable
private fun SpeedGauge(
    value: Float,
    maxValue: Float,
    modifier: Modifier = Modifier
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
                .fillMaxWidth(0.68f)
                .aspectRatio(1f)
        ) {
            val strokeWidth = 36.dp.toPx()
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
                fontSize = if (animatedValue >= 100f) 42.sp else 48.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Mbps",
                color = ConnectionInfoColors.TextSecondary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

/** Combined panel showing latency, download, and upload statistics. */
@Composable
private fun SpeedStatsPanel(
    speedFormat: String,
    latencyFormat: String,
    ping: LatencyStats,
    download: SpeedRateStats,
    upload: SpeedRateStats
) {
    Surface(
        color = ConnectionInfoColors.Surface,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, ConnectionInfoColors.SurfaceLine),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
            PingSummaryRow(
                latency = ping,
                latencyFormat = latencyFormat
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .height(1.dp)
                    .background(ConnectionInfoColors.SurfaceLine.copy(alpha = 0.75f))
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.width(96.dp))
                SpeedHeaderText(text = stringResource(R.string.last), modifier = Modifier.weight(1f))
                SpeedHeaderText(text = stringResource(R.string.max), modifier = Modifier.weight(1f))
                SpeedHeaderText(text = stringResource(R.string.avg), modifier = Modifier.weight(1f))
            }
            SpeedStatsRow(
                label = stringResource(R.string.download),
                stats = download,
                speedFormat = speedFormat
            )
            SpeedStatsRow(
                label = stringResource(R.string.upload),
                stats = upload,
                speedFormat = speedFormat
            )
        }
    }
}

/** Latency summary row with current, best, and average ping values. */
@Composable
private fun PingSummaryRow(
    latency: LatencyStats,
    latencyFormat: String
) {
    val hasSamples = latency.count > 0
    val current = latency.current.takeIf { hasSamples }?.let { formatLatency(it, latencyFormat) } ?: "-- ms"
    val best = latency.best.takeIf { hasSamples }?.let { formatLatency(it, latencyFormat) } ?: "-- ms"
    val average = latency.average.takeIf { hasSamples }?.let { formatLatency(it, latencyFormat) } ?: "-- ms"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.ping),
                color = ConnectionInfoColors.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.latency),
                color = ConnectionInfoColors.TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Text(
            text = current,
            color = ConnectionInfoColors.SpeedValue,
            fontSize = 20.sp,
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
                text = "${stringResource(R.string.best)} $best",
                color = ConnectionInfoColors.TextSecondary,
                fontSize = 10.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End
            )
            Text(
                text = "${stringResource(R.string.avg_short)} $average",
                color = ConnectionInfoColors.TextSecondary,
                fontSize = 10.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

/** Header text used by the throughput stats table. */
@Composable
private fun SpeedHeaderText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = ConnectionInfoColors.TextSecondary,
        fontSize = 13.sp,
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
    speedFormat: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = ConnectionInfoColors.TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(96.dp)
        )
        SpeedValueText(text = stats.current.takeIf { stats.count > 0 }?.let { formatSpeed(it, speedFormat) } ?: "")
        SpeedValueText(text = stats.maximum.takeIf { stats.count > 0 }?.let { formatSpeed(it, speedFormat) } ?: "")
        SpeedValueText(text = stats.average.takeIf { stats.count > 0 }?.let { formatSpeed(it, speedFormat) } ?: "")
    }
}

/** Highlighted numeric value cell inside the throughput stats table. */
@Composable
private fun RowScope.SpeedValueText(text: String) {
    Text(
        text = text,
        color = ConnectionInfoColors.SpeedValue,
        fontSize = 11.sp,
        textAlign = TextAlign.End,
        modifier = Modifier
            .weight(1f)
            .padding(start = 8.dp)
    )
}

/** Network information page showing local and public connection metadata. */
@Composable
private fun NetworkInfoScreen(info: NetworkDetails?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = ConnectionInfoColors.Surface,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, ConnectionInfoColors.SurfaceLine),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                NetworkInfoRow(stringResource(R.string.network), info?.transport?.displayName.orEmpty())
                NetworkInfoRow(stringResource(R.string.internal_ip), info?.internalIp.orEmpty())
                NetworkInfoRow(stringResource(R.string.external_ip), info?.externalIp.orEmpty())
                NetworkInfoRow(stringResource(R.string.isp), info?.lookup?.isp.orEmpty())
                NetworkInfoRow(stringResource(R.string.org), info?.lookup?.organization.orEmpty())
                NetworkInfoRow(stringResource(R.string.city), info?.lookup?.city.orEmpty())
                NetworkInfoRow(stringResource(R.string.region), info?.lookup?.region.orEmpty())
                NetworkInfoRow(stringResource(R.string.region_name), info?.lookup?.regionName.orEmpty())
                NetworkInfoRow(stringResource(R.string.country), info?.lookup?.country.orEmpty())
                NetworkInfoRow(stringResource(R.string.country_code), info?.lookup?.countryCode.orEmpty())
                NetworkInfoRow(stringResource(R.string.latitude), info?.lookup?.latitude.orEmpty())
                NetworkInfoRow(stringResource(R.string.longitude), info?.lookup?.longitude.orEmpty())
            }
        }
    }
}

/** Label/value row used in the Network Info card. */
@Composable
private fun NetworkInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = ConnectionInfoColors.TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(128.dp)
        )
        Text(
            text = value,
            color = ConnectionInfoColors.SpeedValue,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

/** Formats Mbps values with the localized string resource pattern. */
private fun formatSpeed(value: Float, speedFormat: String): String {
    return String.format(Locale.getDefault(), speedFormat, value)
}

/** Formats latency values with the localized string resource pattern. */
private fun formatLatency(value: Float, latencyFormat: String): String {
    return String.format(Locale.getDefault(), latencyFormat, value)
}

/** Formats the central gauge value without units. */
private fun formatSpeedNumber(value: Float): String {
    return String.format(Locale.getDefault(), "%.2f", value)
}

/** Preview of the speed screen with representative metrics. */
@Preview(widthDp = 393, heightDp = 852)
@Composable
private fun RunningSpeedPreview() {
    ConnectionInfoTheme {
        ConnectionInfoApp(
            state = MainUiState(
                internetAvailable = true,
                speedTest = SpeedTestUiState(
                    running = true,
                    phase = SpeedTestPhase.DOWNLOAD,
                    gaugeValue = 327.5f,
                    progress = 0.56f,
                    ping = LatencyStats(current = 18f, best = 14f, total = 84f, count = 5),
                    download = SpeedRateStats(current = 327.5f, maximum = 372.1f, total = 980f, count = 3)
                )
            ),
            versionName = "1.0.272",
            onAction = {}
        )
    }
}

/** Preview of the network information screen with realistic long IP values. */
@Preview(widthDp = 393, heightDp = 852)
@Composable
private fun NetworkInfoPreview() {
    ConnectionInfoTheme {
        ConnectionInfoApp(
            state = MainUiState(
                selectedTab = MainTab.NETWORK_INFO,
                internetAvailable = true,
                networkInfo = NetworkDetails(
                    transport = NetworkTransport.WIFI,
                    internalIp = "2001:1AEB:7E80:AF00:3412:90FF:FE94:4E0C",
                    externalIp = "193.86.34.190",
                    lookup = NetworkLookup(
                        isp = "T-Mobile Czech Republic a.s.",
                        organization = "TMCZ FIXoVF STATIC",
                        city = "Prague",
                        region = "10",
                        regionName = "Prague",
                        country = "Czechia",
                        countryCode = "CZ",
                        latitude = "50.0948",
                        longitude = "14.4785"
                    )
                )
            ),
            versionName = "1.0.272",
            onAction = {}
        )
    }
}
