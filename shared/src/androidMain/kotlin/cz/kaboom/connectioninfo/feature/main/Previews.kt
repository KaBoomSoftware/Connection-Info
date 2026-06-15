package cz.kaboom.connectioninfo.feature.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import cz.kaboom.connectioninfo.domain.model.network.NetworkDetails
import cz.kaboom.connectioninfo.domain.model.network.NetworkLookup
import cz.kaboom.connectioninfo.domain.model.network.NetworkTransport
import cz.kaboom.connectioninfo.domain.model.speedtest.SpeedTestPhase
import cz.kaboom.connectioninfo.feature.main.network.NetworkInfoScreen
import cz.kaboom.connectioninfo.feature.main.speedtest.SpeedTestScreen
import cz.kaboom.connectioninfo.presentation.main.LatencyStats
import cz.kaboom.connectioninfo.presentation.main.MainTab
import cz.kaboom.connectioninfo.presentation.main.MainUiState
import cz.kaboom.connectioninfo.presentation.main.SpeedRateStats
import cz.kaboom.connectioninfo.presentation.main.SpeedTestUiState
import cz.kaboom.connectioninfo.ui.theme.ConnectionInfoTheme

private val previewLayoutSpec = mainLayoutSpec(390.dp(), 844.dp())

@Preview
@Composable
private fun AppIdlePreview() {
    ConnectionInfoTheme {
        ConnectionInfoApp(
            state = MainUiState(
                selectedTab = MainTab.SPEED_TEST,
                internetAvailable = true
            ),
            versionName = "2.0.0",
            onAction = {}
        )
    }
}

@Preview
@Composable
private fun AppRunningPreview() {
    ConnectionInfoTheme {
        ConnectionInfoApp(
            state = MainUiState(
                selectedTab = MainTab.SPEED_TEST,
                internetAvailable = true,
                speedTest = SpeedTestUiState(
                    running = true,
                    phase = SpeedTestPhase.DOWNLOAD,
                    gaugeValue = 187.4f,
                    progress = 0.6f,
                    ping = LatencyStats(count = 5, current = 18.0f, best = 15.0f),
                    download = SpeedRateStats(count = 10, current = 187.4f, maximum = 210.0f)
                )
            ),
            versionName = "2.0.0",
            onAction = {}
        )
    }
}

@Preview
@Composable
private fun AppOfflinePreview() {
    ConnectionInfoTheme {
        ConnectionInfoApp(
            state = MainUiState(
                selectedTab = MainTab.SPEED_TEST,
                internetAvailable = false
            ),
            versionName = "2.0.0",
            onAction = {}
        )
    }
}

@Preview
@Composable
private fun NetworkInfoPopulatedPreview() {
    ConnectionInfoTheme {
        NetworkInfoScreen(
            info = NetworkDetails(
                transport = NetworkTransport.WIFI,
                internalIp = "192.168.1.42",
                externalIp = "203.0.113.55",
                lookup = NetworkLookup(
                    isp = "Example ISP s.r.o.",
                    organization = "AS12345 Example",
                    city = "Prague",
                    region = "PR",
                    regionName = "Prague",
                    country = "Czech Republic",
                    countryCode = "CZ",
                    latitude = "50.0755",
                    longitude = "14.4378"
                )
            ),
            internetAvailable = true,
            errorMessage = null,
            layoutSpec = previewLayoutSpec
        )
    }
}

@Preview
@Composable
private fun NetworkInfoLoadingPreview() {
    ConnectionInfoTheme {
        NetworkInfoScreen(
            info = null,
            internetAvailable = true,
            errorMessage = null,
            layoutSpec = previewLayoutSpec
        )
    }
}

@Preview
@Composable
private fun SpeedTestIdlePreview() {
    ConnectionInfoTheme {
        SpeedTestScreen(
            state = SpeedTestUiState(),
            internetAvailable = true,
            onToggleTest = {},
            layoutSpec = previewLayoutSpec
        )
    }
}

@Preview
@Composable
private fun SpeedTestDownloadPreview() {
    ConnectionInfoTheme {
        SpeedTestScreen(
            state = SpeedTestUiState(
                running = true,
                phase = SpeedTestPhase.DOWNLOAD,
                gaugeValue = 342.5f,
                progress = 0.45f,
                ping = LatencyStats(count = 5, current = 12.0f, best = 10.0f, total = 57.5f),
                download = SpeedRateStats(count = 8, current = 342.5f, maximum = 380.0f, total = 2640.0f)
            ),
            internetAvailable = true,
            onToggleTest = {},
            layoutSpec = previewLayoutSpec
        )
    }
}

private fun Int.dp(): androidx.compose.ui.unit.Dp = androidx.compose.ui.unit.Dp(this.toFloat())
