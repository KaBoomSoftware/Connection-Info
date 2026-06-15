package cz.kaboom.connectioninfo.data.network.remote.dto

import cz.kaboom.connectioninfo.data.network.networkJson
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Serialization and asDisplayString coverage for ip-api.com responses. */
class NetworkLookupDtoTest {

    // ─── asDisplayString ────────────────────────────────────────────────────────

    @Test
    fun asDisplayStringOnNumericPrimitiveReturnsContentString() {
        assertEquals("50.0948", JsonPrimitive(50.0948).asDisplayString())
    }

    @Test
    fun asDisplayStringOnStringPrimitiveReturnsValue() {
        assertEquals("50.0948", JsonPrimitive("50.0948").asDisplayString())
    }

    @Test
    fun asDisplayStringOnNullElementReturnsEmpty() {
        assertEquals("", null.asDisplayString())
    }

    // ─── deserialization ────────────────────────────────────────────────────────

    @Test
    fun decodesNumericCoordinatesAsDisplayText() {
        val dto = baseJson.decodeFromString<NetworkLookupDto>(
            """{"lat": 50.0948, "lon": 14.4785, "status": "success", "query": "1.2.3.4"}"""
        )
        assertEquals("50.0948", dto.latitude.asDisplayString())
        assertEquals("14.4785", dto.longitude.asDisplayString())
    }

    @Test
    fun decodesStringCoordinatesAsDisplayText() {
        val dto = baseJson.decodeFromString<NetworkLookupDto>(
            """{"lat": "50.5", "lon": "14.5"}"""
        )
        assertEquals("50.5", dto.latitude.asDisplayString())
        assertEquals("14.5", dto.longitude.asDisplayString())
    }

    @Test
    fun missingCoordinatesProduceEmptyDisplayString() {
        val dto = baseJson.decodeFromString<NetworkLookupDto>("{}")
        assertEquals("", dto.latitude.asDisplayString())
        assertEquals("", dto.longitude.asDisplayString())
    }

    @Test
    fun decodesFullResponseWithAllFields() {
        val dto = baseJson.decodeFromString<NetworkLookupDto>(
            """
            {
              "status": "success",
              "country": "Czechia",
              "countryCode": "CZ",
              "region": "PR",
              "regionName": "Prague",
              "city": "Prague",
              "isp": "T-Mobile CZ",
              "org": "AS12345 TMCZ",
              "as": "AS12345 T-Mobile",
              "query": "193.86.34.190",
              "timezone": "Europe/Prague",
              "zip": "11000",
              "lat": 50.0948,
              "lon": 14.4785
            }
            """.trimIndent()
        )

        assertEquals("success", dto.status)
        assertEquals("Czechia", dto.country)
        assertEquals("CZ", dto.countryCode)
        assertEquals("PR", dto.region)
        assertEquals("Prague", dto.regionName)
        assertEquals("Prague", dto.city)
        assertEquals("T-Mobile CZ", dto.isp)
        assertEquals("AS12345 TMCZ", dto.organization)
        assertEquals("AS12345 T-Mobile", dto.autonomousSystem)
        assertEquals("193.86.34.190", dto.query)
        assertEquals("Europe/Prague", dto.timezone)
        assertEquals("11000", dto.zip)
    }

    @Test
    fun missingOptionalFieldsUseStringDefaults() {
        val dto = baseJson.decodeFromString<NetworkLookupDto>("{}")
        assertEquals("", dto.country)
        assertEquals("", dto.countryCode)
        assertEquals("", dto.city)
        assertEquals("", dto.isp)
        assertEquals("", dto.organization)
        assertEquals("", dto.region)
        assertEquals("", dto.regionName)
        assertEquals("", dto.status)
        assertEquals("", dto.query)
        assertEquals("", dto.timezone)
        assertEquals("", dto.zip)
        assertNull(dto.latitude)
        assertNull(dto.longitude)
    }

    @Test
    fun unknownFieldsAreIgnored() {
        val dto = baseJson.decodeFromString<NetworkLookupDto>(
            """{"unknownFieldXyz": "some value", "country": "Czechia"}"""
        )
        assertEquals("Czechia", dto.country)
    }

    @Test
    fun productionJsonConfigCoercesNullStringFieldToDefault() {
        // networkJson has coerceInputValues = true: null where String is expected → ""
        val dto = networkJson.decodeFromString<NetworkLookupDto>(
            """{"country": null}"""
        )
        assertEquals("", dto.country)
    }

    private companion object {
        val baseJson = Json { ignoreUnknownKeys = true }
    }
}
