package cz.kaboom.connectioninfo.data.network

/** Network lookup endpoints shared by platform repositories and the remote client. */
internal object NetworkLookupServiceConfig {
    /** External IP lookup endpoint returning a plain-text address. */
    const val externalIpUrl = "https://api.ipify.org"

    /** IP geolocation lookup endpoint; the target IP is appended to this base URL. */
    const val lookupBaseUrl = "http://ip-api.com/json/"
}
