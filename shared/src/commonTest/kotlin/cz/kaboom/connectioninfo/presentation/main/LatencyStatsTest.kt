package cz.kaboom.connectioninfo.presentation.main

import kotlin.test.Test
import kotlin.test.assertEquals

class LatencyStatsTest {

    @Test
    fun emptyStatsHaveZeroValues() {
        val stats = LatencyStats()
        assertEquals(0f, stats.current)
        assertEquals(0f, stats.best)
        assertEquals(0f, stats.total)
        assertEquals(0, stats.count)
        assertEquals(0f, stats.average)
    }

    @Test
    fun firstSampleSetsCurrentBestAndTotal() {
        val stats = LatencyStats().add(42f)
        assertEquals(42f, stats.current)
        assertEquals(42f, stats.best)
        assertEquals(42f, stats.total)
        assertEquals(1, stats.count)
        assertEquals(42f, stats.average)
    }

    @Test
    fun bestTracksMinimumAcrossSamples() {
        val stats = LatencyStats().add(30f).add(10f).add(25f)
        assertEquals(10f, stats.best)
    }

    @Test
    fun bestIsSetFromFirstSampleRegardlessOfMagnitude() {
        val stats = LatencyStats().add(999f)
        assertEquals(999f, stats.best)
    }

    @Test
    fun subsequentHigherSampleDoesNotUpdateBest() {
        val stats = LatencyStats().add(10f).add(50f)
        assertEquals(10f, stats.best)
        assertEquals(50f, stats.current)
    }

    @Test
    fun currentAlwaysReflectsLastSample() {
        val stats = LatencyStats().add(30f).add(10f).add(25f)
        assertEquals(25f, stats.current)
    }

    @Test
    fun averageIsMeanOfAllSamples() {
        val stats = LatencyStats().add(10f).add(20f).add(30f)
        assertEquals(20f, stats.average)
        assertEquals(60f, stats.total)
        assertEquals(3, stats.count)
    }

    @Test
    fun averageIsZeroWhenNoSamplesAdded() {
        assertEquals(0f, LatencyStats().average)
    }

    @Test
    fun countIncrementsWithEachSample() {
        var stats = LatencyStats()
        repeat(5) { stats = stats.add(it.toFloat() * 10f) }
        assertEquals(5, stats.count)
    }

    @Test
    fun addReturnsNewInstanceLeavingOriginalUnchanged() {
        val original = LatencyStats()
        val updated = original.add(50f)
        assertEquals(0, original.count)
        assertEquals(1, updated.count)
    }
}
