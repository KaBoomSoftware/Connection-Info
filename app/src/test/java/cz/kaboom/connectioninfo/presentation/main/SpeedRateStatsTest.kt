package cz.kaboom.connectioninfo.presentation.main

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit coverage for presentation-level metric aggregators.
 */
class SpeedRateStatsTest {
    /** Ensures throughput samples update latest, maximum, and average values. */
    @Test
    fun `add keeps current maximum and average`() {
        val stats = SpeedRateStats()
            .add(12f)
            .add(48f)
            .add(30f)

        assertEquals(30f, stats.current, 0.001f)
        assertEquals(48f, stats.maximum, 0.001f)
        assertEquals(30f, stats.average, 0.001f)
        assertEquals(3, stats.count)
    }

    /** Ensures latency samples update latest, best, and average values. */
    @Test
    fun `latency add keeps current best and average`() {
        val stats = LatencyStats()
            .add(24f)
            .add(18f)
            .add(30f)

        assertEquals(30f, stats.current, 0.001f)
        assertEquals(18f, stats.best, 0.001f)
        assertEquals(24f, stats.average, 0.001f)
        assertEquals(3, stats.count)
    }
}
