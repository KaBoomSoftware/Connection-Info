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
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
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

/** Values at or above this Mbps threshold are shown as Gbps in table cells. */
private const val SPEED_GBPS_THRESHOLD_MBPS = 1_000f

/** Long table values use a smaller font to stay on one line on compact screens. */
private const val LONG_SPEED_VALUE_LENGTH = 9

/** Responsive sizing values for compact phones, regular phones, and expanded devices. */
private data class MainLayoutSpec(
    val contentMaxWidth: Dp,
    val horizontalPadding: Dp,
    val speedVerticalPadding: Dp,
    val networkVerticalPadding: Dp,
    val tabHeight: Dp,
    val tabItemHeight: Dp,
    val tabFontSize: TextUnit,
    val tabLetterSpacing: TextUnit,
    val tabIndicatorWidth: Dp,
    val tabIndicatorTopSpacing: Dp,
    val footerVerticalPadding: Dp,
    val versionTextSize: TextUnit,
    val copyrightTextSize: TextUnit,
    val gaugeHeight: Dp,
    val gaugeFillFraction: Float,
    val gaugeStrokeWidth: Dp,
    val gaugeValueLargeTextSize: TextUnit,
    val gaugeValueSmallTextSize: TextUnit,
    val gaugeUnitTextSize: TextUnit,
    val sectionTitleTextSize: TextUnit,
    val statsHorizontalPadding: Dp,
    val statsVerticalPadding: Dp,
    val statsDividerVerticalPadding: Dp,
    val statsLabelWidth: Dp,
    val statsRowHeight: Dp,
    val pingRowHeight: Dp,
    val pingLabelTextSize: TextUnit,
    val pingSubtitleTextSize: TextUnit,
    val pingCurrentTextSize: TextUnit,
    val pingMetaTextSize: TextUnit,
    val pingMetaLineHeight: TextUnit,
    val statsHeaderTextSize: TextUnit,
    val statsLabelTextSize: TextUnit,
    val statsValueTextSize: TextUnit,
    val statsValueCompactTextSize: TextUnit,
    val buttonHeight: Dp,
    val actionTopPadding: Dp,
    val actionTextSize: TextUnit,
    val progressTopPadding: Dp,
    val networkLabelWidth: Dp,
    val networkCardHorizontalPadding: Dp,
    val networkCardVerticalPadding: Dp,
    val networkRowVerticalPadding: Dp,
    val networkTextSize: TextUnit,
    val networkValueLineHeight: TextUnit
)

