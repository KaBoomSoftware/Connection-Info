package cz.kaboom.connectioninfo.feature.main

import kotlin.math.abs
import kotlin.math.roundToInt

private const val SpeedGbpsThresholdMbps = 1_000f

/** Formats Mbps values with fixed two-decimal output on every platform. */
internal fun formatSpeed(value: Float): String {
    return if (value >= SpeedGbpsThresholdMbps) {
        "${formatDecimal(value / SpeedGbpsThresholdMbps, 2)} Gbps"
    } else {
        "${formatDecimal(value, 2)} Mbps"
    }
}

/** Formats latency values with fixed whole-number output on every platform. */
internal fun formatLatency(value: Float): String = "${formatDecimal(value, 0)} ms"

/** Formats the central gauge value without units. */
internal fun formatSpeedNumber(value: Float): String = formatDecimal(value, 2)

/** Small `%s` replacement helper kept common-source friendly. */
internal fun String.formatPlain(value: String): String = replace("%s", value)

/** Decimal formatter that avoids JVM-only `String.format`. */
internal fun formatDecimal(value: Float, decimals: Int): String {
    val sign = if (value < 0f) "-" else ""
    val multiplier = tenPow(decimals)
    val scaled = (abs(value) * multiplier).roundToInt()
    val whole = scaled / multiplier
    val fraction = scaled % multiplier

    return if (decimals == 0) {
        "$sign$whole"
    } else {
        "$sign$whole.${fraction.toString().padStart(decimals, '0')}"
    }
}

private fun tenPow(exponent: Int): Int {
    var value = 1
    repeat(exponent) {
        value *= 10
    }
    return value
}
