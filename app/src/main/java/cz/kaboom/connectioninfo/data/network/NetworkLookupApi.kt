package cz.kaboom.connectioninfo.data.network

import cz.kaboom.connectioninfo.dto.NetworkLookupDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface NetworkLookupApi {
    @GET("/")
    suspend fun getMyExternalIp(): Response<String>

    @GET("{ip_address}")
    suspend fun getLookupData(
        @Path(value = "ip_address") ip: String
    ): Response<NetworkLookupDto>
}
