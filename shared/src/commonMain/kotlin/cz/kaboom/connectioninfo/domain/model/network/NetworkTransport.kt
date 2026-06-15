package cz.kaboom.connectioninfo.domain.model.network

/**
 * Transport category currently backing the active network.
 *
 * [displayName] is intentionally UI-ready because the set of values is small and stable.
 */
enum class NetworkTransport(val displayName: String) {
    /** Wi-Fi transport reported by Android network capabilities. */
    WIFI("WIFI"),

    /** Cellular transport reported by Android network capabilities. */
    CELLULAR("Cellular"),

    /** Fallback when Android cannot provide a known transport. */
    UNKNOWN("Unknown")
}
