package cz.kaboom.connectioninfo.data.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import cz.kaboom.connectioninfo.domain.repository.ConnectivityObserver
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

/**
 * Observes the platform connectivity callbacks and exposes a stable online/offline stream.
 *
 * The flow emits the current state immediately, then follows validated internet connectivity so the
 * UI does not offer tests while Android knows the network cannot reach the internet.
 */
class AndroidConnectivityObserver @Inject constructor(
    @ApplicationContext context: Context
) : ConnectivityObserver {

    /** System service used to query active capabilities and register network callbacks. */
    private val connectivityManager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /** Emits distinct validated connectivity states and conflates fast callback bursts. */
    override val isConnected: Flow<Boolean> = callbackFlow {
        /** Reads the currently active network and verifies that Android has validated it. */
        fun currentState(): Boolean = connectivityManager.activeNetwork
            ?.let(connectivityManager::getNetworkCapabilities)
            ?.isOnline()
            ?: false

        /** Bridges Android's callback API into the coroutine flow above. */
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(currentState())
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                trySend(networkCapabilities.isOnline())
            }

            override fun onLost(network: Network) {
                trySend(currentState())
            }

            override fun onUnavailable() {
                trySend(false)
            }
        }

        trySend(currentState())

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)
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
