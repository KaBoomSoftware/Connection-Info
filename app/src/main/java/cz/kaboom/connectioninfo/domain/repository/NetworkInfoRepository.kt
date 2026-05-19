package cz.kaboom.connectioninfo.domain.repository

import cz.kaboom.connectioninfo.domain.model.NetworkDetails

interface NetworkInfoRepository {
    suspend fun refresh(): Result<NetworkDetails>
}
