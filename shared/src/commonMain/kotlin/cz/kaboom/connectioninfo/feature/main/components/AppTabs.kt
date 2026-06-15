package cz.kaboom.connectioninfo.feature.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cz.kaboom.connectioninfo.feature.main.MainLayoutSpec
import cz.kaboom.connectioninfo.feature.main.UiText
import cz.kaboom.connectioninfo.presentation.main.MainTab
import cz.kaboom.connectioninfo.ui.theme.ConnectionInfoColors

/** Top tab row for switching between speed test and network information pages. */
@Composable
internal fun AppTabs(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    layoutSpec: MainLayoutSpec
) {
    val tabs = listOf(MainTab.SPEED_TEST, MainTab.NETWORK_INFO)
    val selectedIndex = tabs.indexOf(selectedTab).coerceAtLeast(0)

    SecondaryTabRow(
        selectedTabIndex = selectedIndex,
        containerColor = Color.Transparent,
        contentColor = ConnectionInfoColors.MaterialPurple,
        divider = {},
        indicator = {
            Box(
                modifier = Modifier
                    .tabIndicatorOffset(selectedIndex, matchContentSize = false)
                    .fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .padding(bottom = layoutSpec.tabIndicatorTopSpacing)
                        .width(layoutSpec.tabIndicatorWidth)
                        .height(3.dp)
                        .background(ConnectionInfoColors.MaterialPurple)
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(layoutSpec.tabHeight)
    ) {
        tabs.forEach { tab ->
            val selected = tab == selectedTab
            Tab(
                selected = selected,
                onClick = { onTabSelected(tab) },
                selectedContentColor = ConnectionInfoColors.MaterialPurple,
                unselectedContentColor = ConnectionInfoColors.TextSecondary,
                modifier = Modifier.height(layoutSpec.tabItemHeight),
                text = {
                    Text(
                        text = tab.title.uppercase(),
                        fontSize = layoutSpec.tabFontSize,
                        letterSpacing = layoutSpec.tabLetterSpacing,
                        fontWeight = FontWeight.Medium
                    )
                }
            )
        }
    }
}

private val MainTab.title: String
    get() = when (this) {
        MainTab.SPEED_TEST -> UiText.speedTest
        MainTab.NETWORK_INFO -> UiText.networkInfo
    }
