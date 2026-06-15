package cz.kaboom.connectioninfo.data.speedtest

/** Central place for speed-test endpoints and timing values. */
internal object SpeedTestConfig {
    /** Lightweight Cloudflare endpoint used for latency samples. */
    const val pingUrl = "https://speed.cloudflare.com/__down?bytes=1"

    /** Large remote file streamed for the timed download measurement. */
    const val downloadUrl = "https://proof.ovh.net/files/100Mb.dat"

    /** Cloudflare upload endpoint accepting the generated binary test payload. */
    const val uploadUrl = "https://speed.cloudflare.com/__up"

    /** Number of latency probes collected before throughput phases begin. */
    const val pingSampleCount = 5

    /** Duration of the download phase in milliseconds. */
    const val downloadDurationMs = 6000L

    /** Duration of the upload phase in milliseconds. */
    const val uploadDurationMs = 5000L

    /** Number of bytes streamed into the upload request body. */
    const val uploadFileSize = 50 * 1000000

    /** Maximum speed displayed on the gauge in Mbps. */
    const val gaugeMaxValue = 1000f
}
