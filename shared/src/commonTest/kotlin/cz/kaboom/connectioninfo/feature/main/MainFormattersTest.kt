package cz.kaboom.connectioninfo.feature.main

import kotlin.test.Test
import kotlin.test.assertEquals

class MainFormattersTest {

    // ─── formatDecimal ───────────────────────────────────────────────────────────

    @Test
    fun formatDecimalZeroDecimalsReturnsWholeNumber() {
        assertEquals("0", formatDecimal(0f, 0))
        assertEquals("1", formatDecimal(1f, 0))
        assertEquals("42", formatDecimal(42f, 0))
        assertEquals("1000", formatDecimal(1000f, 0))
    }

    @Test
    fun formatDecimalOneDecimalPadsFraction() {
        assertEquals("1.0", formatDecimal(1f, 1))
        assertEquals("1.5", formatDecimal(1.5f, 1))
        assertEquals("9.9", formatDecimal(9.9f, 1))
    }

    @Test
    fun formatDecimalTwoDecimalsPadsFraction() {
        assertEquals("0.00", formatDecimal(0f, 2))
        assertEquals("1.00", formatDecimal(1f, 2))
        assertEquals("0.05", formatDecimal(0.05f, 2))
        assertEquals("3.14", formatDecimal(3.14f, 2))
        assertEquals("999.99", formatDecimal(999.99f, 2))
    }

    @Test
    fun formatDecimalRoundsHalfUp() {
        assertEquals("124", formatDecimal(123.5f, 0))
        assertEquals("1.6", formatDecimal(1.55f, 1))
    }

    // ─── formatSpeed ─────────────────────────────────────────────────────────────

    @Test
    fun formatSpeedBelowThresholdUsesMbps() {
        assertEquals("0.00 Mbps", formatSpeed(0f))
        assertEquals("1.00 Mbps", formatSpeed(1f))
        assertEquals("100.00 Mbps", formatSpeed(100f))
        assertEquals("999.99 Mbps", formatSpeed(999.99f))
    }

    @Test
    fun formatSpeedAtExactThresholdUsesGbps() {
        assertEquals("1.00 Gbps", formatSpeed(1000f))
    }

    @Test
    fun formatSpeedAboveThresholdUsesGbps() {
        assertEquals("1.50 Gbps", formatSpeed(1500f))
        assertEquals("10.00 Gbps", formatSpeed(10000f))
    }

    // ─── formatLatency ───────────────────────────────────────────────────────────

    @Test
    fun formatLatencyShowsWholeMilliseconds() {
        assertEquals("0 ms", formatLatency(0f))
        assertEquals("18 ms", formatLatency(18f))
        assertEquals("123 ms", formatLatency(123f))
    }

    @Test
    fun formatLatencyRoundsToNearestInteger() {
        assertEquals("123 ms", formatLatency(123.4f))
        assertEquals("124 ms", formatLatency(123.6f))
    }

    // ─── formatSpeedNumber ───────────────────────────────────────────────────────

    @Test
    fun formatSpeedNumberShowsTwoDecimals() {
        assertEquals("0.00", formatSpeedNumber(0f))
        assertEquals("250.00", formatSpeedNumber(250f))
        assertEquals("3.14", formatSpeedNumber(3.14159f))
    }

    // ─── formatPlain ─────────────────────────────────────────────────────────────

    @Test
    fun formatPlainSubstitutesPlaceholder() {
        assertEquals("Speed: 42 Mbps", "Speed: %s Mbps".formatPlain("42"))
        assertEquals("Error: none", "Error: %s".formatPlain("none"))
    }

    @Test
    fun formatPlainReplacesAllOccurrences() {
        // replace() substitutes every %s in the template
        assertEquals("a-b-b", "a-%s-%s".formatPlain("b"))
    }

    @Test
    fun formatPlainWithNoPlaceholderLeavesStringUnchanged() {
        assertEquals("no placeholder", "no placeholder".formatPlain("ignored"))
    }
}
