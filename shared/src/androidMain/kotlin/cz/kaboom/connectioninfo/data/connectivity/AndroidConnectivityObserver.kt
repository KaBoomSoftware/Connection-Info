package cz.kaboom.connectioninfo.data.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import cz.kaboom.connectioninfo.domain.repository.ConnectivityObserver
import cz.kaboom.connectioninfo.domain.repository.ConnectivityStatus
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Observes the platform connectivity callbacks and exposes a stable active-network stream.
 *
 * The flow emits the current state immediately, then follows active network identity changes so the
 * UI refreshes details when Android switches between Wi-Fi and cellular without going offline.
 */
class AndroidConnectivityObserver @Inject constructor(
    context: Context
) : ConnectivityObserver {

    /** System service used to query active capabilities and register network callbacks. */
    private val connectivityManager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /** Emits distinct active network snapshots and conflates fast callback bursts. */
    override val status: Flow<ConnectivityStatus> = callbackFlow {
        /** Builds a status object from the callback network when Android has just promoted it. */
        fun statusOf(
            network: Network?,
            capabilities: NetworkCapabilities?
        ): ConnectivityStatus {
            return ConnectivityStatus(
                isConnected = capabilities?.isOnline() == true,
                activeNetworkKey = network?.toString()
            )
        }

        /** Reads the current default network and whether Android has validated it. */
        fun currentStatus(): ConnectivityStatus {
            val activeNetwork = connectivityManager.activeNetwork
            val capabilities = activeNetwork?.let(connectivityManager::getNetworkCapabilities)

            return statusOf(activeNetwork, capabilities)
        }

        /** Bridges Android's callback API into the coroutine flow above. */
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(statusOf(network, connectivityManager.getNetworkCapabilities(network)))
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                trySend(statusOf(network, networkCapabilities))
            }

            override fun onLost(network: Network) {
                trySend(currentStatus())
            }

            override fun onUnavailable() {
                trySend(currentStatus())
            }
        }

        trySend(currentStatus())

        connectivityManager.registerDefaultNetworkCallback(callback)
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }
        .distinctUntilChanged()
        .conflate()

    /** Returns true only for networks that both advertise and validate internet access. */
    private fun NetworkCapabilities.isOnline(): Boolean {
        return hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
