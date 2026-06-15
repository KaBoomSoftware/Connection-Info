package cz.kaboom.connectioninfo.presentation.main

import kotlin.math.min

/** Running aggregate for latency samples measured in milliseconds. */
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
