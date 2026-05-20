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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cz.kaboom.connectioninfo.domain.model.NetworkDetails
import cz.kaboom.connectioninfo.domain.model.SpeedTestPhase
import cz.kaboom.connectioninfo.presentation.main.MainAction
import cz.kaboom.connectioninfo.presentation.main.MainTab
import cz.kaboom.connectioninfo.presentation.main.MainUiState
import cz.kaboom.connectioninfo.presentation.main.LatencyStats
import cz.kaboom.connectioninfo.presentation.main.SpeedRateStats
import cz.kaboom.connectioninfo.presentation.main.SpeedTestUiState
import cz.kaboom.connectioninfo.ui.theme.ConnectionInfoColors
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

/** Minimum horizontal drag distance required to switch tabs. */
private val TabSwipeThreshold = 72.dp

/** Values at or above this Mbps threshold are shown as Gbps in table cells. */
private const val SPEED_GBPS_THRESHOLD_MBPS = 1_000f

/** Long table values use a smaller font to stay on one line on compact screens. */
private const val LONG_SPEED_VALUE_LENGTH = 9

/** Shared English UI copy used by Android and iOS to keep both apps visually identical. */
private object UiText {
    const val speedTest = "Speed Test"
    const val networkInfo = "Network Info"
    const val version = "Version %s"
    const val copyright = "Copyright (C) KaBoom"
    const val speed = "Speed"
    const val stopTest = "Stop Test"
    const val startTest = "Start Test"
    const val networkUnavailable = "Network unavailable"
    const val loading = "Loading..."
    const val notAvailable = "--"
    const val status = "Status"
    const val last = "Last"
    const val max = "Max"
    const val avg = "Avg"
    const val avgShort = "Avg:"
    const val best = "Best:"
    const val download = "Download"
    const val upload = "Upload"
    const val ping = "Ping"
    const val latency = "Latency"
    const val network = "Network"
    const val internalIp = "Internal IP"
    const val externalIp = "External IP"
    const val isp = "ISP"
    const val org = "Org"
    const val city = "City"
    const val region = "Region"
    const val regionName = "Region Name"
    const val country = "Country"
    const val countryCode = "Country Code"
    const val latitude = "Latitude"
    const val longitude = "Longitude"
}

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
    val tinyPhone = maxWidth <= 375.dp || maxHeight <= 700.dp
    val compact = maxWidth < 390.dp || maxHeight < 780.dp
    val expanded = maxWidth >= 700.dp || maxHeight >= 900.dp

    return when {
        tinyPhone -> MainLayoutSpec(
            contentMaxWidth = 390.dp,
            horizontalPadding = 12.dp,
            speedVerticalPadding = 28.dp,
            networkVerticalPadding = 8.dp,
            tabHeight = 54.dp,
            tabItemHeight = 48.dp,
            tabFontSize = 12.sp,
            tabLetterSpacing = 0.sp,
            tabIndicatorWidth = 82.dp,
            tabIndicatorTopSpacing = 7.dp,
            footerVerticalPadding = 4.dp,
            versionTextSize = 11.sp,
            copyrightTextSize = 9.sp,
            gaugeHeight = 156.dp,
            gaugeFillFraction = 0.58f,
            gaugeStrokeWidth = 20.dp,
            gaugeValueLargeTextSize = 34.sp,
            gaugeValueSmallTextSize = 30.sp,
            gaugeUnitTextSize = 12.sp,
            sectionTitleTextSize = 15.sp,
            statsHorizontalPadding = 10.dp,
            statsVerticalPadding = 8.dp,
            statsDividerVerticalPadding = 4.dp,
            statsLabelWidth = 72.dp,
            statsRowHeight = 30.dp,
            pingRowHeight = 44.dp,
            pingLabelTextSize = 13.sp,
            pingSubtitleTextSize = 9.sp,
            pingCurrentTextSize = 16.sp,
            pingMetaTextSize = 8.sp,
            pingMetaLineHeight = 10.sp,
            statsHeaderTextSize = 10.sp,
            statsLabelTextSize = 12.sp,
            statsValueTextSize = 9.sp,
            statsValueCompactTextSize = 7.sp,
            buttonHeight = 46.dp,
            actionTopPadding = 10.dp,
            actionTextSize = 12.sp,
            progressTopPadding = 8.dp,
            networkLabelWidth = 96.dp,
            networkCardHorizontalPadding = 10.dp,
            networkCardVerticalPadding = 10.dp,
            networkRowVerticalPadding = 3.dp,
            networkTextSize = 11.sp,
            networkValueLineHeight = 14.sp
        )

        compact -> MainLayoutSpec(
            contentMaxWidth = 430.dp,
            horizontalPadding = 14.dp,
            speedVerticalPadding = 24.dp,
            networkVerticalPadding = 12.dp,
            tabHeight = 60.dp,
            tabItemHeight = 52.dp,
            tabFontSize = 13.sp,
            tabLetterSpacing = 0.sp,
            tabIndicatorWidth = 88.dp,
            tabIndicatorTopSpacing = 8.dp,
            footerVerticalPadding = 6.dp,
            versionTextSize = 12.sp,
            copyrightTextSize = 10.sp,
            gaugeHeight = 176.dp,
            gaugeFillFraction = 0.60f,
            gaugeStrokeWidth = 24.dp,
            gaugeValueLargeTextSize = 38.sp,
            gaugeValueSmallTextSize = 34.sp,
            gaugeUnitTextSize = 13.sp,
            sectionTitleTextSize = 16.sp,
            statsHorizontalPadding = 12.dp,
            statsVerticalPadding = 9.dp,
            statsDividerVerticalPadding = 5.dp,
            statsLabelWidth = 78.dp,
            statsRowHeight = 32.dp,
            pingRowHeight = 44.dp,
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
            contentMaxWidth = 760.dp,
            horizontalPadding = 44.dp,
            speedVerticalPadding = 34.dp,
            networkVerticalPadding = 44.dp,
            tabHeight = 104.dp,
            tabItemHeight = 84.dp,
            tabFontSize = 18.sp,
            tabLetterSpacing = 0.sp,
            tabIndicatorWidth = 138.dp,
            tabIndicatorTopSpacing = 20.dp,
            footerVerticalPadding = 16.dp,
            versionTextSize = 15.sp,
            copyrightTextSize = 13.sp,
            gaugeHeight = 360.dp,
            gaugeFillFraction = 0.68f,
            gaugeStrokeWidth = 42.dp,
            gaugeValueLargeTextSize = 60.sp,
            gaugeValueSmallTextSize = 52.sp,
            gaugeUnitTextSize = 18.sp,
            sectionTitleTextSize = 23.sp,
            statsHorizontalPadding = 24.dp,
            statsVerticalPadding = 18.dp,
            statsDividerVerticalPadding = 12.dp,
            statsLabelWidth = 118.dp,
            statsRowHeight = 48.dp,
            pingRowHeight = 56.dp,
            pingLabelTextSize = 19.sp,
            pingSubtitleTextSize = 13.sp,
            pingCurrentTextSize = 24.sp,
            pingMetaTextSize = 12.sp,
            pingMetaLineHeight = 15.sp,
            statsHeaderTextSize = 14.sp,
            statsLabelTextSize = 18.sp,
            statsValueTextSize = 13.sp,
            statsValueCompactTextSize = 11.sp,
            buttonHeight = 72.dp,
            actionTopPadding = 28.dp,
            actionTextSize = 16.sp,
            progressTopPadding = 18.dp,
            networkLabelWidth = 160.dp,
            networkCardHorizontalPadding = 24.dp,
            networkCardVerticalPadding = 22.dp,
            networkRowVerticalPadding = 8.dp,
            networkTextSize = 15.sp,
            networkValueLineHeight = 21.sp
        )

        else -> MainLayoutSpec(
            contentMaxWidth = 520.dp,
            horizontalPadding = 20.dp,
            speedVerticalPadding = 12.dp,
            networkVerticalPadding = 22.dp,
            tabHeight = 76.dp,
            tabItemHeight = 64.dp,
            tabFontSize = 15.sp,
            tabLetterSpacing = 0.sp,
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
                        internetAvailable = state.internetAvailable,
                        errorMessage = state.errorMessage,
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
                text = UiText.version.formatPlain(versionName),
                color = ConnectionInfoColors.TextMuted,
                fontSize = layoutSpec.versionTextSize,
                textAlign = TextAlign.Center
            )
            Text(
                text = UiText.copyright,
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
            text = UiText.speedTest.uppercase(),
            selected = selectedTab == MainTab.SPEED_TEST,
            onClick = { onTabSelected(MainTab.SPEED_TEST) },
            modifier = Modifier.weight(1f),
            layoutSpec = layoutSpec
        )
        AppTabItem(
            text = UiText.networkInfo.uppercase(),
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
    val progressText = when (state.phase) {
        SpeedTestPhase.PING -> "Ping: ${formatDecimal(state.progress * 100f, 0)}%"
        SpeedTestPhase.DOWNLOAD -> "DL: ${formatDecimal(state.progress * 100f, 0)}%"
        SpeedTestPhase.UPLOAD -> "UL: ${formatDecimal(state.progress * 100f, 0)}%"
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
                text = UiText.speed,
                color = ConnectionInfoColors.TextPrimary,
                fontSize = layoutSpec.sectionTitleTextSize,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 10.dp)
            )

            SpeedStatsPanel(
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
                        text = (if (state.running) UiText.stopTest else UiText.startTest).uppercase(),
                        fontSize = layoutSpec.actionTextSize,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.sp
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
                        text = UiText.networkUnavailable.uppercase(),
                        color = ConnectionInfoColors.Error,
                        fontSize = layoutSpec.actionTextSize,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.sp,
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
            text = stats.current.formattedSpeedOrEmpty(
                hasSamples = stats.count > 0
            ),
            layoutSpec = layoutSpec
        )
        SpeedValueText(
            text = stats.maximum.formattedSpeedOrEmpty(
                hasSamples = stats.count > 0
            ),
            layoutSpec = layoutSpec
        )
        SpeedValueText(
            text = stats.average.formattedSpeedOrEmpty(
                hasSamples = stats.count > 0
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
    hasSamples: Boolean
): String {
    return takeIf { hasSamples }?.let { formatSpeed(it) }.orEmpty()
}

/** Network information page showing local and public connection metadata. */
@Composable
private fun NetworkInfoScreen(
    info: NetworkDetails?,
    internetAvailable: Boolean,
    errorMessage: String?,
    layoutSpec: MainLayoutSpec
) {
    val statusMessage = when {
        !internetAvailable -> UiText.networkUnavailable
        info == null && errorMessage != null -> errorMessage
        else -> null
    }
    val isPending = internetAvailable && info == null && errorMessage == null

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
                statusMessage?.let {
                    NetworkInfoRow(UiText.status, it, layoutSpec, isError = true)
                }
                NetworkInfoRow(UiText.network, info?.transport?.displayName.valueOrPlaceholder(isPending), layoutSpec)
                NetworkInfoRow(UiText.internalIp, info?.internalIp.valueOrPlaceholder(isPending), layoutSpec)
                NetworkInfoRow(UiText.externalIp, info?.externalIp.valueOrPlaceholder(isPending), layoutSpec)
                NetworkInfoRow(UiText.isp, info?.lookup?.isp.valueOrPlaceholder(isPending), layoutSpec)
                NetworkInfoRow(UiText.org, info?.lookup?.organization.valueOrPlaceholder(isPending), layoutSpec)
                NetworkInfoRow(UiText.city, info?.lookup?.city.valueOrPlaceholder(isPending), layoutSpec)
                NetworkInfoRow(UiText.region, info?.lookup?.region.valueOrPlaceholder(isPending), layoutSpec)
                NetworkInfoRow(UiText.regionName, info?.lookup?.regionName.valueOrPlaceholder(isPending), layoutSpec)
                NetworkInfoRow(UiText.country, info?.lookup?.country.valueOrPlaceholder(isPending), layoutSpec)
                NetworkInfoRow(UiText.countryCode, info?.lookup?.countryCode.valueOrPlaceholder(isPending), layoutSpec)
                NetworkInfoRow(UiText.latitude, info?.lookup?.latitude.valueOrPlaceholder(isPending), layoutSpec)
                NetworkInfoRow(UiText.longitude, info?.lookup?.longitude.valueOrPlaceholder(isPending), layoutSpec)
            }
        }
    }
}