/** Picks the layout density from the actual available window size. */
private fun mainLayoutSpec(maxWidth: Dp, maxHeight: Dp): MainLayoutSpec {
    val compact = maxWidth < 380.dp || maxHeight < 760.dp
    val expanded = maxWidth >= 600.dp

    return when {
        compact -> MainLayoutSpec(
            contentMaxWidth = 430.dp,
            horizontalPadding = 16.dp,
            speedVerticalPadding = 6.dp,
            networkVerticalPadding = 12.dp,
            tabHeight = 64.dp,
            tabItemHeight = 56.dp,
            tabFontSize = 13.sp,
            tabLetterSpacing = 1.2.sp,
            tabIndicatorWidth = 92.dp,
            tabIndicatorTopSpacing = 10.dp,
            footerVerticalPadding = 6.dp,
            versionTextSize = 12.sp,
            copyrightTextSize = 10.sp,
            gaugeHeight = 190.dp,
            gaugeFillFraction = 0.62f,
            gaugeStrokeWidth = 26.dp,
            gaugeValueLargeTextSize = 40.sp,
            gaugeValueSmallTextSize = 36.sp,
            gaugeUnitTextSize = 13.sp,
            sectionTitleTextSize = 17.sp,
            statsHorizontalPadding = 12.dp,
            statsVerticalPadding = 10.dp,
            statsDividerVerticalPadding = 6.dp,
            statsLabelWidth = 78.dp,
            statsRowHeight = 34.dp,
            pingRowHeight = 42.dp,
            pingLabelTextSize = 15.sp,
            pingSubtitleTextSize = 10.sp,
            pingCurrentTextSize = 18.sp,
            pingMetaTextSize = 9.sp,
            pingMetaLineHeight = 11.sp,
            statsHeaderTextSize = 11.sp,
            statsLabelTextSize = 14.sp,
            statsValueTextSize = 10.sp,
            statsValueCompactTextSize = 8.sp,
            buttonHeight = 52.dp,
            actionTopPadding = 14.dp,
            actionTextSize = 13.sp,
            progressTopPadding = 10.dp,
            networkLabelWidth = 108.dp,
            networkCardHorizontalPadding = 12.dp,
            networkCardVerticalPadding = 12.dp,
            networkRowVerticalPadding = 4.dp,
            networkTextSize = 12.sp,
            networkValueLineHeight = 16.sp
        )

        expanded -> MainLayoutSpec(
            contentMaxWidth = 640.dp,
            horizontalPadding = 28.dp,
            speedVerticalPadding = 22.dp,
            networkVerticalPadding = 32.dp,
            tabHeight = 88.dp,
            tabItemHeight = 72.dp,
            tabFontSize = 16.sp,
            tabLetterSpacing = 2.sp,
            tabIndicatorWidth = 118.dp,
            tabIndicatorTopSpacing = 18.dp,
            footerVerticalPadding = 12.dp,
            versionTextSize = 14.sp,
            copyrightTextSize = 12.sp,
            gaugeHeight = 300.dp,
            gaugeFillFraction = 0.66f,
            gaugeStrokeWidth = 36.dp,
            gaugeValueLargeTextSize = 50.sp,
            gaugeValueSmallTextSize = 44.sp,
            gaugeUnitTextSize = 16.sp,
            sectionTitleTextSize = 20.sp,
            statsHorizontalPadding = 18.dp,
            statsVerticalPadding = 14.dp,
            statsDividerVerticalPadding = 10.dp,
            statsLabelWidth = 96.dp,
            statsRowHeight = 40.dp,
            pingRowHeight = 46.dp,
            pingLabelTextSize = 16.sp,
            pingSubtitleTextSize = 11.sp,
            pingCurrentTextSize = 20.sp,
            pingMetaTextSize = 10.sp,
            pingMetaLineHeight = 12.sp,
            statsHeaderTextSize = 13.sp,
            statsLabelTextSize = 16.sp,
            statsValueTextSize = 11.sp,
            statsValueCompactTextSize = 9.sp,
            buttonHeight = 64.dp,
            actionTopPadding = 22.dp,
            actionTextSize = 14.sp,
            progressTopPadding = 14.dp,
            networkLabelWidth = 128.dp,
            networkCardHorizontalPadding = 16.dp,
            networkCardVerticalPadding = 14.dp,
            networkRowVerticalPadding = 5.dp,
            networkTextSize = 13.sp,
            networkValueLineHeight = 18.sp
        )

        else -> MainLayoutSpec(
            contentMaxWidth = 520.dp,
            horizontalPadding = 20.dp,
            speedVerticalPadding = 12.dp,
            networkVerticalPadding = 22.dp,
            tabHeight = 76.dp,
            tabItemHeight = 64.dp,
            tabFontSize = 15.sp,
            tabLetterSpacing = 1.6.sp,
            tabIndicatorWidth = 108.dp,
            tabIndicatorTopSpacing = 14.dp,
            footerVerticalPadding = 10.dp,
            versionTextSize = 13.sp,
            copyrightTextSize = 11.sp,
            gaugeHeight = 230.dp,
            gaugeFillFraction = 0.64f,
            gaugeStrokeWidth = 32.dp,
            gaugeValueLargeTextSize = 46.sp,
            gaugeValueSmallTextSize = 40.sp,
            gaugeUnitTextSize = 15.sp,
            sectionTitleTextSize = 18.sp,
            statsHorizontalPadding = 16.dp,
            statsVerticalPadding = 12.dp,
            statsDividerVerticalPadding = 8.dp,
            statsLabelWidth = 88.dp,
            statsRowHeight = 38.dp,
            pingRowHeight = 44.dp,
            pingLabelTextSize = 15.sp,
            pingSubtitleTextSize = 11.sp,
            pingCurrentTextSize = 19.sp,
            pingMetaTextSize = 10.sp,
            pingMetaLineHeight = 12.sp,
            statsHeaderTextSize = 12.sp,
            statsLabelTextSize = 15.sp,
            statsValueTextSize = 11.sp,
            statsValueCompactTextSize = 9.sp,
            buttonHeight = 58.dp,
            actionTopPadding = 18.dp,
            actionTextSize = 14.sp,
            progressTopPadding = 12.dp,
            networkLabelWidth = 120.dp,
            networkCardHorizontalPadding = 14.dp,
            networkCardVerticalPadding = 13.dp,
            networkRowVerticalPadding = 5.dp,
            networkTextSize = 13.sp,
            networkValueLineHeight = 18.sp
        )
    }
}

