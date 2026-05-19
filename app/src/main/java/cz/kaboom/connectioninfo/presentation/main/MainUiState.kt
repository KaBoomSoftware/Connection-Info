package cz.kaboom.connectioninfo.presentation.main

import cz.kaboom.connectioninfo.domain.model.NetworkDetails
import cz.kaboom.connectioninfo.domain.model.SpeedTestPhase
import kotlin.math.max
import kotlin.math.min

enum class MainTab {
    SPEED_TEST,
    NETWORK_INFO
}

data class MainUiState(
    val selectedTab: MainTab = MainTab.SPEED_TEST,
    val internetAvailable: Boolean = false,
    val networkInfo: NetworkDetails? = null,
    val speedTest: SpeedTestUiState = SpeedTestUiState(),
    val errorMessage: String? = null
)

data class SpeedTestUiState(
    val running: Boolean = false,
    val phase: SpeedTestPhase = SpeedTestPhase.IDLE,
    val gaugeValue: Float = 0f,
    val progress: Float = 0f,
    val ping: LatencyStats = LatencyStats(),
    val download: SpeedRateStats = SpeedRateStats(),
    val upload: SpeedRateStats = SpeedRateStats()
)

data class SpeedRateStats(
    val current: Float = 0f,
    val maximum: Float = 0f,
    val total: Float = 0f,
    val count: Int = 0
) {
    val average: Float get() = if (count == 0) 0f else total / count

    fun add(value: Float) = copy(
        current = value,
        maximum = max(maximum, value),
        total = total + value,
        count = count + 1
    )
}

data class LatencyStats(
    val current: Float = 0f,
    val best: Float = 0f,
    val total: Float = 0f,
    val count: Int = 0
) {
    val average: Float get() = if (count == 0) 0f else total / count

    fun add(value: Float) = copy(
        current = value,
        best = if (count == 0) value else min(best, value),
        total = total + value,
        count = count + 1
    )
}

sealed interface MainAction {
    data class SelectTab(val tab: MainTab) : MainAction
    data object ToggleSpeedTest : MainAction
    data object RefreshNetworkInfo : MainAction
}
