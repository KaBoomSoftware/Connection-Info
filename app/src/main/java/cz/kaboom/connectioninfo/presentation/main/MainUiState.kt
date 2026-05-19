package cz.kaboom.connectioninfo.presentation.main

import cz.kaboom.connectioninfo.domain.model.NetworkDetails
import cz.kaboom.connectioninfo.domain.model.SpeedTestPhase
import kotlin.math.max
import kotlin.math.min

/**
 * Top-level destinations available from the main screen tabs.
 */
enum class MainTab {
    /** Speed test screen with ping, download, and upload metrics. */
    SPEED_TEST,

    /** Network details screen with local and public IP metadata. */
    NETWORK_INFO
}

/**
 * Immutable state consumed by the Compose main screen.
 */
data class MainUiState(
    /** Currently selected tab. */
    val selectedTab: MainTab = MainTab.SPEED_TEST,

    /** True when Android reports validated internet connectivity. */
    val internetAvailable: Boolean = false,

    /** Latest network information snapshot, or null before the first successful refresh. */
    val networkInfo: NetworkDetails? = null,

    /** Current speed test presentation state. */
    val speedTest: SpeedTestUiState = SpeedTestUiState(),

    /** Last user-facing error emitted by repositories. */
    val errorMessage: String? = null
)

/**
 * Presentation state for the Speed Test tab.
 */
data class SpeedTestUiState(
    /** True while a speed test flow is being collected. */
    val running: Boolean = false,

    /** Current phase shown in progress copy. */
    val phase: SpeedTestPhase = SpeedTestPhase.IDLE,

    /** Mbps value rendered in the gauge. */
    val gaugeValue: Float = 0f,

    /** Current phase progress in the range 0..1. */
    val progress: Float = 0f,

    /** Latency statistics collected during the ping phase. */
    val ping: LatencyStats = LatencyStats(),

    /** Download throughput statistics. */
    val download: SpeedRateStats = SpeedRateStats(),

    /** Upload throughput statistics. */
    val upload: SpeedRateStats = SpeedRateStats()
)

/**
 * Running aggregate for throughput samples measured in Mbps.
 */
data class SpeedRateStats(
    /** Most recent sample. */
    val current: Float = 0f,

    /** Highest sample observed so far. */
    val maximum: Float = 0f,

    /** Sum of samples used to derive [average]. */
    val total: Float = 0f,

    /** Number of samples included in [total]. */
    val count: Int = 0
) {
    /** Mean throughput across all collected samples. */
    val average: Float get() = if (count == 0) 0f else total / count

    /** Returns a new aggregate with [value] folded in. */
    fun add(value: Float) = copy(
        current = value,
        maximum = max(maximum, value),
        total = total + value,
        count = count + 1
    )
}

/**
 * Running aggregate for latency samples measured in milliseconds.
 */
data class LatencyStats(
    /** Most recent latency sample. */
    val current: Float = 0f,

    /** Lowest latency sample observed so far. */
    val best: Float = 0f,

    /** Sum of samples used to derive [average]. */
    val total: Float = 0f,

    /** Number of samples included in [total]. */
    val count: Int = 0
) {
    /** Mean latency across all collected samples. */
    val average: Float get() = if (count == 0) 0f else total / count

    /** Returns a new aggregate with [value] folded in. */
    fun add(value: Float) = copy(
        current = value,
        best = if (count == 0) value else min(best, value),
        total = total + value,
        count = count + 1
    )
}

/**
 * User and lifecycle actions accepted by [MainViewModel].
 */
sealed interface MainAction {
    /** Selects one of the top-level tabs. */
    data class SelectTab(val tab: MainTab) : MainAction

    /** Starts or cancels the speed test depending on the current state. */
    data object ToggleSpeedTest : MainAction

    /** Requests a manual network info refresh. */
    data object RefreshNetworkInfo : MainAction
}
