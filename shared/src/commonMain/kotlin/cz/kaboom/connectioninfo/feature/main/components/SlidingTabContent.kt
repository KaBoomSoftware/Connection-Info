package cz.kaboom.connectioninfo.feature.main.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import cz.kaboom.connectioninfo.presentation.main.MainTab
import kotlin.math.roundToInt

private const val TabTransitionDurationMillis = 360

/**
 * Slide transition that keeps working when Android's global animation duration scale is 0.
 */
@Composable
internal fun SlidingTabContent(
    selectedTab: MainTab,
    modifier: Modifier = Modifier,
    content: @Composable (MainTab) -> Unit
) {
    var visibleTab by remember { mutableStateOf(selectedTab) }
    var outgoingTab by remember { mutableStateOf<MainTab?>(null) }
    var direction by remember { mutableStateOf(0) }
    var progress by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(selectedTab) {
        if (selectedTab == visibleTab && outgoingTab == null) {
            progress = 1f
            return@LaunchedEffect
        }

        val previousTab = visibleTab
        direction = if (selectedTab.index > previousTab.index) 1 else -1
        outgoingTab = previousTab
        visibleTab = selectedTab
        progress = 0f

        val durationNanos = TabTransitionDurationMillis * 1_000_000L
        val startedAt = withFrameNanos { it }
        var rawProgress: Float

        do {
            val now = withFrameNanos { it }
            rawProgress = ((now - startedAt).toFloat() / durationNanos).coerceIn(0f, 1f)
            progress = FastOutSlowInEasing.transform(rawProgress)
        } while (rawProgress < 1f)

        progress = 1f
        outgoingTab = null
    }

    BoxWithConstraints(modifier = modifier.clipToBounds()) {
        val widthPx = constraints.maxWidth
        val activeDirection = direction.takeIf { outgoingTab != null } ?: 0
        val enteringOffset = ((1f - progress) * widthPx * activeDirection).roundToInt()
        val exitingOffset = (-progress * widthPx * activeDirection).roundToInt()

        outgoingTab?.let { tab ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset(exitingOffset, 0) }
                    .zIndex(0f)
            ) {
                content(tab)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(enteringOffset, 0) }
                .zIndex(1f)
        ) {
            content(visibleTab)
        }
    }
}

/** Ordering helper used to choose the animated page transition direction. */
private val MainTab.index: Int
    get() = when (this) {
        MainTab.SPEED_TEST -> 0
        MainTab.NETWORK_INFO -> 1
    }
