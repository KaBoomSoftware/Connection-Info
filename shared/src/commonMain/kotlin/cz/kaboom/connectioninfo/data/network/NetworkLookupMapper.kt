package cz.kaboom.connectioninfo.data.network

import cz.kaboom.connectioninfo.data.network.remote.dto.NetworkLookupDto
import cz.kaboom.connectioninfo.data.network.remote.dto.asDisplayString
import cz.kaboom.connectioninfo.domain.model.network.NetworkLookup

/** Converts the transport DTO into the immutable domain object used by presentation code. */
internal fun NetworkLookupDto.toDomain() = NetworkLookup(
    isp = isp,
    organization = organization,
    city = city,
    region = region,
    regionName = regionName,
    country = country,
    countryCode = countryCode,
    latitude = latitude.asDisplayString(),
    longitude = longitude.asDisplayString()
)
