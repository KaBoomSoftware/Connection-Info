package cz.kaboom.connectioninfo.domain.repository

import cz.kaboom.connectioninfo.domain.model.network.NetworkDetails

/**
 * Domain contract for retrieving the current network snapshot shown in the UI.
 */
interface NetworkInfoRepository {
    /** Refreshes local and public network information, returning failures as [Result]. */
    suspend fun refresh(): Result<NetworkDetails>
}
