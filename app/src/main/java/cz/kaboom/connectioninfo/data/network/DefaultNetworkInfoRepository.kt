package cz.kaboom.connectioninfo.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import cz.kaboom.connectioninfo.domain.model.NetworkDetails
import cz.kaboom.connectioninfo.domain.model.NetworkLookup
import cz.kaboom.connectioninfo.domain.model.NetworkTransport
import cz.kaboom.connectioninfo.domain.repository.NetworkInfoRepository
import cz.kaboom.connectioninfo.dto.NetworkLookupDto
import cz.kaboom.connectioninfo.di.modules.IoDispatcher
import cz.kaboom.connectioninfo.di.modules.IpApi
import cz.kaboom.connectioninfo.di.modules.LookupApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.net.NetworkInterface
import java.util.Locale
import javax.inject.Inject

class DefaultNetworkInfoRepository @Inject constructor(
    @ApplicationContext context: Context,
    @param:IpApi private val ipApi: NetworkLookupApi,
    @param:LookupApi private val lookupApi: NetworkLookupApi,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : NetworkInfoRepository {

    private val appContext = context.applicationContext
    private val connectivityManager =
        appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override suspend fun refresh(): Result<NetworkDetails> = withContext(ioDispatcher) {
        runCatching {
            require(isConnected()) { "Network is unavailable" }

            val externalIp = ipApi.getMyExternalIp().requireBody("Cannot get external IP address")
                .trim()
                .also { require(it.length > MIN_IP_LENGTH) { "Invalid external IP address" } }

            val lookup = lookupApi.getLookupData(externalIp)
                .requireBody("Cannot get network lookup data")

            NetworkDetails(
                transport = currentTransport(),
                internalIp = resolveInternalIp(),
                externalIp = externalIp,
                lookup = lookup.toDomain()
            )
        }
    }

    private fun isConnected(): Boolean {
        val capabilities = connectivityManager.activeNetwork
            ?.let(connectivityManager::getNetworkCapabilities)
            ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

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

    private fun resolveInternalIp(): String {
        return NetworkInterface.getNetworkInterfaces()
            .asSequence()
            .flatMap { it.inetAddresses.asSequence() }
            .firstOrNull { !it.isLoopbackAddress && !it.isLinkLocalAddress }
            ?.hostAddress
            ?.substringBefore('%')
            ?.uppercase(Locale.getDefault())
            .orEmpty()
    }

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

    private fun <T> retrofit2.Response<T>.requireBody(errorMessage: String): T {
        if (!isSuccessful) error("$errorMessage: HTTP ${code()}")
        return body() ?: error(errorMessage)
    }

    private companion object {
        const val MIN_IP_LENGTH = 8
    }
}