/**
 * Root composable for the application.
 *
 * It owns the tab chrome, animated page transitions, swipe navigation, and footer metadata while
 * delegating feature content to focused child composables.
 */
@Composable
fun ConnectionInfoApp(
    state: MainUiState,
    versionName: String,
    onAction: (MainAction) -> Unit
) {
    val swipeThresholdPx = with(LocalDensity.current) { TabSwipeThreshold.toPx() }
    val selectedTab = state.selectedTab

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(ConnectionInfoColors.Background)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        val layoutSpec = mainLayoutSpec(maxWidth, maxHeight)

        Column(modifier = Modifier.fillMaxSize()) {
        AppTabs(
            selectedTab = state.selectedTab,
            onTabSelected = { onAction(MainAction.SelectTab(it)) },
            layoutSpec = layoutSpec
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
                        onToggleTest = { onAction(MainAction.ToggleSpeedTest) },
                        layoutSpec = layoutSpec
                    )

                    MainTab.NETWORK_INFO -> NetworkInfoScreen(
                        info = if (state.internetAvailable) state.networkInfo else null,
                        layoutSpec = layoutSpec
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = layoutSpec.footerVerticalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = stringResource(R.string.version, versionName),
                color = ConnectionInfoColors.TextMuted,
                fontSize = layoutSpec.versionTextSize,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.copyright),
                color = ConnectionInfoColors.TextMuted,
                fontSize = layoutSpec.copyrightTextSize,
                textAlign = TextAlign.Center
            )
        }
        }
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
    onTabSelected: (MainTab) -> Unit,
    layoutSpec: MainLayoutSpec
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(layoutSpec.tabHeight),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppTabItem(
            text = stringResource(R.string.speed_test).uppercase(Locale.getDefault()),
            selected = selectedTab == MainTab.SPEED_TEST,
            onClick = { onTabSelected(MainTab.SPEED_TEST) },
            modifier = Modifier.weight(1f),
            layoutSpec = layoutSpec
        )
        AppTabItem(
            text = stringResource(R.string.network_info).uppercase(Locale.getDefault()),
            selected = selectedTab == MainTab.NETWORK_INFO,
            onClick = { onTabSelected(MainTab.NETWORK_INFO) },
            modifier = Modifier.weight(1f),
            layoutSpec = layoutSpec
        )
    }
}

/** Single tab label with the active underline affordance. */
@Composable
private fun AppTabItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    layoutSpec: MainLayoutSpec
) {
    Column(
        modifier = modifier
            .height(layoutSpec.tabItemHeight)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = text,
            color = if (selected) ConnectionInfoColors.MaterialPurple else ConnectionInfoColors.TextSecondary,
            fontSize = layoutSpec.tabFontSize,
            letterSpacing = layoutSpec.tabLetterSpacing,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(layoutSpec.tabIndicatorTopSpacing))
        Box(
            modifier = Modifier
                .width(layoutSpec.tabIndicatorWidth)
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
    onToggleTest: () -> Unit,
    layoutSpec: MainLayoutSpec
) {
    val speedFormat = stringResource(R.string.speed_format)
    val speedGbpsFormat = stringResource(R.string.speed_gbps_format)
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
            .padding(
                horizontal = layoutSpec.horizontalPadding,
                vertical = layoutSpec.speedVerticalPadding
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = layoutSpec.contentMaxWidth)
        ) {
            SpeedGauge(
                value = state.gaugeValue,
                maxValue = 500f,
                layoutSpec = layoutSpec,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(layoutSpec.gaugeHeight)
            )

            Text(
                text = stringResource(R.string.speed),
                color = ConnectionInfoColors.TextPrimary,
                fontSize = layoutSpec.sectionTitleTextSize,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 10.dp)
            )

            SpeedStatsPanel(
                speedFormat = speedFormat,
                speedGbpsFormat = speedGbpsFormat,
                latencyFormat = latencyFormat,
                ping = state.ping,
                download = state.download,
                upload = state.upload,
                layoutSpec = layoutSpec
            )

            ProgressBar(
                progress = state.progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = layoutSpec.progressTopPadding)
                    .height(6.dp)
            )

            Text(
                text = progressText,
                color = ConnectionInfoColors.TextSecondary,
                fontSize = layoutSpec.actionTextSize,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
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
                        .padding(top = layoutSpec.actionTopPadding)
                        .height(layoutSpec.buttonHeight)
                ) {
                    Text(
                        text = stringResource(if (state.running) R.string.stop_test else R.string.start_test)
                            .uppercase(Locale.getDefault()),
                        fontSize = layoutSpec.actionTextSize,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = layoutSpec.actionTopPadding)
                        .height(layoutSpec.buttonHeight)
                ) {
                    Text(
                        text = stringResource(R.string.network_unavailable).uppercase(Locale.getDefault()),
                        color = ConnectionInfoColors.Error,
                        fontSize = layoutSpec.actionTextSize,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    )
                }
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

