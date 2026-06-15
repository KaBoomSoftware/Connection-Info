package cz.kaboom.connectioninfo.data.network

private const val MinimumIpLength = 8

/** Normalizes and validates the external IP returned by the lookup service. */
internal fun String.requireValidExternalIp(): String {
    return trim().also { require(it.length > MinimumIpLength) { "Invalid external IP address" } }
}
