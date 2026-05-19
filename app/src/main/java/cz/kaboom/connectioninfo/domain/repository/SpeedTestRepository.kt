package cz.kaboom.connectioninfo.domain.repository

import cz.kaboom.connectioninfo.domain.model.SpeedTestUpdate
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for executing a multi-phase network speed test.
 */
interface SpeedTestRepository {
    /** Returns a cold flow that emits test lifecycle, latency, and throughput events. */
    fun runSpeedTest(): Flow<SpeedTestUpdate>
}
