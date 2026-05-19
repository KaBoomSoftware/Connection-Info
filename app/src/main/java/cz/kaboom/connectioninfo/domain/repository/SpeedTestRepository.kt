package cz.kaboom.connectioninfo.domain.repository

import cz.kaboom.connectioninfo.domain.model.SpeedTestUpdate
import kotlinx.coroutines.flow.Flow

interface SpeedTestRepository {
    fun runSpeedTest(): Flow<SpeedTestUpdate>
}
