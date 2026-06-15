package cz.kaboom.connectioninfo.data.network.remote

import cz.kaboom.connectioninfo.data.network.NetworkLookupServiceConfig
import cz.kaboom.connectioninfo.data.network.remote.dto.NetworkLookupDto
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

/**
 * Thin Ktor client wrapper for external IP and IP metadata lookups.
 *
 * Keeping endpoint-level calls here lets repositories stay focused on composing domain models and
 * keeps HTTP status validation close to the transport boundary.
 */
class NetworkLookupClient @Inject constructor(
    private val client: HttpClient
) {
    /** Fetches the device's public IP address as plain text. */
    suspend fun getMyExternalIp(): String {
        val response = client.get(NetworkLookupServiceConfig.externalIpUrl)
        check(response.status.isSuccess()) {
            "Cannot get external IP address: HTTP ${response.status.value}"
        }
        return response.bodyAsText()
    }

    /** Fetches geolocation and ISP metadata for [ip]. */
    suspend fun getLookupData(ip: String): NetworkLookupDto {
        val response = client.get("${NetworkLookupServiceConfig.lookupBaseUrl}$ip")
        check(response.status.isSuccess()) {
            "Cannot get network lookup data: HTTP ${response.status.value}"
        }
        return response.body()
    }
}