/** Combined panel showing latency, download, and upload statistics. */
@Composable
private fun SpeedStatsPanel(
    speedFormat: String,
    speedGbpsFormat: String,
    latencyFormat: String,
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
                latencyFormat = latencyFormat,
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
                    text = stringResource(R.string.last),
                    layoutSpec = layoutSpec,
                    modifier = Modifier.weight(1f)
                )
                SpeedHeaderText(
                    text = stringResource(R.string.max),
                    layoutSpec = layoutSpec,
                    modifier = Modifier.weight(1f)
                )
                SpeedHeaderText(
                    text = stringResource(R.string.avg),
                    layoutSpec = layoutSpec,
                    modifier = Modifier.weight(1f)
                )
            }
            SpeedStatsRow(
                label = stringResource(R.string.download),
                stats = download,
                speedFormat = speedFormat,
                speedGbpsFormat = speedGbpsFormat,
                layoutSpec = layoutSpec
            )
            SpeedStatsRow(
                label = stringResource(R.string.upload),
                stats = upload,
                speedFormat = speedFormat,
                speedGbpsFormat = speedGbpsFormat,
                layoutSpec = layoutSpec
            )
        }
    }
}

/** Latency summary row with current, best, and average ping values. */
@Composable
private fun PingSummaryRow(
    latency: LatencyStats,
    latencyFormat: String,
    layoutSpec: MainLayoutSpec
) {
    val hasSamples = latency.count > 0
    val current = latency.current.takeIf { hasSamples }?.let { formatLatency(it, latencyFormat) } ?: "-- ms"
    val best = latency.best.takeIf { hasSamples }?.let { formatLatency(it, latencyFormat) } ?: "-- ms"
    val average = latency.average.takeIf { hasSamples }?.let { formatLatency(it, latencyFormat) } ?: "-- ms"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(layoutSpec.pingRowHeight),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.ping),
                color = ConnectionInfoColors.TextPrimary,
                fontSize = layoutSpec.pingLabelTextSize,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.latency),
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
                text = "${stringResource(R.string.best)} $best",
                color = ConnectionInfoColors.TextSecondary,
                fontSize = layoutSpec.pingMetaTextSize,
                lineHeight = layoutSpec.pingMetaLineHeight,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End
            )
            Text(
                text = "${stringResource(R.string.avg_short)} $average",
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
    speedFormat: String,
    speedGbpsFormat: String,
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
            text = stats.current.formattedSpeedOrEmpty(
                hasSamples = stats.count > 0,
                speedFormat = speedFormat,
                speedGbpsFormat = speedGbpsFormat
            ),
            layoutSpec = layoutSpec
        )
        SpeedValueText(
            text = stats.maximum.formattedSpeedOrEmpty(
                hasSamples = stats.count > 0,
                speedFormat = speedFormat,
                speedGbpsFormat = speedGbpsFormat
            ),
            layoutSpec = layoutSpec
        )
        SpeedValueText(
            text = stats.average.formattedSpeedOrEmpty(
                hasSamples = stats.count > 0,
                speedFormat = speedFormat,
                speedGbpsFormat = speedGbpsFormat
            ),
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
    val valueTextSize = if (text.length > LONG_SPEED_VALUE_LENGTH) {
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
private fun Float.formattedSpeedOrEmpty(
    hasSamples: Boolean,
    speedFormat: String,
    speedGbpsFormat: String
): String {
    return takeIf { hasSamples }?.let { formatSpeed(it, speedFormat, speedGbpsFormat) }.orEmpty()
}

/** Network information page showing local and public connection metadata. */
@Composable
private fun NetworkInfoScreen(
    info: NetworkDetails?,
    layoutSpec: MainLayoutSpec
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = layoutSpec.horizontalPadding,
                vertical = layoutSpec.networkVerticalPadding
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = ConnectionInfoColors.Surface,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, ConnectionInfoColors.SurfaceLine),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = layoutSpec.contentMaxWidth)
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = layoutSpec.networkCardHorizontalPadding,
                    vertical = layoutSpec.networkCardVerticalPadding
                )
            ) {
                NetworkInfoRow(stringResource(R.string.network), info?.transport?.displayName.orEmpty(), layoutSpec)
                NetworkInfoRow(stringResource(R.string.internal_ip), info?.internalIp.orEmpty(), layoutSpec)
                NetworkInfoRow(stringResource(R.string.external_ip), info?.externalIp.orEmpty(), layoutSpec)
                NetworkInfoRow(stringResource(R.string.isp), info?.lookup?.isp.orEmpty(), layoutSpec)
                NetworkInfoRow(stringResource(R.string.org), info?.lookup?.organization.orEmpty(), layoutSpec)
                NetworkInfoRow(stringResource(R.string.city), info?.lookup?.city.orEmpty(), layoutSpec)
                NetworkInfoRow(stringResource(R.string.region), info?.lookup?.region.orEmpty(), layoutSpec)
                NetworkInfoRow(stringResource(R.string.region_name), info?.lookup?.regionName.orEmpty(), layoutSpec)
                NetworkInfoRow(stringResource(R.string.country), info?.lookup?.country.orEmpty(), layoutSpec)
                NetworkInfoRow(stringResource(R.string.country_code), info?.lookup?.countryCode.orEmpty(), layoutSpec)
                NetworkInfoRow(stringResource(R.string.latitude), info?.lookup?.latitude.orEmpty(), layoutSpec)
                NetworkInfoRow(stringResource(R.string.longitude), info?.lookup?.longitude.orEmpty(), layoutSpec)
            }
        }
    }
}

