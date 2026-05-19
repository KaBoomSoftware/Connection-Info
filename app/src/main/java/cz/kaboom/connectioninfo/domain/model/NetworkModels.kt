package cz.kaboom.connectioninfo.domain.model

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

/**
 * Public IP metadata returned by the remote lookup service and normalized for the UI.
 */
data class NetworkLookup(
    /** Internet service provider name. */
    val isp: String = "",

    /** Organization or allocation name associated with the public IP. */
    val organization: String = "",

    /** City reported by the lookup service. */
    val city: String = "",

    /** Region code reported by the lookup service. */
    val region: String = "",

    /** Human-readable region name. */
    val regionName: String = "",

    /** Country name reported by the lookup service. */
    val country: String = "",

    /** ISO-style country code reported by the lookup service. */
    val countryCode: String = "",

    /** Latitude string preserved from the transport response. */
    val latitude: String = "",

    /** Longitude string preserved from the transport response. */
    val longitude: String = ""
)

/**
 * Complete snapshot shown on the Network Info tab.
 */
data class NetworkDetails(
    /** Active network transport. */
    val transport: NetworkTransport = NetworkTransport.UNKNOWN,

    /** Local interface address visible from the device. */
    val internalIp: String = "",

    /** Public IP address visible to internet services. */
    val externalIp: String = "",

    /** Metadata associated with [externalIp]. */
    val lookup: NetworkLookup = NetworkLookup()
)
