package cz.kaboom.connectioninfo.presentation.main

import kotlin.math.max

/** Running aggregate for throughput samples measured in Mbps. */
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
