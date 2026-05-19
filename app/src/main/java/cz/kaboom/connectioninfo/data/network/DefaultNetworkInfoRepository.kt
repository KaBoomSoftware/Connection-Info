package cz.kaboom.connectioninfo.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkAddress
import android.net.NetworkCapabilities
import cz.kaboom.connectioninfo.domain.model.NetworkDetails
import cz.kaboom.connectioninfo.domain.model.NetworkLookup
import cz.kaboom.connectioninfo.domain.model.NetworkTransport
import cz.kaboom.connectioninfo.domain.repository.NetworkInfoRepository
import cz.kaboom.connectioninfo.dto.NetworkLookupDto
import cz.kaboom.connectioninfo.di.modules.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

/**
 * Android-backed implementation that combines local link data with external IP lookup metadata.
 *
 * All work is moved to the injected IO dispatcher because it touches platform network interfaces
 * and performs remote lookups through [NetworkLookupClient].
 */
class DefaultNetworkInfoRepository @Inject constructor(
    @ApplicationContext context: Context,
    private val networkLookupClient: NetworkLookupClient,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : NetworkInfoRepository {

    /** Application context retained to avoid leaking an activity. */
    private val appContext = context.applicationContext

    /** Platform service used for validated network state and transport detection. */
    private val connectivityManager =
        appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /** Refreshes all visible network details and wraps failures in [Result]. */
    override suspend fun refresh(): Result<NetworkDetails> = withContext(ioDispatcher) {
        runCatching {
            require(isConnected()) { "Network is unavailable" }

            val transport = currentTransport()
            val internalIp = resolveInternalIp()
            val externalLookup = runCatching {
                val externalIp = networkLookupClient.getMyExternalIp()
                    .trim()
                    .also { require(it.length > MIN_IP_LENGTH) { "Invalid external IP address" } }
                externalIp to networkLookupClient.getLookupData(externalIp).toDomain()
            }

            NetworkDetails(
                transport = transport,
                internalIp = internalIp,
                externalIp = externalLookup.getOrNull()?.first.orEmpty(),
                lookup = externalLookup.getOrNull()?.second ?: NetworkLookup()
            )
        }
    }

    /** Confirms that Android currently has a validated internet-capable active network. */
    private fun isConnected(): Boolean {
        val capabilities = connectivityManager.activeNetwork
            ?.let(connectivityManager::getNetworkCapabilities)
            ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /** Maps platform transport capabilities into the small domain enum shown in the UI. */
    private fun currentTransport(): NetworkTransport {
        val capabilities = connectivityManager.activeNetwork
            ?.let(connectivityManager::getNetworkCapabilities)
            ?: return NetworkTransport.UNKNOWN

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkTransport.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkTransport.CELLULAR
            else -> NetworkTransport.UNKNOWN
        }
    }

    /** Finds a local address from Android's active network and normalizes IPv6 zone suffixes away. */
    private fun resolveInternalIp(): String {
        val activeNetwork = connectivityManager.activeNetwork ?: return ""

        return connectivityManager.getLinkProperties(activeNetwork)
            ?.linkAddresses
            ?.asSequence()
            ?.map(LinkAddress::getAddress)
            ?.firstOrNull { !it.isLoopbackAddress && !it.isLinkLocalAddress }
            ?.hostAddress
            ?.substringBefore('%')
            ?.uppercase(Locale.getDefault())
            .orEmpty()
    }

    /** Converts the transport DTO into the immutable domain object used by presentation code. */
    private fun NetworkLookupDto.toDomain() = NetworkLookup(
        isp = isp,
        organization = organization,
        city = city,
        region = region,
        regionName = regionName,
        country = country,
        countryCode = countryCode,
        latitude = latitude,
        longitude = longitude
    )

    /** Internal validation threshold that filters obviously malformed external IP responses. */
    private companion object {
        const val MIN_IP_LENGTH = 8
    }
}
