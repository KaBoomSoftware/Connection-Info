package cz.kaboom.connectioninfo.domain.model.network

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
