package cz.kaboom.connectioninfo.domain.model

enum class NetworkTransport(val displayName: String) {
    WIFI("WIFI"),
    CELLULAR("Cellular"),
    UNKNOWN("Unknown")
}

data class NetworkLookup(
    val isp: String = "",
    val organization: String = "",
    val city: String = "",
    val region: String = "",
    val regionName: String = "",
    val country: String = "",
    val countryCode: String = "",
    val latitude: String = "",
    val longitude: String = ""
)

data class NetworkDetails(
    val transport: NetworkTransport = NetworkTransport.UNKNOWN,
    val internalIp: String = "",
    val externalIp: String = "",
    val lookup: NetworkLookup = NetworkLookup()
)