/** Label/value row used in the Network Info card. */
@Composable
private fun NetworkInfoRow(
    label: String,
    value: String,
    layoutSpec: MainLayoutSpec
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = layoutSpec.networkRowVerticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = ConnectionInfoColors.TextSecondary,
            fontSize = layoutSpec.networkTextSize,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(layoutSpec.networkLabelWidth)
        )
        Text(
            text = value,
            color = ConnectionInfoColors.SpeedValue,
            fontSize = layoutSpec.networkTextSize,
            lineHeight = layoutSpec.networkValueLineHeight,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

/** Formats Mbps values with the localized string resource pattern. */
private fun formatSpeed(
    value: Float,
    speedFormat: String,
    speedGbpsFormat: String
): String {
    return if (value >= SPEED_GBPS_THRESHOLD_MBPS) {
        String.format(Locale.getDefault(), speedGbpsFormat, value / SPEED_GBPS_THRESHOLD_MBPS)
    } else {
        String.format(Locale.getDefault(), speedFormat, value)
    }
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
            versionName = "2.0.0",
            onAction = {}
        )
    }
}

/** Preview that exercises the compact phone layout used by narrower devices. */
@Preview(widthDp = 360, heightDp = 780)
@Composable
private fun CompactRunningSpeedPreview() {
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
            versionName = "2.0.0",
            onAction = {}
        )
    }
}

/** Preview that exercises long throughput values on a compact phone. */
@Preview(widthDp = 360, heightDp = 780)
@Composable
private fun CompactHighSpeedPreview() {
    ConnectionInfoTheme {
        ConnectionInfoApp(
            state = MainUiState(
                internetAvailable = true,
                speedTest = SpeedTestUiState(
                    running = true,
                    phase = SpeedTestPhase.DOWNLOAD,
                    gaugeValue = 500f,
                    progress = 0.82f,
                    ping = LatencyStats(current = 7f, best = 5f, total = 26f, count = 4),
                    download = SpeedRateStats(
                        current = 945.73f,
                        maximum = 1324.91f,
                        total = 4270.33f,
                        count = 4
                    ),
                    upload = SpeedRateStats(
                        current = 812.42f,
                        maximum = 1198.46f,
                        total = 3014.21f,
                        count = 4
                    )
                )
            ),
            versionName = "2.0.0",
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
            versionName = "2.0.0",
            onAction = {}
        )
    }
}

/** Preview that exercises the expanded layout cap used by tablets and large screens. */
@Preview(widthDp = 840, heightDp = 1180)
@Composable
private fun ExpandedNetworkInfoPreview() {
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
            versionName = "2.0.0",
            onAction = {}
        )
    }
}
