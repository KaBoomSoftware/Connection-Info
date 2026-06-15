package cz.kaboom.connectioninfo.domain.model.speedtest

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
