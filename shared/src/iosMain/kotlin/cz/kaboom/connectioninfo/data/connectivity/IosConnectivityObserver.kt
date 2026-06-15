package cz.kaboom.connectioninfo.data.connectivity

import cz.kaboom.connectioninfo.domain.repository.ConnectivityObserver
import cz.kaboom.connectioninfo.domain.repository.ConnectivityStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import platform.Network.NWPathMonitor
import platform.Network.NWPath
import platform.Network.nw_path_status_satisfied
import platform.darwin.dispatch_queue_create
import platform.darwin.DISPATCH_QUEUE_SERIAL

class IosConnectivityObserver : ConnectivityObserver {
    override val status: Flow<ConnectivityStatus> = callbackFlow {
        val monitor = NWPathMonitor()
        val queue = dispatch_queue_create("connectivity", DISPATCH_QUEUE_SERIAL)

        monitor.setUpdateHandler { path: NWPath ->
            trySend(path.toStatus())
        }

        monitor.startWithQueue(queue)

        // Emit current state immediately before the first callback fires.
        trySend(monitor.currentPath.toStatus())

        awaitClose { monitor.cancel() }
    }
        .distinctUntilChanged()
        .conflate()
}

private fun NWPath.toStatus() = ConnectivityStatus(
    isConnected = status == nw_path_status_satisfied,
    activeNetworkKey = status.toString()
)
