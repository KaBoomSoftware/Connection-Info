package cz.kaboom.connectioninfo.dto

import com.google.gson.annotations.SerializedName

/**
 * Raw DTO matching the ip-api.com JSON response.
 *
 * The app maps only the display-relevant fields into the domain model, but keeping the complete
 * response shape here makes future UI additions straightforward.
 */
data class NetworkLookupDto(
    /** Autonomous system description. */
    @SerializedName("as") val autonomousSystem: String = "",

    /** City returned for the queried IP. */
    @SerializedName("city") val city: String = "",

    /** Country returned for the queried IP. */
    @SerializedName("country") val country: String = "",

    /** Country code returned for the queried IP. */
    @SerializedName("countryCode") val countryCode: String = "",

    /** Internet service provider name. */
    @SerializedName("isp") val isp: String = "",

    /** Latitude value serialized as text for presentation stability. */
    @SerializedName("lat") val latitude: String = "",

    /** Longitude value serialized as text for presentation stability. */
    @SerializedName("lon") val longitude: String = "",

    /** Organization associated with the queried IP. */
    @SerializedName("org") val organization: String = "",

    /** Queried public IP echoed by the service. */
    @SerializedName("query") val query: String = "",

    /** Region code returned by the service. */
    @SerializedName("region") val region: String = "",

    /** Human-readable region name returned by the service. */
    @SerializedName("regionName") val regionName: String = "",

    /** Service status field. */
    @SerializedName("status") val status: String = "",

    /** Time zone associated with the queried IP. */
    @SerializedName("timezone") val timezone: String = "",

    /** ZIP or postal code associated with the queried IP. */
    @SerializedName("zip") val zip: String = ""
)
