package cz.kaboom.connectioninfo.presentation.main

import cz.kaboom.connectioninfo.domain.model.speedtest.SpeedTestPhase

/** Presentation state for the Speed Test tab. */
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