/** Keeps Network Info readable while the first lookup is loading or partial data is unavailable. */
private fun String?.valueOrPlaceholder(isPending: Boolean): String {
    return when {
        isPending -> UiText.loading
        isNullOrBlank() -> UiText.notAvailable
        else -> this
    }
}

/** Label/value row used in the Network Info card. */
@Composable
private fun NetworkInfoRow(
    label: String,
    value: String,
    layoutSpec: MainLayoutSpec,
    isError: Boolean = false
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
            color = if (isError) ConnectionInfoColors.Error else ConnectionInfoColors.SpeedValue,
            fontSize = layoutSpec.networkTextSize,
            lineHeight = layoutSpec.networkValueLineHeight,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

/** Formats Mbps values with fixed two-decimal output on every platform. */
private fun formatSpeed(value: Float): String {
    return if (value >= SPEED_GBPS_THRESHOLD_MBPS) {
        "${formatDecimal(value / SPEED_GBPS_THRESHOLD_MBPS, 2)} Gbps"
    } else {
        "${formatDecimal(value, 2)} Mbps"
    }
}

/** Formats latency values with fixed whole-number output on every platform. */
private fun formatLatency(value: Float): String = "${formatDecimal(value, 0)} ms"

/** Formats the central gauge value without units. */
private fun formatSpeedNumber(value: Float): String = formatDecimal(value, 2)

/** Small `%s` replacement helper kept common-source friendly. */
private fun String.formatPlain(value: String): String = replace("%s", value)

/** Decimal formatter that avoids JVM-only `String.format`. */
private fun formatDecimal(value: Float, decimals: Int): String {
    val sign = if (value < 0f) "-" else ""
    val multiplier = tenPow(decimals)
    val scaled = (abs(value) * multiplier).roundToInt()
    val whole = scaled / multiplier
    val fraction = scaled % multiplier

    return if (decimals == 0) {
        "$sign$whole"
    } else {
        "$sign$whole.${fraction.toString().padStart(decimals, '0')}"
    }
}

private fun tenPow(exponent: Int): Int {
    var value = 1
    repeat(exponent) {
        value *= 10
    }
    return value
}
