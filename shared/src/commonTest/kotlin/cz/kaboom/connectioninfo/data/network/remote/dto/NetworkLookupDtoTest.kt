package cz.kaboom.connectioninfo.data.network.remote.dto

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

/** Regression coverage for ip-api.com responses where coordinates are numeric JSON values. */
class NetworkLookupDtoTest {

    @Test
    fun decodesNumericCoordinatesAsDisplayText() {
        val dto = json.decodeFromString<NetworkLookupDto>(
            """
            {
              "status": "success",
              "country": "Czechia",
              "countryCode": "CZ",
              "lat": 50.0948,
              "lon": 14.4785,
              "query": "193.86.34.190"
            }
            """.trimIndent()
        )

        assertEquals("50.0948", dto.latitude.asDisplayString())
        assertEquals("14.4785", dto.longitude.asDisplayString())
    }

    private companion object {
        val json = Json { ignoreUnknownKeys = true }
    }
}
