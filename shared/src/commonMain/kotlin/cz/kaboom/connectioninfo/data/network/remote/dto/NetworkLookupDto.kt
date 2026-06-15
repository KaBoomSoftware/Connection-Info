package cz.kaboom.connectioninfo.data.network.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * Raw DTO matching the ip-api.com JSON response.
 *
 * The app maps only the display-relevant fields into the domain model, but keeping the complete
 * response shape here makes future UI additions straightforward.
 */
@Serializable
data class NetworkLookupDto(
    /** Autonomous system description. */
    @SerialName("as") val autonomousSystem: String = "",

    /** City returned for the queried IP. */
    @SerialName("city") val city: String = "",

    /** Country returned for the queried IP. */
    @SerialName("country") val country: String = "",

    /** Country code returned for the queried IP. */
    @SerialName("countryCode") val countryCode: String = "",

    /** Internet service provider name. */
    @SerialName("isp") val isp: String = "",

    /** Latitude value can arrive as a number or string depending on the lookup response. */
    @SerialName("lat") val latitude: JsonElement? = null,

    /** Longitude value can arrive as a number or string depending on the lookup response. */
    @SerialName("lon") val longitude: JsonElement? = null,

    /** Organization associated with the queried IP. */
    @SerialName("org") val organization: String = "",

    /** Queried public IP echoed by the service. */
    @SerialName("query") val query: String = "",

    /** Region code returned by the service. */
    @SerialName("region") val region: String = "",

    /** Human-readable region name returned by the service. */
    @SerialName("regionName") val regionName: String = "",

    /** Service status field. */
    @SerialName("status") val status: String = "",

    /** Time zone associated with the queried IP. */
    @SerialName("timezone") val timezone: String = "",

    /** ZIP or postal code associated with the queried IP. */
    @SerialName("zip") val zip: String = ""
)

/** Converts primitive JSON values into stable display text. */
fun JsonElement?.asDisplayString(): String {
    return (this as? JsonPrimitive)?.contentOrNull.orEmpty()
}
