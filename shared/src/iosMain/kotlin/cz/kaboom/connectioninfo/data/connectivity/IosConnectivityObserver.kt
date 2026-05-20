package cz.kaboom.connectioninfo.data.connectivity

import cz.kaboom.connectioninfo.domain.repository.ConnectivityObserver
import cz.kaboom.connectioninfo.domain.repository.ConnectivityStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** Minimal iOS connectivity source; lookup failures still surface through repository results. */
class IosConnectivityObserver : ConnectivityObserver {
    override val status: Flow<ConnectivityStatus> = flowOf(
        ConnectivityStatus(isConnected = true, activeNetworkKey = "ios")
    )
}
