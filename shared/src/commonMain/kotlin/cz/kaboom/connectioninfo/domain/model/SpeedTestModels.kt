package cz.kaboom.connectioninfo.domain.model

/**
 * Current phase of the speed-test state machine.
 */
enum class SpeedTestPhase {
    /** No speed test is currently active. */
    IDLE,

    /** Latency samples are being collected. */
    PING,

    /** Download throughput is being measured. */
    DOWNLOAD,

    /** Upload throughput is being measured. */
    UPLOAD
}

/**
 * Streamed domain events emitted by [cz.kaboom.connectioninfo.domain.repository.SpeedTestRepository].
 */
sealed interface SpeedTestUpdate {
    /** Emitted once before the first measurement phase starts. */
    data object Started : SpeedTestUpdate

    /** Emitted when all phases complete or a running test is cancelled cleanly. */
    data object Finished : SpeedTestUpdate

    /** Throughput progress for either download or upload phases. */
    data class Progress(
        /** Phase this throughput sample belongs to. */
        val phase: SpeedTestPhase,

        /** Phase progress in the range 0..100. */
        val percent: Float,

        /** Measured throughput in bits per second. */
        val bitsPerSecond: Double
    ) : SpeedTestUpdate

    /** Latency progress emitted during the ping phase. */
    data class Latency(
        /** Ping phase progress in the range 0..100. */
        val percent: Float,

        /** Measured request latency in milliseconds. */
        val milliseconds: Double
    ) : SpeedTestUpdate

    /** Terminal failure event with a user-facing message and optional cause. */
    data class Failed(
        /** Message safe to surface in the UI. */
        val message: String,

        /** Original exception retained for diagnostics. */
        val cause: Throwable? = null
    ) : SpeedTestUpdate
}
