package cz.kaboom.connectioninfo.presentation.main

import kotlin.test.Test
import kotlin.test.assertEquals

class SpeedRateStatsTest {

    @Test
    fun emptyStatsHaveZeroValues() {
        val stats = SpeedRateStats()
        assertEquals(0f, stats.current)
        assertEquals(0f, stats.maximum)
        assertEquals(0f, stats.total)
        assertEquals(0, stats.count)
        assertEquals(0f, stats.average)
    }

    @Test
    fun firstSampleSetsCurrentMaximumAndTotal() {
        val stats = SpeedRateStats().add(100f)
        assertEquals(100f, stats.current)
        assertEquals(100f, stats.maximum)
        assertEquals(100f, stats.total)
        assertEquals(1, stats.count)
        assertEquals(100f, stats.average)
    }

    @Test
    fun maximumTracksHighestSample() {
        val stats = SpeedRateStats().add(100f).add(300f).add(200f)
        assertEquals(300f, stats.maximum)
    }

    @Test
    fun maximumIsSetFromFirstSampleRegardlessOfMagnitude() {
        val stats = SpeedRateStats().add(0.01f)
        assertEquals(0.01f, stats.maximum)
    }

    @Test
    fun subsequentLowerSampleDoesNotUpdateMaximum() {
        val stats = SpeedRateStats().add(500f).add(100f)
        assertEquals(500f, stats.maximum)
        assertEquals(100f, stats.current)
    }

    @Test
    fun currentAlwaysReflectsLastSample() {
        val stats = SpeedRateStats().add(100f).add(300f).add(200f)
        assertEquals(200f, stats.current)
    }

    @Test
    fun averageIsMeanOfAllSamples() {
        val stats = SpeedRateStats().add(100f).add(200f).add(300f)
        assertEquals(200f, stats.average)
        assertEquals(600f, stats.total)
        assertEquals(3, stats.count)
    }

    @Test
    fun averageIsZeroWhenNoSamplesAdded() {
        assertEquals(0f, SpeedRateStats().average)
    }

    @Test
    fun countIncrementsWithEachSample() {
        var stats = SpeedRateStats()
        repeat(7) { stats = stats.add(it.toFloat() * 50f) }
        assertEquals(7, stats.count)
    }

    @Test
    fun addReturnsNewInstanceLeavingOriginalUnchanged() {
        val original = SpeedRateStats()
        val updated = original.add(150f)
        assertEquals(0, original.count)
        assertEquals(1, updated.count)
    }
}
