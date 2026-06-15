package cz.kaboom.connectioninfo.feature.main

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import cz.kaboom.connectioninfo.feature.main.components.AppFooter
import cz.kaboom.connectioninfo.feature.main.components.AppTabs
import cz.kaboom.connectioninfo.feature.main.components.SlidingTabContent
import cz.kaboom.connectioninfo.feature.main.network.NetworkInfoScreen
import cz.kaboom.connectioninfo.feature.main.speedtest.SpeedTestScreen
import cz.kaboom.connectioninfo.presentation.main.MainAction
import cz.kaboom.connectioninfo.presentation.main.MainTab
import cz.kaboom.connectioninfo.presentation.main.MainUiState
import cz.kaboom.connectioninfo.ui.theme.ConnectionInfoColors

private val TabSwipeThreshold = 72.dp

/**
 * Root composable for the application.
 *
 * It owns the tab chrome, swipe navigation, page transitions, and footer while delegating feature
 * content to focused child composables.
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
                SlidingTabContent(
                    selectedTab = selectedTab,
                    modifier = Modifier.fillMaxSize()
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

            AppFooter(
                versionName = versionName,
                layoutSpec = layoutSpec
            )
        }
    }
}
