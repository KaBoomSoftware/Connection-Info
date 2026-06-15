package cz.kaboom.connectioninfo.domain.model.network

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
