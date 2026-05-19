package cz.kaboom.connectioninfo.data.network

import cz.kaboom.connectioninfo.common.Const
import cz.kaboom.connectioninfo.dto.NetworkLookupDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import javax.inject.Inject

class NetworkLookupClient @Inject constructor(
    private val client: HttpClient
) {
    suspend fun getMyExternalIp(): String {
        val response = client.get(Const.IPAPI_BASE_URL)
        check(response.status.isSuccess()) {
            "Cannot get external IP address: HTTP ${response.status.value}"
        }
        return response.bodyAsText()
    }

    suspend fun getLookupData(ip: String): NetworkLookupDto {
        val response = client.get("${Const.LOOKUP_BASE_URL}$ip")
        check(response.status.isSuccess()) {
            "Cannot get network lookup data: HTTP ${response.status.value}"
        }
        return response.body()
    }
}
