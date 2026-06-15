package cz.kaboom.connectioninfo.data.network

import cz.kaboom.connectioninfo.data.network.remote.dto.NetworkLookupDto
import cz.kaboom.connectioninfo.domain.model.network.NetworkLookup
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class NetworkLookupMapperTest {

    @Test
    fun toDomainMapsAllTextFields() {
        val dto = NetworkLookupDto(
            isp = "Example ISP",
            organization = "AS12345 Example",
            city = "Prague",
            region = "PR",
            regionName = "Prague Region",
            country = "Czechia",
            countryCode = "CZ"
        )

        val domain = dto.toDomain()

        assertEquals("Example ISP", domain.isp)
        assertEquals("AS12345 Example", domain.organization)
        assertEquals("Prague", domain.city)
        assertEquals("PR", domain.region)
        assertEquals("Prague Region", domain.regionName)
        assertEquals("Czechia", domain.country)
        assertEquals("CZ", domain.countryCode)
    }

    @Test
    fun toDomainConvertsStringCoordinates() {
        val dto = NetworkLookupDto(
            latitude = JsonPrimitive("50.0948"),
            longitude = JsonPrimitive("14.4785")
        )
        val domain = dto.toDomain()
        assertEquals("50.0948", domain.latitude)
        assertEquals("14.4785", domain.longitude)
    }

    @Test
    fun toDomainHandlesNullCoordinatesAsEmptyStrings() {
        val domain = NetworkLookupDto(latitude = null, longitude = null).toDomain()
        assertEquals("", domain.latitude)
        assertEquals("", domain.longitude)
    }

    @Test
    fun toDomainWithDefaultDtoProducesDefaultNetworkLookup() {
        assertEquals(NetworkLookup(), NetworkLookupDto().toDomain())
    }

    @Test
    fun toDomainMapsFullDtoRoundTrip() {
        val dto = NetworkLookupDto(
            isp = "ISP Name",
            organization = "ORG Name",
            city = "Brno",
            region = "JM",
            regionName = "Jihomoravský kraj",
            country = "Czechia",
            countryCode = "CZ",
            latitude = JsonPrimitive("49.1951"),
            longitude = JsonPrimitive("16.6068")
        )

        val expected = NetworkLookup(
            isp = "ISP Name",
            organization = "ORG Name",
            city = "Brno",
            region = "JM",
            regionName = "Jihomoravský kraj",
            country = "Czechia",
            countryCode = "CZ",
            latitude = "49.1951",
            longitude = "16.6068"
        )

        assertEquals(expected, dto.toDomain())
    }
}
