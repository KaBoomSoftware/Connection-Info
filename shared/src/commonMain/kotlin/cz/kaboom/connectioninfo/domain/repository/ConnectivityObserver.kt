package cz.kaboom.connectioninfo.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Current validated connectivity state and active Android network identity.
 *
 * [activeNetworkKey] changes when Android promotes another network as active, even if both the old
 * and new networks have internet access.
 */
data class ConnectivityStatus(
    /** True when the active network has validated internet access. */
    val isConnected: Boolean,

    /** Stable-enough identity for the active network that produced this status. */
    val activeNetworkKey: String?
)

/**
 * Domain contract for observing whether the device currently has validated internet access.
 */
interface ConnectivityObserver {
    /** Emits when either validated connectivity or the active network identity changes. */
    val status: Flow<ConnectivityStatus>

    /** Emits true when the active network can reach the internet, false otherwise. */
    val isConnected: Flow<Boolean>
        get() = status
            .map { it.isConnected }
            .distinctUntilChanged()
}
