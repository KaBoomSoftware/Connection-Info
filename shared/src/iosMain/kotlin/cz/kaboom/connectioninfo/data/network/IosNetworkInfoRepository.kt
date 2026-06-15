package cz.kaboom.connectioninfo.data.network

import cz.kaboom.connectioninfo.data.network.remote.NetworkLookupClient
import cz.kaboom.connectioninfo.domain.model.network.NetworkDetails
import cz.kaboom.connectioninfo.domain.model.network.NetworkTransport
import cz.kaboom.connectioninfo.domain.repository.NetworkInfoRepository
import dev.zacsweers.metro.Inject
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import platform.darwin.freeifaddrs
import platform.darwin.getifaddrs
import platform.darwin.ifaddrs
import platform.posix.AF_INET
import platform.posix.AF_INET6
import platform.posix.NI_MAXHOST
import platform.posix.NI_NUMERICHOST
import platform.posix.getnameinfo
import platform.posix.sockaddr
import platform.posix.sockaddr_in
import platform.posix.sockaddr_in6

/** iOS implementation combining the public IP lookup service with shared domain models. */
@OptIn(ExperimentalForeignApi::class)
class IosNetworkInfoRepository @Inject constructor(
    private val networkLookupClient: NetworkLookupClient
) : NetworkInfoRepository {

    override suspend fun refresh(): Result<NetworkDetails> = runCatching {
        val externalIp = networkLookupClient.getMyExternalIp().requireValidExternalIp()
        val localNetwork = resolveLocalNetwork()

        NetworkDetails(
            transport = localNetwork.transport,
            internalIp = localNetwork.internalIp,
            externalIp = externalIp,
            lookup = networkLookupClient.getLookupData(externalIp).toDomain()
        )
    }

    private fun resolveLocalNetwork(): LocalNetworkSnapshot {
        return interfaceAddressCandidates()
            .minByOrNull(LocalAddressCandidate::priority)
            ?.let { LocalNetworkSnapshot(transport = it.transport, internalIp = it.address.uppercase()) }
            ?: LocalNetworkSnapshot()
    }

    private fun interfaceAddressCandidates(): List<LocalAddressCandidate> = memScoped {
        val interfaces = alloc<CPointerVar<ifaddrs>>()
        if (getifaddrs(interfaces.ptr) != 0) {
            return@memScoped emptyList()
        }

        val candidates = mutableListOf<LocalAddressCandidate>()
        try {
            var cursor = interfaces.value
            while (cursor != null) {
                cursor.readAddressCandidate()?.let(candidates::add)
                cursor = cursor.pointed.ifa_next
            }
        } finally {
            freeifaddrs(interfaces.value)
        }
        candidates
    }

    private fun CPointer<ifaddrs>.readAddressCandidate(): LocalAddressCandidate? {
        val addressPointer = pointed.ifa_addr ?: return null
        val family = addressPointer.pointed.sa_family.toInt()
        if (family != AF_INET && family != AF_INET6) return null

        val interfaceName = pointed.ifa_name?.toKString().orEmpty()
        if (interfaceName == LOOPBACK_INTERFACE) return null

        val address = addressPointer.toHostAddress(family) ?: return null
        if (address.isLocalOnlyAddress()) return null

        return LocalAddressCandidate(
            address = address,
            transport = interfaceName.toNetworkTransport(),
            priority = interfaceName.interfacePriority() + family.addressPriority()
        )
    }

    private fun CPointer<sockaddr>.toHostAddress(family: Int): String? = memScoped {
        val host = allocArray<ByteVar>(NI_MAXHOST)
        val addressLength = when (family) {
            AF_INET -> sizeOf<sockaddr_in>()
            AF_INET6 -> sizeOf<sockaddr_in6>()
            else -> return@memScoped null
        }

        val result = getnameinfo(
            this@toHostAddress,
            addressLength.convert(),
            host,
            NI_MAXHOST.convert(),
            null,
            0.convert(),
            NI_NUMERICHOST
        )

        if (result == 0) {
            host.toKString().substringBefore('%')
        } else {
            null
        }
    }

    private fun String.toNetworkTransport(): NetworkTransport = when {
        this == WIFI_INTERFACE || startsWith(WIFI_INTERFACE_PREFIX) -> NetworkTransport.WIFI
        startsWith(CELLULAR_INTERFACE_PREFIX) -> NetworkTransport.CELLULAR
        else -> NetworkTransport.UNKNOWN
    }

    private fun String.interfacePriority(): Int = when {
        this == WIFI_INTERFACE -> 0
        startsWith(CELLULAR_INTERFACE_PREFIX) -> 2
        startsWith(WIFI_INTERFACE_PREFIX) -> 4
        else -> 20
    }

    private fun Int.addressPriority(): Int = when (this) {
        AF_INET -> 0
        AF_INET6 -> 1
        else -> 10
    }

    private fun String.isLocalOnlyAddress(): Boolean {
        val normalized = lowercase()
        return normalized == IPV6_LOOPBACK ||
            normalized.startsWith(IPV4_LOOPBACK_PREFIX) ||
            normalized.startsWith(IPV4_LINK_LOCAL_PREFIX) ||
            normalized.startsWith(IPV6_LINK_LOCAL_PREFIX)
    }

    private data class LocalNetworkSnapshot(
        val transport: NetworkTransport = NetworkTransport.UNKNOWN,
        val internalIp: String = ""
    )

    private data class LocalAddressCandidate(
        val address: String,
        val transport: NetworkTransport,
        val priority: Int
    )

    private companion object {
        const val LOOPBACK_INTERFACE = "lo0"
        const val WIFI_INTERFACE = "en0"
        const val WIFI_INTERFACE_PREFIX = "en"
        const val CELLULAR_INTERFACE_PREFIX = "pdp_ip"
        const val IPV4_LOOPBACK_PREFIX = "127."
        const val IPV4_LINK_LOCAL_PREFIX = "169.254."
        const val IPV6_LOOPBACK = "::1"
        const val IPV6_LINK_LOCAL_PREFIX = "fe80"
    }
}
