package cz.kaboom.connectioninfo.domain.model

enum class SpeedTestPhase {
    IDLE,
    PING,
    DOWNLOAD,
    UPLOAD
}

sealed interface SpeedTestUpdate {
    data object Started : SpeedTestUpdate
    data object Finished : SpeedTestUpdate

    data class Progress(
        val phase: SpeedTestPhase,
        val percent: Float,
        val bitsPerSecond: Double
    ) : SpeedTestUpdate

    data class Latency(
        val percent: Float,
        val milliseconds: Double
    ) : SpeedTestUpdate

    data class Failed(
        val message: String,
        val cause: Throwable? = null
    ) : SpeedTestUpdate
}
