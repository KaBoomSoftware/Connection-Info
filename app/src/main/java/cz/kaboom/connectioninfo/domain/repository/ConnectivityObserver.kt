package cz.kaboom.connectioninfo.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for observing whether the device currently has validated internet access.
 */
interface ConnectivityObserver {
    /** Emits true when the active network can reach the internet, false otherwise. */
    val isConnected: Flow<Boolean>
}
