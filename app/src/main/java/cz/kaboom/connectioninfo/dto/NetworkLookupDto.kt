package cz.kaboom.connectioninfo.dto

import com.google.gson.annotations.SerializedName

data class NetworkLookupDto
    (
    @SerializedName("as") val autonomousSystem: String = "",
    @SerializedName("city") val city: String = "",
    @SerializedName("country") val country: String = "",
    @SerializedName("countryCode") val countryCode: String = "",
    @SerializedName("isp") val isp: String = "",
    @SerializedName("lat") val latitude: String = "",
    @SerializedName("lon") val longitude: String = "",
    @SerializedName("org") val organization: String = "",
    @SerializedName("query") val query: String = "",
    @SerializedName("region") val region: String = "",
    @SerializedName("regionName") val regionName: String = "",
    @SerializedName("status") val status: String = "",
    @SerializedName("timezone") val timezone: String = "",
    @SerializedName("zip") val zip: String = ""
)
